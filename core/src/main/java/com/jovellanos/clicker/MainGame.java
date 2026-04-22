package com.jovellanos.clicker;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.jovellanos.clicker.audio.AudioManager;
import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.core.ResourceManager;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.jovellanos.clicker.logic.IOThread;
import com.jovellanos.clicker.logic.LogicThread;
import com.jovellanos.clicker.logic.PurchaseService;
import com.jovellanos.clicker.persistence.SaveManager;
import com.jovellanos.clicker.screens.GameScreen;
import com.jovellanos.clicker.screens.GameScreenAndroid;
import com.jovellanos.clicker.screens.IntroScreen;
import com.jovellanos.clicker.screens.MainMenuScreen;
import com.jovellanos.clicker.screens.SettingsScreen;

/* ===============================================
    Recursos Globales y Gestión Multiplataforma
   ===============================================
    SpriteBatch, se comparte para todas las pantallas. Es el uso recomendado que da LibGDX para el consumo de GPU.
    GameState, estado central de la partida accedido por tres hilos: Main Thread, Logic Thread e IO Thread.
    ScreenType, tipos de pantalla de la aplicación para centralizar su navegación.
    
    Implementación dinámica: 
    El sistema detecta el entorno de ejecución en tiempo real para alternar 
    entre GameScreen (Escritorio) y GameScreenAndroid (Móvil), permitiendo 
    layouts específicos sin duplicar la lógica de estado.

    ===============================================
    Ciclo de vida
    ===============================================
    create():
      1. SpriteBatch compartido.
      2. Carga de recursos (Skin).
      3. AudioManager — inicializa música y efectos de sonido.
      4. SaveManager — comprueba si hay partida guardada.
      5. GameState — inicializa mejoras (UpgradeFactory.build()).
      6. PurchaseService — instanciado una vez y reutilizado.
      7. IOThread y LogicThread — arrancan con los datos correctos.
      8. Primera pantalla: menú principal.

    dispose():
      Para los hilos secundarios antes de liberar recursos.
      Detiene el AudioManager y libera sus assets.

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

        // 1. AudioManager — debe inicializarse tras ResourceManager y antes de las pantallas
        AudioManager.getInstance();

        // 2. SaveManager y carga de datos previos
        saveManager = new SaveManager();
        SaveManager.SaveData datosGuardados = saveManager.carga();

        // 3. GameState (solo estado, sin lógica de negocio de compra)
        gameState = new GameState();

        if (datosGuardados != null) {
            gameState.cargarDesdeSaveData(datosGuardados);
            LocaleManager.getInstance().loadLanguage(
                datosGuardados.idiomaActual != null ? datosGuardados.idiomaActual : "es");
            Gdx.app.log("MainGame", "Partida cargada con éxito.");
        } else {
            LocaleManager.getInstance().loadLanguage("es");
            Gdx.app.log("MainGame", "No hay partida previa. Iniciando nueva partida.");
        }

        // 4. PurchaseService — instancia única que contiene las reglas de compra
        purchaseService = new PurchaseService();

        // 5. Hilos secundarios
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
            case MAIN_MENU: 
                setScreen(new MainMenuScreen(this)); 
                break;
            case GAME:
                if (Gdx.app.getType() == Application.ApplicationType.Android) {
                    setScreen(new GameScreenAndroid(this));
                } else {
                    setScreen(new GameScreen(this));
                }
                break;
            case INTRO: 
                setScreen(new IntroScreen(this)); 
                break;
            case SETTINGS: 
                setScreen(new SettingsScreen(this)); 
                break;
        }
    }

    @Override
    public void dispose() {
        if (logicThread != null) logicThread.stop();
        if (ioThread    != null) ioThread.stopThread();
        AudioManager.getInstance().dispose();
        super.dispose();
        if (batch != null) batch.dispose();
        ResourceManager.dispose();
    }

    //Ciclo de vida de android

    //Si la app se pone en segundo plano, fuerza guardado y pausa la música
    @Override
    public void pause(){
        super.pause();

        if (ioThread != null){
            ioThread.forceSave();
        }
        AudioManager.getInstance().pauseMusic();
    }

    //Si la app vuelve a primer plano, libGDX restaura el contexto
    @Override
    public void resume(){
        super.resume();
        AudioManager.getInstance().resumeMusic();
    }

    public GameState       getGameState()       { return gameState; }
    /** Servicio de compras. GameScreen y cualquier pantalla futura lo usan en lugar de GameState. */
    public PurchaseService getPurchaseService() { return purchaseService; }
}