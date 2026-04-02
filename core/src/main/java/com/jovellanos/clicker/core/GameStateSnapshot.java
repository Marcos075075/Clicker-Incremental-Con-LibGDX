package com.jovellanos.clicker.core;

import java.util.*;

/*
    ===============================================
    GameStateSnapshot — Copia inmutable del GameState
    ===============================================
    El IOThread nunca accede directamente al GameState
    para evitar condiciones de carrera. En su lugar,
    GameState genera un snapshot con createSnapshot()
    y el IOThread serializa esa copia.

    Patrón: Memento
*/
public class GameStateSnapshot {

    public final long ppActual;
    public final long ppHistorico;
    public final double ppPorClick;
    public final double ppPorSegundo;
    public final Map<String, Integer> mejorasAdquiridas;
    public final String idiomaActual;
    public final long ultimoGuardado;

    public GameStateSnapshot(long ppActual, long ppHistorico, double ppPorClick, double ppPorSegundo, Map<String, Integer> mejorasAdquiridas, String idiomaActual, long ultimoGuardado) {
        this.ppActual = ppActual;
        this.ppHistorico = ppHistorico;
        this.ppPorClick = ppPorClick;
        this.ppPorSegundo = ppPorSegundo;
        this.mejorasAdquiridas = new HashMap<String, Integer>(mejorasAdquiridas);
        this.idiomaActual = idiomaActual;
        this.ultimoGuardado = ultimoGuardado;
    }
}
