package com.jovellanos.clicker.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.kotcrab.vis.ui.widget.*;

/*
    ===============================================
    Pausa
    ===============================================
    Pantalla que aparece al pulsar el botón de pausa durante el juego.
    Interrumpe la partida y ofrece tres acciones al jugador.

    ===============================================
    Estructura visual (según mockup)
    ===============================================
    - Título "PAUSA"
    - Botón "Reanudar"      -> vuelve a GameScreen
    - Botón "Ajustes"       -> navega a SettingsScreen
    - Botón "Salir al menú" -> guarda y vuelve a MainMenuScreen

    ===============================================
    Nota sobre el guardado
    ===============================================
    Al pulsar "Salir al menú", MainGame.changeScreen() detecta
    la transición a MAIN_MENU y ejecuta SaveManager.save()
    automáticamente antes de cambiar de pantalla.
*/

public class PauseScreen extends BaseScreen {

    public PauseScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void buildUI() {
        LocaleManager i18n = LocaleManager.getInstance();

        VisLabel titulo = new VisLabel("PAUSA");

        VisTextButton btnReanudar = new VisTextButton("Reanudar");
        VisTextButton btnAjustes  = new VisTextButton(i18n.getText("menu_ajustes"));
        VisTextButton btnSalir    = new VisTextButton("Salir al menú");

        root.add(titulo).padBottom(40).row();
        root.add(btnReanudar).width(300).height(65).padBottom(12).row();
        root.add(btnAjustes).width(300).height(65).padBottom(12).row();
        root.add(btnSalir).width(300).height(65).row();

        btnReanudar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.GAME);
            }
        });

        btnAjustes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.SETTINGS);
            }
        });

        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // MainGame.changeScreen() ejecuta SaveManager.save() antes de cambiar
                game.changeScreen(ScreenType.MAIN_MENU);
            }
        });
    }
}