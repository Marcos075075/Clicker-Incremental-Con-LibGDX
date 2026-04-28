package com.jovellanos.clicker.core;

import com.jovellanos.clicker.upgrades.Upgrade;
import com.jovellanos.clicker.upgrades.UpgradeFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/*
    ===============================================
    GameState — Estado Central de la Partida
    ===============================================
    Objeto compartido por los tres hilos. Todos los accesos
    a datos de la partida pasan por aquí.

    ===============================================
    Por qué BigInteger para ppActual / ppHistorico
    ===============================================
    Los juegos clicker acumulan cifras que desbordan long
    (~9,2×10^18) en sesiones largas con multiplicadores altos.
    BigInteger permite crecer indefinidamente sin overflow.

    ===============================================
    Estrategia de sincronización
    ===============================================
    - ppActual / ppHistorico → AtomicReference<BigInteger>:
      El método addPP() usa un bucle CAS (compare-and-set) para
      garantizar atomicidad sin bloqueo. BigInteger es inmutable,
      por lo que el patrón es seguro y eficiente.

    - pendingClicks → AtomicLong:
      Los clics por tick son pequeños; long es más que suficiente.

    - ppPorClick / ppPorSegundo → volatile double:
      El Logic Thread escribe, el Main Thread solo lee.

    - upgrades (Map) → synchronized en takeSnapshot(), reset() y
      cargarDesdeSaveData(). PurchaseService sincroniza externamente
      sobre el monitor de este objeto.

    ===============================================
    Responsabilidades (post-refactorización 4.3)
    ===============================================
    GameState es SOLO estado. La lógica de negocio vive en:
    - PurchaseService  → compras
    - ClickHandler     → cálculo de PP por clic (combo, críticos)
    - UpgradeFactory   → cálculo de PP/clic y PP/s
*/
public class GameState {

    // ── Contadores principales (BigInteger, sin límite de desbordamiento) ─
    private final AtomicReference<BigInteger> ppActual    =
            new AtomicReference<>(BigInteger.ZERO);
    private final AtomicReference<BigInteger> ppHistorico =
            new AtomicReference<>(BigInteger.ZERO);

    // Clics pendientes: Main Thread escribe, Logic Thread drena vía ClickHandler
    private final AtomicLong pendingClicks = new AtomicLong(0);

    // ── Tasas calculadas ─────────────────────────────────────────────────
    /** PP base por clic. Escrito por PurchaseService y cargarDesdeSaveData. */
    private volatile double ppPorClick   = 1.0;
    /** PP/s total. Actualizado por el Logic Thread cada tick. */
    private volatile double ppPorSegundo = 0.0;

    // ── Mejoras ──────────────────────────────────────────────────────────
    private final Map<String, Upgrade> upgrades;

    // ── Sistema ──────────────────────────────────────────────────────────
    private volatile long   ultimoGuardado;
    private volatile String idiomaActual = "es";
    private volatile int    screenMode = 0;

    // ────────────────────────────────────────────────────────────────────
    // Constructor
    // ────────────────────────────────────────────────────────────────────

    public GameState() {
        upgrades       = UpgradeFactory.build();
        ultimoGuardado = System.currentTimeMillis();
    }

    // ────────────────────────────────────────────────────────────────────
    // API para el Main Thread (eventos de UI)
    // ────────────────────────────────────────────────────────────────────

    /** Registra un clic del usuario. Llamado desde el Main Thread. */
    public void addPendingClick() {
        pendingClicks.incrementAndGet();
    }

    // ────────────────────────────────────────────────────────────────────
    // API para el Logic Thread
    // ────────────────────────────────────────────────────────────────────

    /**
     * Devuelve y resetea atómicamente todos los clics acumulados.
     * El Logic Thread lo llama una vez por tick y pasa el resultado
     * a ClickHandler.procesarClics().
     */
    public long drainPendingClicks() {
        return pendingClicks.getAndSet(0);
    }

    /**
     * Añade PP al contador actual e histórico de forma atómica.
     * Usa un bucle CAS sobre AtomicReference<BigInteger> ya que
     * BigInteger es inmutable y no existe AtomicBigInteger en el JDK.
     */
    public void addPP(BigInteger cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) return;
        BigInteger current, updated;
        do {
            current = ppActual.get();
            updated = current.add(cantidad);
        } while (!ppActual.compareAndSet(current, updated));

