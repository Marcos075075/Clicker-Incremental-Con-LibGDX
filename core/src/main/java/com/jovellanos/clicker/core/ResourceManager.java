package com.jovellanos.clicker.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ResourceManager {
    private static Skin skin;

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