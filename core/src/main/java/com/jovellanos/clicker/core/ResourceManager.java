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
    public static Texture fondoGalaxia;
    public static Texture fondoNave;
    public static Texture iconoAvance;
    public static Texture iconoTierra1;
    public static Texture iconoTierra2;
    public static Texture maia1;
    public static Texture maiaTablet;
    public static Texture monito1;
    public static Texture monitoTablet;
    public static Texture fondoGalaxiaandroid;
    public static Texture fondoNaveandroid;

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
        fondoGalaxia = new Texture(Gdx.files.internal("img/FondoGalaxia.png"));
        fondoGalaxiaandroid = new Texture(Gdx.files.internal("img/FondoGalaxia_Android.png"));
        fondoNaveandroid = new Texture(Gdx.files.internal("img/FondoNave_Android.png"));
        fondoNave = new Texture(Gdx.files.internal("img/FondoNave.png"));
        iconoAvance = new Texture(Gdx.files.internal("img/IconoAvance.png"));
        iconoTierra1 = new Texture(Gdx.files.internal("img/IconoTierra1.png"));
        iconoTierra2 = new Texture(Gdx.files.internal("img/IconoTierra2.png"));
        maia1 = new Texture(Gdx.files.internal("img/Maia1.png"));
        maiaTablet = new Texture(Gdx.files.internal("img/MaiaTablet.png"));
        monito1 = new Texture(Gdx.files.internal("img/Monito1.png"));
        monitoTablet = new Texture(Gdx.files.internal("img/MonitoTablet.png"));
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
        if (fondoGalaxia != null) fondoGalaxia.dispose();
        if (fondoNave != null) fondoNave.dispose();
        if (iconoAvance != null) iconoAvance.dispose();
        if (iconoTierra1 != null) iconoTierra1.dispose();
        if (iconoTierra2 != null) iconoTierra2.dispose();
        if (maia1 != null) maia1.dispose();
        if (maiaTablet != null) maiaTablet.dispose();
        if (monito1 != null) monito1.dispose();
        if (monitoTablet != null) monitoTablet.dispose();
    }
}