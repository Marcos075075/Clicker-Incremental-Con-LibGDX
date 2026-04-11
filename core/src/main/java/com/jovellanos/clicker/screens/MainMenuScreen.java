package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.core.ResourceManager;
import com.jovellanos.clicker.i18n.LocaleManager;

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
    - Botón "Salir"           -> cierra el juego completamente

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
    public void show() {
        super.show();
    }

    @Override
    protected void buildUI() {
        LocaleManager i18n = LocaleManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        // Aplicación del fondo a la tabla raíz
        root.setBackground(new TextureRegionDrawable(new TextureRegion(ResourceManager.fondoMain)));

        // Título con fuente 'large' y escalado manual aumentado
        Label titulo = new Label(i18n.getText("app_titulo"), skin, "large");
        titulo.setFontScale(1.5f);
        
        // Subtítulo y resto de elementos con fuente default
        Label subtitulo = new Label(i18n.getText("app_version"), skin);

        // Botones principales
        TextButton btnNueva   = new TextButton(i18n.getText("menu_nueva_partida"), skin);
        TextButton btnCargar  = new TextButton(i18n.getText("menu_cargar_partida"), skin);
        TextButton btnAjustes = new TextButton(i18n.getText("menu_ajustes"), skin);
        TextButton btnSalir = new TextButton(i18n.getText("menu_salir"), skin);

        // Layout centrado verticalmente
        root.add(titulo).padBottom(8).row();
        root.add(subtitulo).padBottom(80).row();
        root.add(btnNueva).width(380).height(65).padBottom(16).row();
        root.add(btnCargar).width(380).height(65).padBottom(16).row();
        root.add(btnAjustes).width(380).height(65).row();
        root.add(btnSalir).width(380).height(65).padTop(16).row();

        // Listeners
        btnNueva.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Se comprueba la existencia de la partida a través del gestor de guardado
                com.jovellanos.clicker.persistence.SaveManager saveManager = new com.jovellanos.clicker.persistence.SaveManager();
                if (saveManager.saveExists()) {
                    mostrarDialogoConfirmacion();
                } else {
                    iniciarNuevaPartida();
                }
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

        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    private void mostrarDialogoConfirmacion() {
        Skin skin = ResourceManager.getSkin();
        LocaleManager i18n = LocaleManager.getInstance();

        // Generación del fondo oscuro al 75% de opacidad
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.75f)); 
        pixmap.fill();
        TextureRegionDrawable fondoTranslucido = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        Dialog.WindowStyle dialogStyle = new Dialog.WindowStyle();
        dialogStyle.background = fondoTranslucido;
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;

        Dialog dialog = new Dialog("", dialogStyle) {
            @Override
            protected void result(Object object) {
                boolean confirmar = (Boolean) object;
                if (confirmar) {
                    iniciarNuevaPartida();
                }
            }
        };

        // Textos del diálogo con fuente por defecto
        Label lblTitulo = new Label(i18n.getText("aviso_titulo"), skin);
        lblTitulo.setAlignment(Align.center);
        
        Label lblAviso = new Label(i18n.getText("aviso_sobreescribir"), skin);
        lblAviso.setAlignment(Align.center);
        lblAviso.setFontScale(0.85f);
        
        dialog.getContentTable().add(lblTitulo).padBottom(15).row();
        dialog.getContentTable().add(lblAviso).padBottom(20).row();
        
        // Botones de respuesta con fuente pequeña
        TextButton.TextButtonStyle smallBtnStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        if (skin.has("small", Label.LabelStyle.class)) {
            smallBtnStyle.font = skin.get("small", Label.LabelStyle.class).font;
        }
        
        TextButton btnSi = new TextButton(i18n.getText("opcion_si"), smallBtnStyle);
        btnSi.pad(15f, 30f, 15f, 30f);
        
        TextButton btnNo = new TextButton(i18n.getText("opcion_no"), smallBtnStyle);
        btnNo.pad(15f, 30f, 15f, 30f);

        dialog.getButtonTable().defaults().pad(0, 15f, 0, 15f);

        dialog.button(btnSi, true);
        dialog.button(btnNo, false);

        dialog.pad(40f);
        dialog.getButtonTable().padTop(20f);

        dialog.show(stage);
    }

    private void iniciarNuevaPartida() {
        game.getGameState().reset();
        game.changeScreen(ScreenType.INTRO); 
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        // Recalcula la posición central de los cuadros de diálogo al cambiar el tamaño de la ventana
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Dialog) {
                actor.setPosition(Math.round((stage.getWidth() - actor.getWidth()) / 2), Math.round((stage.getHeight() - actor.getHeight()) / 2));
            }
        }
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