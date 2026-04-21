package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.audio.AudioManager;
import com.jovellanos.clicker.audio.UISounds;
import com.jovellanos.clicker.core.ResourceManager;
import com.jovellanos.clicker.i18n.LocaleManager;

/*
    ===============================================
    Ajustes (con adaptación para móvil)
    ===============================================
    Pantalla de configuración accesible desde el Menú Principal
    y desde el juego. Cuando se abre desde el juego muestra
    adicionalmente el botón Reanudar para volver a la partida.

    Nota sobre Android: En el entorno móvil, cambia el fondo
    y la estructura. Los textos adoptan mayor escala y se sustituye 
    el selector de resolución por uno de orientación de pantalla
    (Vertical/Horizontal) diseñado para implementaciones futuras.

    ===============================================
    Parámetro desdeJuego
    ===============================================
    Si desdeJuego es true, se muestra el botón Reanudar que
    vuelve a GAME. El botón Volver/Salir al menú siempre
    va a MAIN_MENU independientemente del origen.

    ===============================================
    Persistencia de volumen
    ===============================================
    Los sliders leen el volumen actual desde AudioManager al construir
    la pantalla y escriben de vuelta en tiempo real. AudioManager persiste
    los valores en LibGDX Preferences automáticamente.
*/

public class SettingsScreen extends BaseScreen {

    private String idiomaActual;
    private final boolean desdeJuego;
    private static final String IDIOMA_ES = "Español";
    private static final String IDIOMA_EN = "English";

    // Para la resolución de PC
    private int currentScreenMode = 0;
    
    // Control de orientación móvil (0 vertical, 1 horizontal)
    private int currentOrientationMode = 0;

    public SettingsScreen(MainGame game) {
        super(game);
        this.desdeJuego = false;
    }

    public SettingsScreen(MainGame game, boolean desdeJuego) {
        super(game);
        this.desdeJuego = desdeJuego;
    }

    @Override
    public void show() {
        // Se mantiene la reproducción de la pista de audio actual
        super.show();
    }

