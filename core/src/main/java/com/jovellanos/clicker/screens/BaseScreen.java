package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.jovellanos.clicker.MainGame;


/*
    ===============================================
    BaseScreen
    ===============================================
    Clase abstracta de la que heredan TODAS las pantallas del juego.
    Centraliza la lógica común para no duplicar código en cada pantalla:

    - Stage y Viewport: se crean aquí una sola vez. El Stage es el
      contenedor de Scene2D que gestiona botones, labels, etc
      El ScreenViewport hace que la UI se adapte a cualquier resolución.

    - InputProcessor: se asigna el Stage como receptor de eventos de
      entrada (clics, teclado) al mostrar la pantalla.

    - Tabla raíz (root): VisTable que ocupa toda la pantalla y sirve
      de base para colocar el resto de elementos en cada pantalla hija.

    - buildUI(): método abstracto que cada pantalla hija implementa
      para construir su propia interfaz.

    - render(), resize() y dispose() ya están implementados aquí
      para que las pantallas hijas no tengan que repetirlos.

    El flujo está definido aquí (show -> buildUI -> render),
    y cada hija solo sobreescribe buildUI() con su contenido propio.
*/

public abstract class BaseScreen implements Screen {

    protected final MainGame game;
    protected Stage stage;
    protected Table root;

    public BaseScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Si el stage ya existe no se recrea, solo se reasigna el InputProcessor
        // Evita el pantallazo negro al abrir tooltips o desplegables
        if (stage != null) {
            Gdx.input.setInputProcessor(stage);
            return;
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        buildUI();
        
        // F11 alterna entre pantalla completa y ventana sin bordes
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.F11) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            com.badlogic.gdx.Graphics.DisplayMode displayMode =
                                Gdx.graphics.getDisplayMode();
                            if (Gdx.graphics.isFullscreen()) {
                                // Volver a ventana sin bordes maximizada
                                Gdx.graphics.setWindowedMode(
                                    displayMode.width, displayMode.height);
                            } else {
                                // Pasar a pantalla completa
                                Gdx.graphics.setFullscreenMode(displayMode);
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }

    // Cada pantalla construye aquí su UI específica
    protected abstract void buildUI();

    /*
        Crea un TextButtonStyle con los colores morados del proyecto.
        Cada pantalla hija lo llama al crear sus botones:
        new VisTextButton("texto", crearEstiloBoton())
        Normal:  #21083B | Pressed/Over: #470C7A
    */

    // Crea un drawable de color sólido para usar como fondo de botón

    @Override
    public void render(float delta) {
        // Fondo negro neutro, cada clase Screen aplicará su fondo
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}