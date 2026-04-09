package com.jovellanos.clicker.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ResourceManager {
    private static Skin skin;

    //Necesario para asegurarse de que no se haga un new resourceManager
    private ResourceManager() {
        throw new IllegalStateException("Clase de utilidad - No instanciar"); 
    }

    public static void load() {
        // Carga el archivo generado con SkinComposer
        skin = new Skin(Gdx.files.internal("skin/Skin.json"));
    }

    public static Skin getSkin() {
        return skin;
    }

    public static void dispose() {
        if (skin != null) skin.dispose();
    }
}