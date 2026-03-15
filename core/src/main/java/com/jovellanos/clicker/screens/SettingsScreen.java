package com.jovellanos.clicker.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.kotcrab.vis.ui.widget.*;

/*
    ===============================================
    Ajustes
    ===============================================
    Pantalla de configuración accesible desde el Menú Principal
    y desde la PauseScreen durante la partida.

    ===============================================
    Estructura visual (según mockup)
    ===============================================
    - Título "CONFIGURACIÓN DE SISTEMA"
    - Slider "VOLUMEN DE EFECTOS" con porcentaje (0-100%)
    - Slider "MÚSICA" con porcentaje (0-100%)
    - Botón "Idioma: Español" para ciclar entre idiomas
    - Botón "Volver": regresa a la pantalla anterior

    ===============================================
    Conexiones pendientes
    ===============================================
    - Conectar sliders al sistema de audio
    - El cambio de idioma ya funciona vía LocaleManager,
      pero recargar los textos de la UI queda para la Fase 3.
*/

public class SettingsScreen extends BaseScreen {

    // Guardamos el idioma actual para cambiar entre español e inglés
    private String idiomaActual = "es";

    public SettingsScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void buildUI() {
        LocaleManager i18n = LocaleManager.getInstance();

        // Título
        VisLabel titulo = new VisLabel(i18n.getText("ajustes_titulo"));

        // Slider de volumen de efectos
        VisLabel lblEfectos    = new VisLabel(i18n.getText("ajustes_volumen_efectos"));
        VisLabel lblEfectosPct = new VisLabel("70%");
        VisSlider sliderEfectos = new VisSlider(0, 100, 1, false);
        sliderEfectos.setValue(70);

        // Slider de música
        VisLabel lblMusica    = new VisLabel(i18n.getText("ajustes_musica"));
        VisLabel lblMusicaPct = new VisLabel("50%");
        VisSlider sliderMusica = new VisSlider(0, 100, 1, false);
        sliderMusica.setValue(50);

        // Botón de idioma — muestra el idioma activo
        VisTextButton btnIdioma = new VisTextButton(
            i18n.getTextVar("ajustes_idioma", "Español")
        );

        // Botón volver
        VisTextButton btnVolver = new VisTextButton(i18n.getText("ajustes_volver"));

        // Layout del panel central
        VisTable panel = new VisTable();
        panel.pad(40);

        panel.add(titulo).colspan(2).padBottom(24).row();

        panel.add(lblEfectos).left().expandX();
        panel.add(lblEfectosPct).right().padBottom(8).row();
        panel.add(sliderEfectos).colspan(2).fillX().padBottom(20).row();

        panel.add(lblMusica).left().expandX();
        panel.add(lblMusicaPct).right().padBottom(8).row();
        panel.add(sliderMusica).colspan(2).fillX().padBottom(28).row();

        panel.add(btnIdioma).colspan(2).fillX().height(55).padBottom(12).row();
        panel.add(btnVolver).colspan(2).fillX().height(55).row();

        root.add(panel).width(560);

        // Actualizar porcentaje al mover el slider de efectos
        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
                // Pendiente: conectar con AudioManager cuando se implemente el audio
            }
        });

        // Actualizar porcentaje al mover el slider de música
        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
                // Pendiente: conectar con AudioManager cuando se implemente el audio
            }
        });

        // Cambiar entre español e inglés al pulsar el botón de idioma
        btnIdioma.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Cambiar el idioma activo al contrario del que hay ahora
                if (idiomaActual.equals("es")) {
                    idiomaActual = "en";
                } else {
                    idiomaActual = "es";
                }

                // Obtener el nombre del idioma para mostrar en el botón
                String nombreIdioma;
                if (idiomaActual.equals("es")) {
                    nombreIdioma = "Español";
                } else {
                    nombreIdioma = "English";
                }

                // Cargar el nuevo idioma y actualizar el texto del botón
                LocaleManager.getInstance().loadLanguage(idiomaActual);
                btnIdioma.setText(
                    LocaleManager.getInstance().getTextVar("ajustes_idioma", nombreIdioma)
                );
            }
        });

        btnVolver.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.MAIN_MENU);
            }
        });
    }
}