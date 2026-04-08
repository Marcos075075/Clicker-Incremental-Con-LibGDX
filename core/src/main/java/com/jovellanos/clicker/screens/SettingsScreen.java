package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
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
    private Texture fondoTexture;

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
    public void show() {
        if (fondoTexture == null) {
            fondoTexture = new Texture(Gdx.files.internal("img/FondoSettings.png"));
        }
        super.show();
    }

    @Override
    protected void buildUI() {
        idiomaActual = game.getGameState().getIdiomaActual();
        final LocaleManager i18n = LocaleManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        root.setBackground(new TextureRegionDrawable(new TextureRegion(fondoTexture)));

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

        final Label lblEfectos = new Label(i18n.getText("ajustes_volumen_efectos"), skin);
        final Label lblEfectosPct = new Label("70%", skin);
        final Slider sliderEfectos = new Slider(0, 100, 1, false, estiloSlider);
        sliderEfectos.setValue(70);

        final Label lblMusica = new Label(i18n.getText("ajustes_musica"), skin);
        final Label lblMusicaPct = new Label("50%", skin);
        final Slider sliderMusica = new Slider(0, 100, 1, false, estiloSlider);
        sliderMusica.setValue(50);

        final Label lblIdioma = new Label(i18n.getText("ajustes_idioma_label"), skin);
        
        // Configuración del SelectBox con estilo y padding personalizado
        SelectBox.SelectBoxStyle estiloSelect = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));
        
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        pixmap.fill();
        TextureRegionDrawable bgSelect = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        
        // Reducción del padding lateral a la mitad (aprox. 8f)
        bgSelect.setLeftWidth(8f);
        bgSelect.setRightWidth(8f);
        estiloSelect.background = bgSelect;
        
        if (skin.has("small", Label.LabelStyle.class)) {
            BitmapFont smallFont = skin.get("small", Label.LabelStyle.class).font;
            estiloSelect.font = smallFont;
            
            if (estiloSelect.listStyle != null) {
                // Se crea una copia de la fuente para reducir la escala solo en el menú desplegable
                BitmapFont listFont = new BitmapFont(smallFont.getData(), smallFont.getRegion(), smallFont.usesIntegerPositions());
                listFont.getData().setScale(0.85f);
                estiloSelect.listStyle.font = listFont;

                // Se aplica el mismo padding lateral a la selección de la lista
                if (estiloSelect.listStyle.selection != null) {
                    estiloSelect.listStyle.selection.setLeftWidth(8f);
                    estiloSelect.listStyle.selection.setRightWidth(8f);
                }
            }
        }
        
        final SelectBox<String> selectIdioma = new SelectBox<String>(estiloSelect);
        pixmap.dispose();

        selectIdioma.setItems("Español", "English");
        if (idiomaActual.equals("es")) {
            selectIdioma.setSelected("Español");
        } else {
            selectIdioma.setSelected("English");
        }

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

        panel.add(btnSalir).colspan(2).fillX().height(55).row();

        root.add(panel).width(560);

        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
            }
        });

        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
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
                if (btnReanudar != null) {
                    btnReanudar.setText(i18n.getText("pausa_reanudar"));
                }
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
        if (fondoTexture != null)
            fondoTexture.dispose();
    }
}