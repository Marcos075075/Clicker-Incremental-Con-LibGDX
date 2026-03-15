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
    y desde el juego. Cuando se abre desde el juego muestra
    adicionalmente el botón Reanudar para volver a la partida.

    ===============================================
    Parámetro desdeJuego
    ===============================================
    Si desdeJuego es true, se muestra el botón Reanudar que
    vuelve a GAME. El botón Volver/Salir al menú siempre
    va a MAIN_MENU independientemente del origen.

    ===============================================
    Estructura visual
    ===============================================
    - Botón "Reanudar"           -> solo visible si desdeJuego = true → va a GAME
    - Título "CONFIGURACIÓN DE SISTEMA"
    - Slider "VOLUMEN DE EFECTOS" con porcentaje (0-100%)
    - Slider "MÚSICA" con porcentaje (0-100%)
    - SelectBox de idioma
    - Botón "Salir al menú"      -> siempre va a MAIN_MENU

    ===============================================
    Conexiones pendientes
    ===============================================
    - Conectar sliders al sistema de audio (Fase 4)
*/

public class SettingsScreen extends BaseScreen {

    private String idiomaActual;
    // Indica si se abrió desde el juego para mostrar el botón Reanudar
    private final boolean desdeJuego;

    // Constructor desde el menú principal
    public SettingsScreen(MainGame game) {
        super(game);
        this.desdeJuego = false;
    }

    // Constructor desde el juego, muestra botón Reanudar
    public SettingsScreen(MainGame game, boolean desdeJuego) {
        super(game);
        this.desdeJuego = desdeJuego;
    }

    @Override
    protected void buildUI() {
        idiomaActual = game.getGameState().getIdiomaActual();
        LocaleManager i18n = LocaleManager.getInstance();

        VisLabel titulo = new VisLabel(i18n.getText("ajustes_titulo"));

        VisLabel lblEfectos     = new VisLabel(i18n.getText("ajustes_volumen_efectos"));
        VisLabel lblEfectosPct  = new VisLabel("70%");
        VisSlider sliderEfectos = new VisSlider(0, 100, 1, false);
        sliderEfectos.setValue(70);

        VisLabel lblMusica    = new VisLabel(i18n.getText("ajustes_musica"));
        VisLabel lblMusicaPct = new VisLabel("50%");
        VisSlider sliderMusica = new VisSlider(0, 100, 1, false);
        sliderMusica.setValue(50);

        VisLabel lblIdioma = new VisLabel(i18n.getText("ajustes_idioma_label"));
        VisSelectBox<String> selectIdioma = new VisSelectBox<>();
        selectIdioma.setItems("Español", "English");
        if (idiomaActual.equals("es")) {
            selectIdioma.setSelected("Español");
        } else {
            selectIdioma.setSelected("English");
        }

        // Volver siempre va al menú principal
        VisTextButton btnSalir = new VisTextButton(i18n.getText("pausa_salir_menu"));

        VisTable panel = new VisTable();
        panel.pad(40);

        // Botón Reanudar, solo si se abre desde el juego
        if (desdeJuego) {
            VisTextButton btnReanudar = new VisTextButton(i18n.getText("pausa_reanudar"));
            panel.add(btnReanudar).colspan(2).fillX().height(55).padBottom(20).row();

            btnReanudar.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Reanudar vuelve al juego
                    game.changeScreen(ScreenType.GAME);
                }
            });
        }

        panel.add(titulo).colspan(2).padBottom(24).row();

        panel.add(lblEfectos).left().expandX();
        panel.add(lblEfectosPct).right().padBottom(8).row();
        panel.add(sliderEfectos).colspan(2).fillX().padBottom(20).row();

        panel.add(lblMusica).left().expandX();
        panel.add(lblMusicaPct).right().padBottom(8).row();
        panel.add(sliderMusica).colspan(2).fillX().padBottom(20).row();

        panel.add(lblIdioma).left().padBottom(8).row();
        panel.add(selectIdioma).colspan(2).fillX().height(55).padBottom(20).row();

        panel.add(btnSalir).colspan(2).fillX().height(55).row();

        root.add(panel).width(560);

        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
                // Pendiente: conectar con AudioManager (Fase 4)
            }
        });

        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
                // Pendiente: conectar con AudioManager (Fase 4)
            }
        });

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

                titulo.setText(i18n.getText("ajustes_titulo"));
                lblEfectos.setText(i18n.getText("ajustes_volumen_efectos"));
                lblMusica.setText(i18n.getText("ajustes_musica"));
                lblIdioma.setText(i18n.getText("ajustes_idioma_label"));
                btnSalir.setText(i18n.getText("pausa_salir_menu"));
            }
        });

        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Salir al menú siempre va a MAIN_MENU
                game.changeScreen(ScreenType.MAIN_MENU);
            }
        });
    }
}