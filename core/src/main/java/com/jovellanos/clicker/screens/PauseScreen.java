package com.jovellanos.clicker.screens;

import com.jovellanos.clicker.MainGame;

/*
    ===============================================
    Pausa
    ===============================================
    Esta pantalla ya no se usa directamente. La funcionalidad
    de pausa y ajustes está integrada en SettingsScreen,
    que muestra el botón Reanudar cuando se abre desde el juego.
*/

public class PauseScreen extends BaseScreen {

    public PauseScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void buildUI() {
        // Sin contenido — ver SettingsScreen
    }
}