package com.jovellanos.clicker.core;

import com.jovellanos.clicker.upgrades.Upgrade;
import com.jovellanos.clicker.upgrades.UpgradeFactory;
import com.jovellanos.clicker.upgrades.DirectUpgrade;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/*
    ===============================================
    GameState — Estado Central de la Partida
    ===============================================
    Objeto compartido por los tres hilos. Todos los accesos
    a datos de la partida pasan por aquí.

    ===============================================
    Estrategia de sincronización
    ===============================================
    - ppActual / ppHistorico / pendingClicks → AtomicLong:
      operaciones atómicas sin bloqueo, el caso más frecuente.

    - ppPorClick / ppPorSegundo → volatile double:
      el Logic Thread escribe, el Main Thread solo lee.
      volatile garantiza visibilidad sin bloqueo.

    - upgrades (Map) → synchronized en purchaseUpgrade()
      y recalculatePPperClick(): la compra es un evento poco
      frecuente, el coste de synchronized es despreciable.

    ===============================================
    Flujo por hilo
    ===============================================
    Main Thread:   lee ppActual/ppPorSegundo para el HUD.
                   llama purchaseUpgrade() al comprar.
    Logic Thread:  llama drainPendingClicks(), addPP(),
                   setPpPorSegundo(), recalculatePPperClick().
    IO Thread:     llama takeSnapshot() para serializar.
*/

public class GameState {

    // ── Contadores principales ───────────────────────────────────────────
    private final AtomicLong ppActual     = new AtomicLong(0);
    private final AtomicLong ppHistorico  = new AtomicLong(0);

    // Clics pendientes de procesar: Main Thread escribe, Logic Thread draina
    private final AtomicLong pendingClicks = new AtomicLong(0);

    // ── Tasas calculadas ─────────────────────────────────────────────────
    private volatile double ppPorClick    = 1.0; // base: 1 PP por clic
    private volatile double ppPorSegundo  = 0.0;

    // ── Mejoras ──────────────────────────────────────────────────────────
    private final Map<String, Upgrade> upgrades;

    // ── Sistema ──────────────────────────────────────────────────────────
    private volatile long ultimoGuardado;
    private volatile String idiomaActual = "es";

    // ────────────────────────────────────────────────────────────────────
    // Constructor
    // ────────────────────────────────────────────────────────────────────

    public GameState() {
        upgrades = UpgradeFactory.build();
        ultimoGuardado = System.currentTimeMillis();
    }

    // ────────────────────────────────────────────────────────────────────
    // API para el Main Thread (UI / clicks)
    // ────────────────────────────────────────────────────────────────────

    /** El ClickHandler llama a esto cada vez que el usuario pulsa. */
    public void addPendingClick() {
        pendingClicks.incrementAndGet();
    }

    /**
     * Intenta comprar una unidad de la mejora indicada.
     * Synchronized: toca el mapa de mejoras y recalcula ppPorClick.
     *
     * @return true si la compra se realizó, false si no hay PP suficientes.
     */
    public synchronized boolean purchaseUpgrade(String id) {
        Upgrade upgrade = upgrades.get(id);
        if (upgrade == null) return false;

        double cost = upgrade.getCurrentCost();
        if (ppActual.get() < (long) cost) return false;

        ppActual.addAndGet(-(long) cost);
        upgrade.purchase();

        // Recalcular ppPorClick si era una directa o un multiplicador de directa
        recalculatePPperClick();
        return true;
    }

    // ────────────────────────────────────────────────────────────────────
    // API para el Logic Thread
    // ────────────────────────────────────────────────────────────────────

    /**
     * Devuelve y resetea atómicamente todos los clics acumulados.
     * El Logic Thread lo llama una vez por tick.
     */
    public long drainPendingClicks() {
        return pendingClicks.getAndSet(0);
    }

    /** Añade PP al contador actual e histórico. */
    public void addPP(long cantidad) {
        if (cantidad <= 0) return;
        ppActual.addAndGet(cantidad);
        ppHistorico.addAndGet(cantidad);
    }

    /** El Logic Thread actualiza este valor en cada tick. */
    public void setPpPorSegundo(double pps) {
        ppPorSegundo = pps;
    }

    /**
     * Recalcula ppPorClick a partir de las DirectUpgrade y sus multiplicadores.
     * Llamado también desde el Logic Thread tras drenar clics con compras nuevas.
     * Synchronized porque accede al mapa de mejoras.
     */
    public synchronized void recalculatePPperClick() {
        double total = 1.0; // base: 1 PP por clic sin mejoras
        for (DirectUpgrade du : UpgradeFactory.getDirectUpgrades(upgrades)) {
            double mult = UpgradeFactory.getMultiplierFor(du.getId(), upgrades);
            total += du.getPPperClick(mult);
        }
        ppPorClick = total;
    }

    // ────────────────────────────────────────────────────────────────────
    // API para el IO Thread
    // ────────────────────────────────────────────────────────────────────

    /**
     * Crea una copia inmutable del estado actual.
     * El IO Thread trabaja sobre el snapshot, nunca sobre GameState directamente.
     */
/*     public synchronized GameStateSnapshot takeSnapshot() {
        ultimoGuardado = System.currentTimeMillis();
        return new GameStateSnapshot(
            ppActual.get(),
            ppHistorico.get(),
            ppPorClick,
            ppPorSegundo,
            upgrades,
            ultimoGuardado
        );
    } */

    // ────────────────────────────────────────────────────────────────────
    // Getters y setters
    // ────────────────────────────────────────────────────────────────────

    public long   getPpActual()      { return ppActual.get(); }
    public long   getPpHistorico()   { return ppHistorico.get(); }
    public double getPpPorClick()    { return ppPorClick; }
    public double getPpPorSegundo()  { return ppPorSegundo; }
    public long   getUltimoGuardado(){ return ultimoGuardado; }
    public String getIdiomaActual()  { return idiomaActual; }
    public void   setIdiomaActual(String idioma){ this.idiomaActual = idioma; }

    /**
     * Acceso al mapa de mejoras para el Logic Thread y la UI.
     * No modificar el mapa desde fuera: usar purchaseUpgrade().
     */
    public Map<String, Upgrade> getUpgrades() {
        return upgrades;
    }
}