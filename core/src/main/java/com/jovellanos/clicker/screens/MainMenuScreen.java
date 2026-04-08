package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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

    private Texture fondoTexture;

    public MainMenuScreen(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        if (fondoTexture == null) {
            fondoTexture = new Texture(Gdx.files.internal("img/FondoMain.png"));
        }
        super.show();
    }

    @Override
    protected void buildUI() {
        LocaleManager i18n = LocaleManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        // Aplicación del fondo a la tabla raíz
        root.setBackground(new TextureRegionDrawable(new TextureRegion(fondoTexture)));

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

        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
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
        if (fondoTexture != null) fondoTexture.dispose();
    }
}