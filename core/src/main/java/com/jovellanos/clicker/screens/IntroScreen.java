package com.jovellanos.clicker.screens;

import com.jovellanos.clicker.MainGame;

/*
    ===============================================
    Introducción
    ===============================================
    Pantalla con la narrativa que aparece al iniciar una nueva 
    partida, antes de entrar al juego.

    Flujo: MainMenuScreen -> IntroScreen -> GameScreen

    De momento está vacía. En fases posteriores se implementará
    con algo de texto narrativo y animación de entrada al clicker.
*/

public class IntroScreen extends BaseScreen {

    public IntroScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void buildUI() {
        // Pendiente: implementar intro narrativa en fases posteriores
    }
}