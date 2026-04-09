package com.jovellanos.clicker.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ResourceManager {
    private static Skin skin;
    
    public static Texture fondoJuego;
    public static Texture texturaNucleo;
    public static Texture texturaIconoPrueba;
    public static Texture fondoMain;
    public static Texture fondoSettings;

    //Necesario para asegurarse de que no se haga un new resourceManager
    private ResourceManager() {
        throw new IllegalStateException("Clase de utilidad - No instanciar"); 
    }

    public static void load() {
        // Carga el archivo generado con SkinComposer
        skin = new Skin(Gdx.files.internal("skin/Skin.json"));

        // Carga las imagenes del juego
        fondoJuego = new Texture(Gdx.files.internal("img/FondoJuego.png"));
        texturaNucleo = new Texture(Gdx.files.internal("img/nucleo.png"));
        texturaIconoPrueba = new Texture(Gdx.files.internal("img/iconoprueba.png"));
        fondoMain = new Texture(Gdx.files.internal("img/FondoMain.png"));
        fondoSettings = new Texture(Gdx.files.internal("img/FondoSettings.png"));
    }

    public static Skin getSkin() {
        return skin;
    }

    public static void dispose() {
        if (skin != null) skin.dispose();
        if (fondoJuego != null) fondoJuego.dispose();
        if (texturaNucleo != null) texturaNucleo.dispose();
        if (texturaIconoPrueba != null) texturaIconoPrueba.dispose();
        if (fondoMain != null) fondoMain.dispose();
        if (fondoSettings != null) fondoSettings.dispose();
    }
}