    @Override
    protected void buildUI() {
        idiomaActual = game.getGameState().getIdiomaActual();
        
        // Selección de interfaz según el entorno de ejecución
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            buildMobileUI();
        } else {
            buildDesktopUI();
        }
    }

    // Vista clásica para ordenador
    private void buildDesktopUI() {
        final LocaleManager i18n = LocaleManager.getInstance();
        final AudioManager audio = AudioManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        if (Gdx.graphics.isFullscreen()) {
            currentScreenMode = 2; 
        } else {
            currentScreenMode = 0;
        }

        root.setBackground(new TextureRegionDrawable(new TextureRegion(ResourceManager.fondoSettings)));

        final Label titulo = new Label(i18n.getText("ajustes_titulo"), skin);
        Slider.SliderStyle estiloSlider = new Slider.SliderStyle();

        Pixmap bgPix = new Pixmap(1, 6, Pixmap.Format.RGBA8888);
        bgPix.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        bgPix.fill();
        estiloSlider.background = new TextureRegionDrawable(new TextureRegion(new Texture(bgPix)));
        bgPix.dispose();

        Pixmap knobPix = new Pixmap(22, 22, Pixmap.Format.RGBA8888);
        knobPix.setColor(Color.valueOf("1BA1E2"));
        knobPix.fill();
        estiloSlider.knob = new TextureRegionDrawable(new TextureRegion(new Texture(knobPix)));
        knobPix.dispose();

        // Slider de efectos
        final Label lblEfectos = new Label(i18n.getText("ajustes_volumen_efectos"), skin);
        final Slider sliderEfectos = new Slider(0, 100, 1, false, estiloSlider);
        sliderEfectos.setValue(audio.getSfxVolume() * 100f);
        final Label lblEfectosPct = new Label((int) sliderEfectos.getValue() + "%", skin);

        // Slider de música
        final Label lblMusica = new Label(i18n.getText("ajustes_musica"), skin);
        final Slider sliderMusica = new Slider(0, 100, 1, false, estiloSlider);
        sliderMusica.setValue(audio.getMusicVolume() * 100f);
        final Label lblMusicaPct = new Label((int) sliderMusica.getValue() + "%", skin);

        final Label lblIdioma = new Label(i18n.getText("ajustes_idioma_label"), skin);

        SelectBox.SelectBoxStyle estiloSelect = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        pixmap.fill();
        TextureRegionDrawable bgSelect = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));

        bgSelect.setLeftWidth(8f);
        bgSelect.setRightWidth(8f);
        estiloSelect.background = bgSelect;

        if (skin.has("small", Label.LabelStyle.class)) {
            BitmapFont smallFont = skin.get("small", Label.LabelStyle.class).font;
            estiloSelect.font = smallFont;

            if (estiloSelect.listStyle != null) {
                BitmapFont listFont = new BitmapFont(smallFont.getData(), smallFont.getRegion(),
                        smallFont.usesIntegerPositions());
                listFont.getData().setScale(0.85f);
                estiloSelect.listStyle.font = listFont;

                if (estiloSelect.listStyle.selection != null) {
                    estiloSelect.listStyle.selection.setLeftWidth(8f);
                    estiloSelect.listStyle.selection.setRightWidth(8f);
                }
            }
        }

        final SelectBox<String> selectIdioma = new SelectBox<String>(estiloSelect);
        pixmap.dispose();

        selectIdioma.setItems(IDIOMA_ES, IDIOMA_EN);
        if (idiomaActual.equals("es")) {
            selectIdioma.setSelected(IDIOMA_ES);
        } else {
            selectIdioma.setSelected(IDIOMA_EN);
        }

        final Label lblResolucion = new Label(i18n.getText("ajustes_resolucion"), skin);

        TextButton.TextButtonStyle smallBtnStyle = new TextButton.TextButtonStyle(
                skin.get(TextButton.TextButtonStyle.class));
        if (skin.has("small", Label.LabelStyle.class)) {
            smallBtnStyle.font = skin.get("small", Label.LabelStyle.class).font;
        }

        final TextButton btnResLeft = new TextButton("<", smallBtnStyle);
        final TextButton btnResRight = new TextButton(">", smallBtnStyle);
        final Label lblResStatus = new Label("", skin);
        lblResStatus.setAlignment(Align.center);

        if (skin.has("small", Label.LabelStyle.class)) {
            Label.LabelStyle smallLabelStyle = new Label.LabelStyle(skin.get("small", Label.LabelStyle.class));
            lblResStatus.setStyle(smallLabelStyle);
        }

        updateScreenModeLabel(lblResStatus, i18n);

        Table tableResolucionControls = new Table();
        tableResolucionControls.add(btnResLeft).width(40).height(40);
        tableResolucionControls.add(lblResStatus).expandX().fillX();
        tableResolucionControls.add(btnResRight).width(40).height(40);

        final TextButton btnSalir = new TextButton(i18n.getText("pausa_salir_menu"), skin);

        Table panel = new Table();
        panel.pad(40);

        final TextButton btnReanudar = desdeJuego
                ? new TextButton(i18n.getText("pausa_reanudar"), skin)
                : null;

        if (desdeJuego) {
            panel.add(btnReanudar).colspan(2).fillX().height(55).padBottom(20).row();
            btnReanudar.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
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

        panel.add(lblResolucion).left().padBottom(8).row();
        panel.add(tableResolucionControls).colspan(2).fillX().height(55).padBottom(20).row();

        panel.add(btnSalir).colspan(2).fillX().height(55).row();

        root.add(panel).width(560);

        // Listeners
        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderEfectos.getValue() / 100f;
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
                audio.setSfxVolume(vol); 
            }
        });

        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderMusica.getValue() / 100f;
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
                audio.setMusicVolume(vol); 
            }
        });

        selectIdioma.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectIdioma.getSelected().equals(IDIOMA_ES)) {
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
                lblResolucion.setText(i18n.getText("ajustes_resolucion"));

                updateScreenModeLabel(lblResStatus, i18n);

                btnSalir.setText(i18n.getText("pausa_salir_menu"));
                if (btnReanudar != null) {
                    btnReanudar.setText(i18n.getText("pausa_reanudar"));
                }
            }
        });

        btnResLeft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentScreenMode--;
                if (currentScreenMode < 0) currentScreenMode = 2;
                applyScreenMode(lblResStatus, i18n);
            }
        });

        btnResRight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentScreenMode++;
                if (currentScreenMode > 2) currentScreenMode = 0;
                applyScreenMode(lblResStatus, i18n);
            }
        });

        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.MAIN_MENU);
            }
        });

        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE && desdeJuego) {
                    game.changeScreen(ScreenType.GAME);
                    return true;
                }
                return false;
            }
        });

        if (desdeJuego) {
            btnReanudar.addListener(UISounds.HOVER);
            btnReanudar.addListener(UISounds.CLICK);
        }

        btnResLeft.addListener(UISounds.HOVER);
        btnResRight.addListener(UISounds.HOVER);
        btnSalir.addListener(UISounds.HOVER);
        btnResLeft.addListener(UISounds.CLICK);
        btnResRight.addListener(UISounds.CLICK);
        btnSalir.addListener(UISounds.CLICK);
    }

    // Vista táctil para terminales móviles
    private void buildMobileUI() {
        final LocaleManager i18n = LocaleManager.getInstance();
        final AudioManager audio = AudioManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        // Asignación de fondo con borde de diseño específico
        root.setBackground(new TextureRegionDrawable(new TextureRegion(ResourceManager.FondoSettingsAndroid)));

        Table panel = new Table();
        
        // Reducción de padding interno para aprovechar ancho disponible
        panel.pad(60);

        // Ajuste de escala para evitar desbordamiento de texto en traducciones largas
        final Label titulo = new Label(i18n.getText("ajustes_titulo"), skin, "large");
        titulo.setFontScale(1.5f);

        Slider.SliderStyle estiloSlider = new Slider.SliderStyle();
        Pixmap bgPix = new Pixmap(1, 15, Pixmap.Format.RGBA8888);
        bgPix.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        bgPix.fill();
        estiloSlider.background = new TextureRegionDrawable(new TextureRegion(new Texture(bgPix)));
        bgPix.dispose();

        Pixmap knobPix = new Pixmap(60, 60, Pixmap.Format.RGBA8888);
        knobPix.setColor(Color.valueOf("1BA1E2"));
        knobPix.fill();
        estiloSlider.knob = new TextureRegionDrawable(new TextureRegion(new Texture(knobPix)));
        knobPix.dispose();

        // Efectos
        final Label lblEfectos = new Label(i18n.getText("ajustes_volumen_efectos"), skin);
        lblEfectos.setFontScale(1.3f);
        final Slider sliderEfectos = new Slider(0, 100, 1, false, estiloSlider);
        sliderEfectos.setValue(audio.getSfxVolume() * 100f);
        final Label lblEfectosPct = new Label((int) sliderEfectos.getValue() + "%", skin);
        lblEfectosPct.setFontScale(1.3f);

        // Música
        final Label lblMusica = new Label(i18n.getText("ajustes_musica"), skin);
        lblMusica.setFontScale(1.3f);
        final Slider sliderMusica = new Slider(0, 100, 1, false, estiloSlider);
        sliderMusica.setValue(audio.getMusicVolume() * 100f);
        final Label lblMusicaPct = new Label((int) sliderMusica.getValue() + "%", skin);
        lblMusicaPct.setFontScale(1.3f);

        // Selección de idioma
        final Label lblIdioma = new Label(i18n.getText("ajustes_idioma_label"), skin);
        lblIdioma.setFontScale(1.3f);

        SelectBox.SelectBoxStyle estiloSelect = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        pixmap.fill();
        TextureRegionDrawable bgSelect = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        bgSelect.setLeftWidth(30f);
        bgSelect.setRightWidth(30f);
        estiloSelect.background = bgSelect;
        
        if (skin.has("large", Label.LabelStyle.class)) {
            estiloSelect.font = skin.get("large", Label.LabelStyle.class).font;
        }

        if (estiloSelect.listStyle != null) {
            Pixmap pixListBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixListBg.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
            pixListBg.fill();
            estiloSelect.listStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(pixListBg)));
            pixListBg.dispose();

            Pixmap pixSel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixSel.setColor(new Color(0.3f, 0.3f, 0.5f, 1f));
            pixSel.fill();
            TextureRegionDrawable selectionBg = new TextureRegionDrawable(new TextureRegion(new Texture(pixSel)));
            selectionBg.setTopHeight(25f);
            selectionBg.setBottomHeight(25f);
            selectionBg.setLeftWidth(30f);
            selectionBg.setRightWidth(30f);
            estiloSelect.listStyle.selection = selectionBg;
            
            if (skin.has("large", Label.LabelStyle.class)) {
                estiloSelect.listStyle.font = skin.get("large", Label.LabelStyle.class).font;
            }
        }

        final SelectBox<String> selectIdioma = new SelectBox<String>(estiloSelect);
        pixmap.dispose();
        selectIdioma.setItems(IDIOMA_ES, IDIOMA_EN);
        selectIdioma.setSelected(idiomaActual.equals("es") ? IDIOMA_ES : IDIOMA_EN);

        // Control de orientación
        final Label lblOrientacion = new Label("Orientación", skin);
        lblOrientacion.setFontScale(1.3f);

        TextButton.TextButtonStyle btnStyle = skin.get(TextButton.TextButtonStyle.class);
        final TextButton btnOrientLeft = new TextButton("<", btnStyle);
        final TextButton btnOrientRight = new TextButton(">", btnStyle);
        btnOrientLeft.getLabel().setFontScale(1.3f);
        btnOrientRight.getLabel().setFontScale(1.3f);

        final Label lblOrientStatus = new Label("Vertical", skin);
        lblOrientStatus.setFontScale(1.3f);
        lblOrientStatus.setAlignment(Align.center);

        Table tableOrientControls = new Table();
        tableOrientControls.add(btnOrientLeft).width(100).height(100);
        tableOrientControls.add(lblOrientStatus).expandX().fillX();
        tableOrientControls.add(btnOrientRight).width(100).height(100);

        final TextButton btnSalir = new TextButton(i18n.getText("pausa_salir_menu"), skin);
        btnSalir.getLabel().setFontScale(1.3f);

        final TextButton btnReanudar = desdeJuego ? new TextButton(i18n.getText("pausa_reanudar"), skin) : null;
        if (desdeJuego) {
            btnReanudar.getLabel().setFontScale(1.3f);
            panel.add(btnReanudar).colspan(2).fillX().height(120).padBottom(40).row();
            btnReanudar.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.changeScreen(ScreenType.GAME);
                }
            });
        }

        panel.add(titulo).colspan(2).padBottom(60).row();

        panel.add(lblEfectos).left().expandX();
        panel.add(lblEfectosPct).right().padBottom(15).row();
        panel.add(sliderEfectos).colspan(2).fillX().height(60).padBottom(40).row();

        panel.add(lblMusica).left().expandX();
        panel.add(lblMusicaPct).right().padBottom(15).row();
        panel.add(sliderMusica).colspan(2).fillX().height(60).padBottom(40).row();

        panel.add(lblIdioma).left().padBottom(15).row();
        panel.add(selectIdioma).colspan(2).fillX().height(100).padBottom(40).row();

        panel.add(lblOrientacion).left().padBottom(15).row();
        panel.add(tableOrientControls).colspan(2).fillX().height(100).padBottom(60).row();

        panel.add(btnSalir).colspan(2).fillX().height(120).row();

        // Ancho dinámico para prevenir el desbordamiento independientemente de la resolución
        root.add(panel).expand().fillX().padLeft(90).padRight(90);

        // Eventos
        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderEfectos.getValue() / 100f;
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
                audio.setSfxVolume(vol); 
            }
        });

        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderMusica.getValue() / 100f;
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
                audio.setMusicVolume(vol); 
            }
        });

        selectIdioma.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                idiomaActual = selectIdioma.getSelected().equals(IDIOMA_ES) ? "es" : "en";
                game.getGameState().setIdiomaActual(idiomaActual);
                i18n.loadLanguage(idiomaActual);

                titulo.setText(i18n.getText("ajustes_titulo"));
                lblEfectos.setText(i18n.getText("ajustes_volumen_efectos"));
                lblMusica.setText(i18n.getText("ajustes_musica"));
                lblIdioma.setText(i18n.getText("ajustes_idioma_label"));
                
                btnSalir.setText(i18n.getText("pausa_salir_menu"));
                if (btnReanudar != null) {
                    btnReanudar.setText(i18n.getText("pausa_reanudar"));
                }
            }
        });

        // Alternancia visual del texto de orientación (lógica técnica pendiente de implementación)
        ChangeListener orientListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentOrientationMode = (currentOrientationMode == 0) ? 1 : 0;
                lblOrientStatus.setText(currentOrientationMode == 0 ? "Vertical" : "Horizontal");
            }
        };
        btnOrientLeft.addListener(orientListener);
        btnOrientRight.addListener(orientListener);

        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.MAIN_MENU);
            }
        });

        if (desdeJuego) {
            btnReanudar.addListener(UISounds.CLICK);
        }
        btnOrientLeft.addListener(UISounds.CLICK);
        btnOrientRight.addListener(UISounds.CLICK);
        btnSalir.addListener(UISounds.CLICK);
    }

    private void updateScreenModeLabel(Label lblResStatus, LocaleManager i18n) {
        if (currentScreenMode == 0) {
            lblResStatus.setText(i18n.getText("resolucion_ventana"));
        } else if (currentScreenMode == 1) {
            lblResStatus.setText(i18n.getText("resolucion_ventana_sin_bordes"));
        } else {
            lblResStatus.setText(i18n.getText("resolucion_pantalla_completa"));
        }
    }

    private void applyScreenMode(Label lblResStatus, LocaleManager i18n) {
        if (currentScreenMode == 0) {
            Gdx.graphics.setUndecorated(false);
            Gdx.graphics.setWindowedMode(1280, 720);
        } else if (currentScreenMode == 1) {
            Gdx.graphics.setUndecorated(true);
            DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
        } else {
            Gdx.graphics.setUndecorated(false);
            DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(currentMode);
        }
        updateScreenModeLabel(lblResStatus, i18n);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}