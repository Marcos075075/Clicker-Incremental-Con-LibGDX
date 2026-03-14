package com.jovellanos.clicker;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.jovellanos.clicker.persistence.SaveManager;
import com.jovellanos.clicker.screens.GameScreen;
import com.jovellanos.clicker.screens.IntroScreen;
import com.jovellanos.clicker.screens.MainMenuScreen;
import com.jovellanos.clicker.screens.PauseScreen;
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
    
    ===============================================
    Ciclo de vida
    ===============================================
    Inicio de los sistemas en el orden que corresponde:
    - SpriteBach compartido.
    - VisuUI que carga el skin.
    - Internacionalización.
    - GameState, crea el estado inicial de la partida que por defecto es nueva hasta que se seleccione cargar partida.
    - La primera pantalla: el menú principal.

    ===============================================
    Cambio de pantalla
    ===============================================
    Se centraliza aquí el cambio de pantalla, permitiendo así:
    - Evitar que las pantallas se referencien entre sí.
    - Añadimos lógica transversal en un único lugar. (Ej: guardado antes de salir). Nos referimos a lógica transversal
    a aquel código que se debe ejecutar siempre que ocurre algo.
*/

public class MainGame extends Game {
    private SpriteBatch batch;
    private GameState gameState;
    public enum ScreenType {
        MAIN_MENU,
        GAME,
        PAUSE,
        INTRO
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        VisUI.load();
        LocaleManager.getInstance().loadLanguage("es");
        gameState = new GameState();
        setScreen(new MainMenuScreen(this));
    }

    public void changeScreen(ScreenType type) {
        if (type == ScreenType.MAIN_MENU) {
            SaveManager.save(gameState);
        }

        switch (type) {
        case MAIN_MENU: setScreen(new MainMenuScreen(this)); break;
        case GAME:      setScreen(new GameScreen(this));     break;
        case PAUSE:     setScreen(new PauseScreen(this));    break;
        case INTRO:     setScreen(new IntroScreen(this));    break;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null)    batch.dispose();
        if (VisUI.isLoaded()) VisUI.dispose();
    }
}
