package com.jovellanos.clicker;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.core.ResourceManager;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.jovellanos.clicker.logic.IOThread;
import com.jovellanos.clicker.logic.LogicThread;
import com.jovellanos.clicker.logic.PurchaseService;
import com.jovellanos.clicker.persistence.SaveManager;
import com.jovellanos.clicker.screens.GameScreen;
import com.jovellanos.clicker.screens.IntroScreen;
import com.jovellanos.clicker.screens.MainMenuScreen;
import com.jovellanos.clicker.screens.SettingsScreen;

/* ===============================================
    Recursos Globales
    ===============================================
    SpriteBatch, se comparte para todas las pantallas. Es el uso recomendado que da LibGDX para el consumo de GPU.
    GameState, estado central de la partida accedido por tres hilos: Main Thread, Logic Thread e IO Thread.
    ScreenType, tipos de pantalla de la aplicación para centralizar su navegación y evitar instancias dispersadas.
        MAIN_MENU, menú principal con opciones Nueva Partida, Cargar, Ajustes y Salir.
        GAM<E, pantalla principal de juego con las 3 columnas.
        PAUSE, superposición de pausa sobre el juego.
        INTRO, introducción narrativa al iniciar una nueva partida.
        SETTINGS, pantalla de ajustes accesible desde el menú principal y desde la pausa.
    
    ===============================================
    Ciclo de vida
    ===============================================
    create():
      1. SpriteBatch compartido.
      2. Carga de recursos (Skin).
      3. SaveManager — comprueba si hay partida guardada.
      4. GameState — inicializa mejoras (UpgradeFactory.build()).
      5. PurchaseService — instanciado una vez y reutilizado.
      6. IOThread y LogicThread — arrancan con los datos correctos.
      7. Primera pantalla: menú principal.

    dispose():
      Para los hilos secundarios antes de liberar recursos.

    ===============================================
    Cambio de pantalla
    ===============================================
    changeScreen() es el único punto de navegación centralizado.
    Fuerza un guardado al volver al menú principal.
*/

public class MainGame extends Game {

    private SpriteBatch     batch;
    private GameState       gameState;
    private LogicThread     logicThread;
    private SaveManager     saveManager;
    private IOThread        ioThread;
    /** Servicio de compras compartido por todas las pantallas que lo necesiten. */
    private PurchaseService purchaseService;

    public enum ScreenType {
        MAIN_MENU,
        GAME,
        INTRO,
        SETTINGS
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        ResourceManager.load();

        // 1. SaveManager y carga de datos previos
        saveManager = new SaveManager();
        SaveManager.SaveData datosGuardados = saveManager.carga();

        // 2. GameState (solo estado, sin lógica de negocio de compra)
        gameState = new GameState();

        if (datosGuardados != null) {
            gameState.cargarDesdeSaveData(datosGuardados);
            LocaleManager.getInstance().loadLanguage(
                datosGuardados.idiomaActual != null ? datosGuardados.idiomaActual : "es");
            Gdx.app.log("MainGame", "Partida cargada con éxito.");
        } else {
            LocaleManager.getInstance().loadLanguage("es");
            Gdx.app.log("MainGame", "No hay partida previa. Iniciando nueva partida."); 
            //TODO: Aqui entra si le das a cargar partida sin que haya una creada
        }

        // 3. PurchaseService — instancia única que contiene las reglas de compra
        purchaseService = new PurchaseService();

        // 4. Hilos secundarios
        ioThread = new IOThread(gameState, saveManager);
        ioThread.startThread();

        logicThread = new LogicThread(gameState);
        logicThread.start();

        setScreen(new MainMenuScreen(this));
    }

    public void changeScreen(ScreenType type) {
        if (type == ScreenType.MAIN_MENU && ioThread != null) {
            ioThread.forceSave();
        }

        switch (type) {
            case MAIN_MENU: setScreen(new MainMenuScreen(this)); break;
            case GAME:      setScreen(new GameScreen(this));     break;
            case INTRO:     setScreen(new IntroScreen(this));    break;
            case SETTINGS:  setScreen(new SettingsScreen(this)); break;
        }
    }

    @Override
    public void dispose() {
        if (logicThread != null) logicThread.stop();
        if (ioThread    != null) ioThread.stopThread();
        super.dispose();
        if (batch != null) batch.dispose();
        ResourceManager.dispose();
    }

    public GameState       getGameState()       { return gameState; }
    /** Servicio de compras. GameScreen y cualquier pantalla futura lo usan en lugar de GameState. */
    public PurchaseService getPurchaseService() { return purchaseService; }
}