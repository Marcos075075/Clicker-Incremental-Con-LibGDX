package com.jovellanos.clicker.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.kotcrab.vis.ui.widget.*;

/*
    ===============================================
    Pausa / Configuración en juego
    ===============================================
    Pantalla que aparece al pulsar "Configuración" durante el juego.
    Combina las opciones de pausa y acceso a ajustes en un solo sitio.

    ===============================================
    Estructura visual
    ===============================================
    - Botón "Reanudar"      -> vuelve a GameScreen
    - Botón "Ajustes"       -> navega a SettingsScreen pasando PAUSE
                              como origen para que Volver regrese aquí
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

        VisTextButton btnReanudar = new VisTextButton(i18n.getText("pausa_reanudar"));
        VisTextButton btnAjustes  = new VisTextButton(i18n.getText("menu_ajustes"));
        VisTextButton btnSalir    = new VisTextButton(i18n.getText("pausa_salir_menu"));

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
                // Se pasa PAUSE como origen para que Volver regrese aquí
                game.setScreen(new SettingsScreen(game, ScreenType.PAUSE));
            }
        });

        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.MAIN_MENU);
            }
        });
    }
}