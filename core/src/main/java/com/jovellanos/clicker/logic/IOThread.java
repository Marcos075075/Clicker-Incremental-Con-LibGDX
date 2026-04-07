package com.jovellanos.clicker.logic;

import com.badlogic.gdx.Gdx;
import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.core.GameStateSnapshot;
import com.jovellanos.clicker.persistence.SaveManager;

/*
    ===============================================
    IOThread — Hilo de persistencia
    ===============================================
    Se ejecuta en segundo plano y guarda la partida
    cada 30 segundos. También puede forzarse un guardado
    inmediato llamando a forceSave().

    Patrón Memento: pide un snapshot al GameState y
    lo pasa al SaveManager. Nunca accede al GameState
    directamente durante la escritura.
*/
public class IOThread extends Thread {

    private static final long SAVE_INTERVAL_MS = 30_000; // 30 segundos

    private final GameState   gameState;
    private final SaveManager saveManager;
    private volatile boolean  running = false;
    private volatile boolean  forceSave = false;

    public IOThread(GameState gameState, SaveManager saveManager) {
        super("IO-Thread");
        this.gameState   = gameState;
        this.saveManager = saveManager;
        setDaemon(true); // se detiene automáticamente al cerrar la app
    }

    // ── Control del hilo ─────────────────────────────────────────────────

    public void startThread() {
        running = true;
        start();
    }

    public void stopThread() {
        running = false;
        // Guardado final al cerrar
        doSave();
        interrupt();
    }

    /** Fuerza un guardado inmediato en el siguiente ciclo. */
    public void forceSave() {
        forceSave = true;
        interrupt(); // despierta el hilo si está dormido
    }

    // ── Bucle principal ──────────────────────────────────────────────────

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(SAVE_INTERVAL_MS);
            } catch (InterruptedException e) {
                // Puede ser por forceSave() o stopThread()
            }

            if (running || forceSave) {
                doSave();
                forceSave = false;
            }
        }
    }

    // ── Guardado ─────────────────────────────────────────────────────────

    private void doSave() {
        try {
            GameStateSnapshot snapshot = gameState.takeSnapshot();
            saveManager.guardar(snapshot);
            Gdx.app.log("IOThread", "Partida guardada correctamente.");
        } catch (Exception e) {
            Gdx.app.error("IOThread", "Error al guardar: " + e.getMessage());
        }
    }
}