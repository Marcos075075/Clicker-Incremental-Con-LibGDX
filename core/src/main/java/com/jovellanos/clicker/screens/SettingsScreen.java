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

    Recibe el ScreenType de origen para que el botón Volver
    regrese siempre a la pantalla correcta:
    - Desde el menú principal -> vuelve a MAIN_MENU
    - Desde la pausa          -> vuelve a PAUSE

    ===============================================
    Estructura visual (según mockup)
    ===============================================
    - Título "CONFIGURACIÓN DE SISTEMA"
    - Slider "VOLUMEN DE EFECTOS" con porcentaje (0-100%)
    - Slider "MÚSICA" con porcentaje (0-100%)
    - SelectBox de idioma con las opciones disponibles
    - Botón "Volver" -> regresa a la pantalla de origen

    ===============================================
    Conexiones pendientes
    ===============================================
    - Conectar sliders al sistema de audio (Fase 4)
*/

public class SettingsScreen extends BaseScreen {

    private String idiomaActual;
    // Pantalla desde la que se abrió este menú
    private final ScreenType pantallaOrigen;

    // Constructor desde el menú principal, siempre vuelve a MAIN_MENU
    public SettingsScreen(MainGame game) {
        super(game);
        this.pantallaOrigen = ScreenType.MAIN_MENU;
    }

    // Constructor desde la pausa, vuelve a donde se indicó
    public SettingsScreen(MainGame game, ScreenType origen) {
        super(game);
        this.pantallaOrigen = origen;
    }

    @Override
    protected void buildUI() {
        idiomaActual = game.getGameState().getIdiomaActual();
        LocaleManager i18n = LocaleManager.getInstance();

        VisLabel titulo = new VisLabel(i18n.getText("ajustes_titulo"));

        // Slider de volumen de efectos
        VisLabel lblEfectos     = new VisLabel(i18n.getText("ajustes_volumen_efectos"));
        VisLabel lblEfectosPct  = new VisLabel("70%");
        VisSlider sliderEfectos = new VisSlider(0, 100, 1, false);
        sliderEfectos.setValue(70);

        // Slider de música
        VisLabel lblMusica    = new VisLabel(i18n.getText("ajustes_musica"));
        VisLabel lblMusicaPct = new VisLabel("50%");
        VisSlider sliderMusica = new VisSlider(0, 100, 1, false);
        sliderMusica.setValue(50);

        // Etiqueta del idioma
        VisLabel lblIdioma = new VisLabel(i18n.getText("ajustes_idioma_label"));

        // Desplegable de idioma
        VisSelectBox<String> selectIdioma = new VisSelectBox<>();
        selectIdioma.setItems("Español", "English");
        // Seleccionar el idioma activo al abrir la pantalla
        if (idiomaActual.equals("es")) {
            selectIdioma.setSelected("Español");
        } else {
            selectIdioma.setSelected("English");
        }

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
        panel.add(sliderMusica).colspan(2).fillX().padBottom(20).row();

        panel.add(lblIdioma).left().padBottom(8).row();
        panel.add(selectIdioma).colspan(2).fillX().height(55).padBottom(20).row();

        panel.add(btnVolver).colspan(2).fillX().height(55).row();

        root.add(panel).width(560);

        // Actualizar porcentaje al mover el slider de efectos
        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
                // Pendiente: conectar con AudioManager (Fase 4)
            }
        });

        // Actualizar porcentaje al mover el slider de música
        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
                // Pendiente: conectar con AudioManager (Fase 4)
            }
        });

        // Cambiar idioma al seleccionar del desplegable
        selectIdioma.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectIdioma.getSelected().equals("Español")) {
                    idiomaActual = "es";
                } else {
                    idiomaActual = "en";
                }
                game.getGameState().setIdiomaActual(idiomaActual);
                i18n.loadLanguage(idiomaActual);

                // Actualizar textos de la pantalla al instante
                titulo.setText(i18n.getText("ajustes_titulo"));
                lblEfectos.setText(i18n.getText("ajustes_volumen_efectos"));
                lblMusica.setText(i18n.getText("ajustes_musica"));
                lblIdioma.setText(i18n.getText("ajustes_idioma_label"));
                btnVolver.setText(i18n.getText("ajustes_volver"));
            }
        });

        btnVolver.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Vuelve a donde se abrió, menú principal o pausa
                game.changeScreen(pantallaOrigen);
            }
        });
    }
}