package com.jovellanos.clicker.logic;

import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.upgrades.AutomatedUpgrade;
import com.jovellanos.clicker.upgrades.Upgrade;
import com.jovellanos.clicker.upgrades.UpgradeFactory;

import java.util.List;
import java.util.Map;

/*
    ===============================================
    LogicThread — Motor de Cálculo
    ===============================================
    Hilo independiente que se ejecuta a 20 ticks por segundo (cada 50 ms).
    El Main Thread nunca hace cálculos de progresión; solo renderiza y
    captura eventos.

    ===============================================
    Lo que hace en cada tick
    ===============================================
    1. Drenar pendingClicks de GameState y convertirlos en PP.
    2. Calcular la tasa PP/s actual de todas las AutomatedUpgrade.
    3. Acumular PP pasivas proporcionales al tiempo transcurrido (delta).
    4. Actualizar GameState con los nuevos valores.

    ===============================================
    Acumulador de fracción
    ===============================================
    pps * delta casi siempre da un decimal (ej: 0.025 PP por tick
    si pps = 0.5 y delta = 0.05s). Truncar a long daría 0 cada tick
    y el jugador nunca vería avance pasivo. El ppAccumulator guarda
    la fracción sobrante y la suma al siguiente tick.

    ===============================================
    Ciclo de vida
    ===============================================
    MainGame.create()   → logicThread.start()
    MainGame.dispose()  → logicThread.stop()
    El hilo es un daemon (no impide que la JVM cierre si el juego termina).
*/

public class LogicThread {

    private static final int TICK_MS = 50; // 20 ticks por segundo

    private final GameState gameState;
    private volatile boolean running = false;
    private Thread thread;

    // Fracción de PP pasivas acumuladas entre ticks
    private double ppAccumulator = 0.0;

    public LogicThread(GameState gameState) {
        this.gameState = gameState;
    }

    // ────────────────────────────────────────────────────────────────────
    // Ciclo de vida
    // ────────────────────────────────────────────────────────────────────

    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this::loop, "LogicThread");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Bucle principal
    // ────────────────────────────────────────────────────────────────────

    private void loop() {
        long lastTick = System.currentTimeMillis();

        while (running) {
            long now   = System.currentTimeMillis();
            double delta = (now - lastTick) / 1000.0; // segundos
            lastTick   = now;

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
    // Un tick de lógica
    // ────────────────────────────────────────────────────────────────────

    private void tick(double delta) {

        // 1. Procesar clics acumulados desde el Main Thread
        long clicks = gameState.drainPendingClicks();
        if (clicks > 0) {
            long ppDeClics = (long) (clicks * gameState.getPpPorClick());
            gameState.addPP(ppDeClics);
        }

        // 2. Calcular PP/s actual con todos los multiplicadores
        double pps = calcularPPS();
        gameState.setPpPorSegundo(pps);

        // 3. El acumulador se encarga de acumular los decimales, ya que al castear a long, lo trunca.
        ppAccumulator += pps * delta;
        long ppPasivas = (long) ppAccumulator;
        ppAccumulator -= ppPasivas;

        if (ppPasivas > 0) {
            gameState.addPP(ppPasivas);
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Cálculo de PP/s
    // ────────────────────────────────────────────────────────────────────

    /**
     * Suma la contribución de todas las AutomatedUpgrade aplicando
     * sus MultiplierUpgrade asociados.
     */
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