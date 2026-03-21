package com.jovellanos.clicker;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.jovellanos.clicker.logic.LogicThread;
import com.jovellanos.clicker.persistence.SaveManager;
import com.jovellanos.clicker.screens.GameScreen;
import com.jovellanos.clicker.screens.IntroScreen;
import com.jovellanos.clicker.screens.MainMenuScreen;
import com.jovellanos.clicker.screens.SettingsScreen;
import com.kotcrab.vis.ui.VisUI;

/* 
    ===============================================
    Recursos Globales 
    ===============================================
    SpriteBatch, se comparte para todas las pantallas. Es el uso recomendado que da LibGDX para el consumo de GPU.
    GameState, estado central de la partida accedido por tres hilos: Main Thread, Logic Thread e IO Thread.
    ScreenType, tipos de pantalla de la aplicación para centralizar su navegación y evitar instancias dispersadas.
        MAIN_MENU, menú principal con opciones Nueva Partida, Cargar, Ajustes y Salir.
        GAME, pantalla principal de juego con las 3 columnas.
        PAUSE, superposición de pausa sobre el juego.
        INTRO, introducción narrativa al iniciar una nueva partida.
        SETTINGS, pantalla de ajustes accesible desde el menú principal y desde la pausa.
    
    ===============================================
    Ciclo de vida
    ===============================================
    create():
      1. SpriteBatch compartido.
      2. VisUI carga el skin.
      3. Internacionalización.
      4. GameState inicializa las mejoras (UpgradeFactory.build()).
      5. LogicThread arranca — ya puede recibir clics y calcular PP.
      6. Primera pantalla: menú principal.
 
    dispose():
      Se para el LogicThread antes de liberar recursos para evitar
      que el hilo intente acceder a objetos ya destruidos.

    ===============================================
    Cambio de pantalla
    ===============================================
    changeScreen() es el único punto de navegación. Evita que las
    pantallas se referencien entre sí y centraliza lógica transversal
    (ej: guardado automático al volver al menú).
 
    GameScreen es la única pantalla que recibe GameState porque es
    la única que necesita leer el estado de la partida en tiempo real.
*/

public class MainGame extends Game {
    private SpriteBatch batch;
    private GameState gameState;
    private LogicThread logicThread;
    public enum ScreenType {
        MAIN_MENU,
        GAME,
        INTRO,
        SETTINGS
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        VisUI.load();
        LocaleManager.getInstance().loadLanguage("es");
        gameState = new GameState();

        logicThread = new LogicThread(gameState);
        logicThread.start();

        setScreen(new MainMenuScreen(this));
    }

    public void changeScreen(ScreenType type) {
        if (type == ScreenType.MAIN_MENU) {
            //SaveManager.save(gameState);
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

        super.dispose();
        if (batch != null)    batch.dispose();
        if (VisUI.isLoaded()) VisUI.dispose();
    }

    public GameState getGameState() { return gameState; }
}
