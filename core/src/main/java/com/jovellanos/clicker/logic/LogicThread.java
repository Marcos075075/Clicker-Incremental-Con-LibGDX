package com.jovellanos.clicker.logic;

import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.upgrades.AutomatedUpgrade;
import com.jovellanos.clicker.upgrades.Upgrade;
import com.jovellanos.clicker.upgrades.UpgradeFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/*
    ===============================================
    LogicThread — Motor de Cálculo
    ===============================================
    Hilo independiente a 20 ticks/segundo (cada 50 ms).

    ===============================================
    Lo que hace en cada tick
    ===============================================
    1. Drena pendingClicks del GameState.
    2. Delega en ClickHandler (combo, críticos) → BigInteger de PP.
    3. Calcula PP/s de las AutomatedUpgrade.
    4. Acumula PP pasivas con el acumulador de fracción decimal.
    5. Suma todo al GameState con addPP(BigInteger).

    ===============================================
    Acumulador de fracción (double)
    ===============================================
    pps * delta es casi siempre decimal. El acumulador guarda la parte
    fraccionaria y la suma al siguiente tick para no perder progresión.
    La parte entera se convierte a BigInteger con BigDecimal como
    intermediario, evitando overflow si pps fuera muy grande.

    ===============================================
    Por qué el acumulador sigue siendo double
    ===============================================
    La tasa PP/s se calcula en double (producto de doubles de las
    mejoras). Mantener el acumulador en double es correcto: la fracción
    decimal que se arrastra nunca supera 1.0, así que no hay riesgo
    de overflow en esa variable.
*/
public class LogicThread {

    private static final int TICK_MS = 50; // 20 ticks por segundo

    private final GameState gameState;
    private final ClickHandler clickHandler;

    private volatile boolean running = false;
    private Thread thread;

    private volatile boolean isPaused = false;
    private final Object pauseLock = new Object();

    /** Fracción de PP pasivas acumuladas entre ticks (siempre < 1.0). */
    private double ppAccumulator = 0.0;

    public LogicThread(GameState gameState) {
        this.gameState = gameState;
        this.clickHandler = new ClickHandler();
    }

    // ────────────────────────────────────────────────────────────────────
    // Ciclo de vida
    // ────────────────────────────────────────────────────────────────────

    public void start() {
        if (running)
            return;
        running = true;
        thread = new Thread(this::loop, "LogicThread");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null)
            thread.interrupt();
    }

    // ────────────────────────────────────────────────────────────────────
    // Bucle principal
    // ────────────────────────────────────────────────────────────────────

    private void loop() {
        long lastTick = System.currentTimeMillis();
        while (running) {
            synchronized (pauseLock) {
                while (isPaused) {
                    try {
                        pauseLock.wait(); // El hilo se duerme ahorrando CPU
                        long tiempoAlDespertar = System.currentTimeMillis();
                        double segundosAusente = (tiempoAlDespertar - lastTick) / 1000.0;

                        double ppsActual = calcularPPS();

                        if (ppsActual > 0 && segundosAusente > 0) {
                           
                            java.math.BigDecimal ganancia = java.math.BigDecimal.valueOf(ppsActual).multiply(java.math.BigDecimal.valueOf(segundosAusente));

                            BigInteger gananciaEntera = ganancia.toBigInteger();

                            if (gananciaEntera.compareTo(BigInteger.ZERO) > 0) {
                                gameState.addPP(gananciaEntera);
                                System.out.println("Progreso Offline: ¡El jugador ganó " + gananciaEntera
                                        + " PP mientras la app dormía durante " + segundosAusente + "s!");
                            }
                        }
                        lastTick = tiempoAlDespertar; // Al despertar,actualizamos el reloj interno
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            long now = System.currentTimeMillis();
            double delta = (now - lastTick) / 1000.0;
            lastTick = now;
            tick(delta);
            try {
                Thread.sleep(TICK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Métodos para controlar la pausa
    // ────────────────────────────────────────────────────────────────────

    public void pauseThread() {
        isPaused = true;
    }

    public void resumeThread() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll(); // Despierta al hilo
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Un tick de lógica
    // ────────────────────────────────────────────────────────────────────

    private void tick(double delta) {

        // 1. Clics → ClickHandler devuelve BigInteger directamente
        long clicks = gameState.drainPendingClicks();
        if (clicks > 0) {
            BigInteger ppDeClics = clickHandler.procesarClics(clicks, gameState.getPpPorClick());
            gameState.addPP(ppDeClics);
        }

        // 2. PP/s total de todas las estructuras
        double pps = calcularPPS();
        gameState.setPpPorSegundo(pps);

        // 3. Acumular la contribución pasiva de este tick
        ppAccumulator += pps * delta;

        // 4. Extraer la parte entera del acumulador y convertirla a BigInteger
        // via BigDecimal para no perder precisión si pps fuera muy grande
        long parteEntera = (long) ppAccumulator;
        ppAccumulator -= parteEntera;

        if (parteEntera > 0) {
            gameState.addPP(BigInteger.valueOf(parteEntera));
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Cálculo de PP/s total de las estructuras automatizadas
    // ────────────────────────────────────────────────────────────────────

    private double calcularPPS() {
        Map<String, Upgrade> upgrades = gameState.getUpgrades();
        List<AutomatedUpgrade> automatizadas = UpgradeFactory.getAutomatedUpgrades(upgrades);
        double total = 0.0;
        for (AutomatedUpgrade au : automatizadas) {
            double mult = UpgradeFactory.getMultiplierFor(au.getId(), upgrades);
            total += au.getPPperSecond(mult);
        }
        return total;
    }
}