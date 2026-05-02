package com.jovellanos.clicker.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SettingsManager {

    private static final String PREFS_NAME = "clicker_settings";
    
    // Claves para el diccionario
    private static final String KEY_IDIOMA = "idioma";
    private static final String KEY_SCREEN_MODE = "screenMode";
    private static final String KEY_VOL_MUSIC = "vol_music";
    private static final String KEY_VOL_SFX = "vol_sfx";

    private static Preferences getPrefs() {
        return Gdx.app.getPreferences(PREFS_NAME);
    }

    // --- IDIOMA ---
    public static void setIdioma(String idioma) {
        getPrefs().putString(KEY_IDIOMA, idioma);
        getPrefs().flush(); // Guarda en disco inmediatamente
    }

    public static String getIdioma() {
        return getPrefs().getString(KEY_IDIOMA, "es"); // "es" por defecto
    }

    // --- VOLUMEN ---
    public static void setMusicVolume(float volume) {
        getPrefs().putFloat(KEY_VOL_MUSIC, volume);
        getPrefs().flush();
    }

    public static float getMusicVolume() {
        return getPrefs().getFloat(KEY_VOL_MUSIC, 1.0f); // 1.0f (100%) por defecto
    }

    public static void setSfxVolume(float volume) {
        getPrefs().putFloat(KEY_VOL_SFX, volume);
        getPrefs().flush();
    }

    public static float getSfxVolume() {
        return getPrefs().getFloat(KEY_VOL_SFX, 1.0f); // 1.0f por defecto
    }

    // --- MODO DE PANTALLA ---
    public static void setScreenMode(int mode) {
        getPrefs().putInteger(KEY_SCREEN_MODE, mode);
        getPrefs().flush();
    }

    public static int getScreenMode() {
        return getPrefs().getInteger(KEY_SCREEN_MODE, 0); // 0 (Ventana) por defecto
    }
}