package com.jovellanos.clicker.core;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/*
    ===============================================
    GameStateSnapshot — Copia inmutable del GameState
    ===============================================
    El IOThread nunca accede directamente al GameState
    para evitar condiciones de carrera. En su lugar,
    GameState genera un snapshot con takeSnapshot()
    y el IOThread serializa esa copia.

    ppActual y ppHistorico son BigInteger para soportar
    las cifras enormes que los juegos clicker acumulan
    en sesiones largas (superan el límite de long).

    Patrón: Memento
*/
public class GameStateSnapshot {

    public final BigInteger          ppActual;
    public final BigInteger          ppHistorico;
    public final double              ppPorClick;
    public final double              ppPorSegundo;
    public final Map<String, Integer> mejorasAdquiridas;
    public final String              idiomaActual;
    public final long                ultimoGuardado;
    public final int                 screenMode;

    public GameStateSnapshot(
            BigInteger ppActual,
            BigInteger ppHistorico,
            double ppPorClick,
            double ppPorSegundo,
            Map<String, Integer> mejorasAdquiridas,
            String idiomaActual,
            long ultimoGuardado,
            int screenMode) {

        this.ppActual          = ppActual;
        this.ppHistorico       = ppHistorico;
        this.ppPorClick        = ppPorClick;
        this.ppPorSegundo      = ppPorSegundo;
        this.mejorasAdquiridas = new HashMap<String, Integer>(mejorasAdquiridas);
        this.idiomaActual      = idiomaActual;
        this.ultimoGuardado    = ultimoGuardado;
        this.screenMode        = screenMode;
    }
}