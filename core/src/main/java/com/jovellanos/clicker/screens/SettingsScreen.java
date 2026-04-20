package com.jovellanos.clicker.screens;

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
    - Botón "Reanudar"            -> solo visible si desdeJuego = true → va a GAME
    - Título "CONFIGURACIÓN DE SISTEMA"
    - Slider "VOLUMEN DE EFECTOS" → conectado a AudioManager.setSfxVolume()
    - Slider "MÚSICA"             → conectado a AudioManager.setMusicVolume()
    - SelectBox de idioma
    - Alternador de Modo de Pantalla
    - Botón "Salir al menú"      -> siempre va a MAIN_MENU

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

    // Estado interno del modo de pantalla (0: Ventana, 1: Sin Bordes, 2: Completa)
    private int currentScreenMode = 0;

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
        // SettingsScreen no cambia la música; la que suena continúa
        super.show();
    }

    @Override
    protected void buildUI() {
        idiomaActual = game.getGameState().getIdiomaActual();
        final LocaleManager i18n = LocaleManager.getInstance();
        final AudioManager audio = AudioManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        // Determinar el estado inicial basándose en la configuración actual de
        // Gdx.graphics
        if (Gdx.graphics.isFullscreen()) {
            currentScreenMode = 2; // Asume pantalla completa estándar si es fullscreen
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

        // ── Slider efectos ──────────────────────────────────────────────
        final Label lblEfectos = new Label(i18n.getText("ajustes_volumen_efectos"), skin);
        final Slider sliderEfectos = new Slider(0, 100, 1, false, estiloSlider);
        // Lee el volumen actual desde AudioManager para que coincida con lo guardado
        sliderEfectos.setValue(audio.getSfxVolume() * 100f);
        final Label lblEfectosPct = new Label((int) sliderEfectos.getValue() + "%", skin);

        // ── Slider música ───────────────────────────────────────────────
        final Label lblMusica = new Label(i18n.getText("ajustes_musica"), skin);
        final Slider sliderMusica = new Slider(0, 100, 1, false, estiloSlider);
        // Lee el volumen actual desde AudioManager para que coincida con lo guardado
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

        // ── Listeners de volumen (conectados al AudioManager) ───────────
        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderEfectos.getValue() / 100f;
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
                audio.setSfxVolume(vol); // ← Conectado al AudioManager
            }
        });

        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderMusica.getValue() / 100f;
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
                audio.setMusicVolume(vol); // ← Conectado al AudioManager
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
                if (currentScreenMode < 0)
                    currentScreenMode = 2;
                applyScreenMode(lblResStatus, i18n);
            }
        });

        btnResRight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentScreenMode++;
                if (currentScreenMode > 2)
                    currentScreenMode = 0;
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