        do {
            current = ppHistorico.get();
            updated = current.add(cantidad);
        } while (!ppHistorico.compareAndSet(current, updated));
    }

    /** Actualiza la tasa PP/s visible en el HUD. Llamado por el Logic Thread. */
    public void setPpPorSegundo(double pps) {
        ppPorSegundo = pps;
    }

    // ────────────────────────────────────────────────────────────────────
    // API para PurchaseService (primitivas de estado, sin reglas)
    // ────────────────────────────────────────────────────────────────────

    /**
     * Descuenta PP tras una compra exitosa usando CAS atómico.
     * PurchaseService garantiza que hay saldo antes de llamar aquí.
     */
    public void subtractPP(BigInteger cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) return;
        BigInteger current, updated;
        do {
            current = ppActual.get();
            updated = current.subtract(cantidad);
        } while (!ppActual.compareAndSet(current, updated));
    }

    /**
     * Actualiza PP/clic tras una compra o carga de partida.
     * El valor lo calcula UpgradeFactory.calcularPPporClick().
     */
    public void setPpPorClick(double ppPorClick) {
        this.ppPorClick = ppPorClick;
    }

    // ────────────────────────────────────────────────────────────────────
    // API para carga de partida
    // ────────────────────────────────────────────────────────────────────

    /**
     * Reconstruye el estado a partir de los datos del JSON.
     * Synchronized porque modifica el mapa de mejoras.
     */
    public synchronized void cargarDesdeSaveData(
            com.jovellanos.clicker.persistence.SaveManager.SaveData data) {
        if (data == null) return;

        // BigInteger serializado como String en el JSON
        ppActual.set(data.getPpActual());
        ppHistorico.set(data.getPpHistorico());
        pendingClicks.set(0);
        ppPorSegundo = data.ppPorSegundo;
        idiomaActual = data.idiomaActual;
        ultimoGuardado = data.ultimoGuardado;
        screenMode = data.screenMode;

        if (data.mejorasAdquiridas != null) {
            for (Map.Entry<String, Integer> entry : data.mejorasAdquiridas.entrySet()) {
                Upgrade upgrade = upgrades.get(entry.getKey());
                if (upgrade != null) {
                    upgrade.setQuantity(entry.getValue());
                }
            }
        }

        // Recalcular PP/clic con UpgradeFactory (sin lógica en GameState)
        ppPorClick = UpgradeFactory.calcularPPporClick(upgrades);
    }

    /**
     * Reinicia el estado a los valores iniciales.
     * No se crea un nuevo objeto porque LogicThread mantiene
     * la referencia directa a este GameState.
     */
    public synchronized void reset() {
        ppActual.set(BigInteger.ZERO);
        ppHistorico.set(BigInteger.ZERO);
        pendingClicks.set(0);
        ppPorClick   = 1.0;
        ppPorSegundo = 0.0;
        ultimoGuardado = System.currentTimeMillis();
        screenMode = 0;
        for (Upgrade u : upgrades.values()) {
            u.setQuantity(0);
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // API para el IO Thread
    // ────────────────────────────────────────────────────────────────────

    /**
     * Crea una copia inmutable del estado actual (patrón Memento).
     * El IO Thread trabaja sobre el snapshot, nunca sobre GameState.
     */
    public synchronized GameStateSnapshot takeSnapshot() {
        ultimoGuardado = System.currentTimeMillis();
        Map<String, Integer> mejoras = new HashMap<String, Integer>();
        for (Map.Entry<String, Upgrade> entry : upgrades.entrySet()) {
            mejoras.put(entry.getKey(), entry.getValue().getQuantity());
        }
        return new GameStateSnapshot(
            ppActual.get(),
            ppHistorico.get(),
            ppPorClick,
            ppPorSegundo,
            mejoras,
            idiomaActual,
            ultimoGuardado,
            screenMode
        );
    }

    // ────────────────────────────────────────────────────────────────────
    // Getters y Setters
    // ────────────────────────────────────────────────────────────────────

    public BigInteger getPpActual()       { return ppActual.get(); }
    public BigInteger getPpHistorico()    { return ppHistorico.get(); }
    public double     getPpPorClick()     { return ppPorClick; }
    public double     getPpPorSegundo()   { return ppPorSegundo; }
    public long       getUltimoGuardado() { return ultimoGuardado; }
    public String     getIdiomaActual()   { return idiomaActual; }
    public void       setIdiomaActual(String idioma) { this.idiomaActual = idioma; }
    public int        getScreenMode() { return screenMode; }
    public void       setScreenMode(int mode) { this.screenMode = mode; }

    /**
     * Acceso al mapa de mejoras para el Logic Thread, la UI y los servicios.
     * No modificar el mapa directamente: usar PurchaseService.comprar().
     */
    public Map<String, Upgrade> getUpgrades() {
        return upgrades;
    }
}