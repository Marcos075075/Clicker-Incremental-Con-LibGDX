package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.jovellanos.clicker.MainGame;
import com.kotcrab.vis.ui.widget.VisTable;

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

    ===============================================
    Nota sobre colores y estilos
    ===============================================
    El color de fondo y los estilos visuales (fuentes, colores de
    botones, bordes) se definirán en Skin Composer y se aplicarán
    en cada pantalla hija. Este BaseScreen no aplica ningún estilo
    visual para no interferir con el skin.
*/

public abstract class BaseScreen implements Screen {

    protected final MainGame game;
    protected Stage stage;
    protected VisTable root;

    public BaseScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        root = new VisTable();
        root.setFillParent(true);
        stage.addActor(root);

        buildUI();
    }

    // Cada pantalla construye aquí su UI específica
    protected abstract void buildUI();

    @Override
    public void render(float delta) {
        // Fondo negro neutro — el color final vendrá del skin
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