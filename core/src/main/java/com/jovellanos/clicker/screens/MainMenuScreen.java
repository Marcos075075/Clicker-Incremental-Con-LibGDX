package com.jovellanos.clicker.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.kotcrab.vis.ui.widget.*;

/*
    ===============================================
    Menú Principal
    ===============================================
    Primera pantalla que ve el usuario al iniciar la aplicación.
    Contiene la identidad visual del juego y los accesos principales.

    ===============================================
    Estructura visual (según el mockup)
    ===============================================
    - Título grande:    "ATLAS M.O.N.O."
    - Subtítulo:        "SISTEMA DE RECOLECCIÓN AUTOMÁTICA"
    - Botón "Nueva Partida"   -> navega a IntroScreen
    - Botón "Cargar Partida"  -> pendiente de implementar
    - Botón "Ajustes"         -> navega a SettingsScreen

    ===============================================
    i18n
    ===============================================
    Todos los textos se obtienen del LocaleManager para soportar
    el cambio de idioma dinámico que implementamos.
*/

public class MainMenuScreen extends BaseScreen {

    public MainMenuScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void buildUI() {
        LocaleManager i18n = LocaleManager.getInstance();

        // Título y subtítulo
        VisLabel titulo    = new VisLabel(i18n.getText("app_titulo"));
        VisLabel subtitulo = new VisLabel(i18n.getText("app_version"));

        // Botones principales
        VisTextButton btnNueva   = new VisTextButton(i18n.getText("menu_nueva_partida"));
        VisTextButton btnCargar  = new VisTextButton(i18n.getText("menu_cargar_partida"));
        VisTextButton btnAjustes = new VisTextButton(i18n.getText("menu_ajustes"));

        // Layout centrado verticalmente
        root.add(titulo).padBottom(8).row();
        root.add(subtitulo).padBottom(80).row();
        root.add(btnNueva).width(380).height(65).padBottom(16).row();
        root.add(btnCargar).width(380).height(65).padBottom(16).row();
        root.add(btnAjustes).width(380).height(65).row();

        // Listeners
        btnNueva.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getGameState().reset();
                game.changeScreen(ScreenType.GAME);
            }
        });

        btnCargar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Hay que implementar la carga del juego.
                game.changeScreen(ScreenType.GAME);
            }
        });

        btnAjustes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.SETTINGS);
            }
        });
    }
}