package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisTextButton.VisTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.jovellanos.clicker.MainGame;
import com.kotcrab.vis.ui.VisUI;
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
    en cada pantalla hija. Por el momento, se utilizan aquí
    temporalmente algunos colores para los botones, que se quitarán
    a futuro para no interferir con el Skin.
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

    /*
        Crea un TextButtonStyle con los colores morados del proyecto.
        Cada pantalla hija lo llama al crear sus botones:
        new VisTextButton("texto", crearEstiloBoton())
        Normal:  #21083B | Pressed/Over: #470C7A
    */
    protected VisTextButtonStyle crearEstiloBoton() {
      VisTextButtonStyle style = new VisTextButtonStyle();
      style.font      = VisUI.getSkin().getFont("default-font");
      style.fontColor = Color.WHITE;
      style.up        = crearColorDrawable(new Color(0x21083Bff));
      style.over      = crearColorDrawable(new Color(0x470C7Aff));
      style.down      = crearColorDrawable(new Color(0x470C7Aff));
      return style;
  }

    // Crea un drawable de color sólido para usar como fondo de botón
    protected TextureRegionDrawable crearColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable drawable = new TextureRegionDrawable(
            new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return drawable;
    }

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