package com.jovellanos.clicker.core;
 
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.text.NumberFormatter;

public class GameState {
    /**
     * Número actual de partículas que el usuario tiene para gastar.
     * Accedido concurrentemente por Main Thread (clics) y Logic Thread (pasivo).
     * Protegido mediante synchronized en addPP y gastarPP.
     */
    private double PPactual = 0.0;

     /**
     * Total de partículas que ha ganado el usuario a lo largo de toda la partida.
     * Se incrementa junto con PPactual en cada ganancia; nunca decrece.
     */
    private double PPhistorico = 0.0;

    /**
     * Número de partículas que gana el usuario al hacer clic.
     * Solo se modifica al comprar mejoras directas (operación puntual).
     * volatile garantiza visibilidad entre hilos sin bloqueo.
     */
    private volatile double PPporClick = 1.0;

    /**
     * Número de partículas que gana el usuario de forma automática por segundo.
     * Calculado y escrito exclusivamente por el Logic Thread.
     * volatile garantiza visibilidad sin necesitar exclusión mutua.
     */
    private volatile double PPporSegundo = 0.0;
    
    /**
     * Map ID de mejora → cantidad adquirida.
     * ConcurrentHashMap permite acceso concurrente seguro sin bloqueos.
     */
    private final ConcurrentHashMap<String, Integer> mejorasAdquiridas = new ConcurrentHashMap<>();
    
    /** Idioma activo para el sistema i18n (ej: "es", "en"). */
    private volatile String idiomaActual = "es";

    /**
     * Timestamp en milisegundos del último guardado exitoso.
     * Permite calcular la progresión offline al reanudar.
     */
    private volatile long ultimoGuardado = System.currentTimeMillis();

    /** Crea un GameState de partida nueva con todos los valores iniciales. */
    public GameState(){};

    /**
     * Añade la cantidad indicada a PPactual y PPhistorico.
     *
     * @param cantidad PP a añadir; se ignora si es menor o igual a 0 o no finita
     */
    public synchronized void addPP(double cantidad) {
        if (cantidad <= 0 || !Double.isFinite(cantidad)) return;
        PPactual    += cantidad;
        PPhistorico += cantidad;
    }

    /**
     * Intenta descontar el coste indicado de PPactual.
     *
     * @param coste PP a descontar; debe ser positivo
     * @return true si había PP suficientes y se descontaron
     */
    public synchronized boolean gastarPP(double coste) {
        if (coste <= 0 || !Double.isFinite(coste)) return false;
        if (PPactual < coste) return false;
        PPactual -= coste;
        return true;
    }

    /**
     * Devuelve la cantidad adquirida de una mejora por su ID.
     *
     * @param id identificador único de la mejora
     * @return cantidad adquirida, o 0 si no se ha comprado ninguna
     */
    public int getCantidadMejora(String id) {
        return mejorasAdquiridas.getOrDefault(id, 0);
    }

    /**
     * Establece la cantidad de una mejora (usado al cargar partida).
     *
     * @param id       identificador único de la mejora
     * @param cantidad cantidad a almacenar
     */
    public void setCantidadMejora(String id, int cantidad) {
        mejorasAdquiridas.put(id, cantidad);
    }

    /**
     * Incrementa en 1 la cantidad de una mejora (usado al comprar).
     * Thread-safe gracias a ConcurrentHashMap.merge.
     *
     * @param id identificador único de la mejora
     */
    public void incrementarMejora(String id) {
        mejorasAdquiridas.merge(id, 1, Integer::sum);
    }

    /** Actualiza el timestamp de último guardado al momento actual. */
    public void marcarGuardado() {
        this.ultimoGuardado = System.currentTimeMillis();
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================
 
    public synchronized double getPPactual()          { return PPactual; }
    public synchronized double getPPhistorico()       { return PPhistorico; }
    public synchronized void   setPPactual(double v)  { PPactual    = v; }
    public synchronized void   setPPhistorico(double v) { PPhistorico = v; }
 
    public double getPPporClick()           { return PPporClick; }
    public double getPPporSegundo()         { return PPporSegundo; }
    public void   setPPporClick(double v)   { PPporClick   = v; }
    public void   setPPporSegundo(double v) { PPporSegundo = v; }
 
    public Map<String, Integer> getMejorasAdquiridas() { return mejorasAdquiridas; }
 
    public String getIdiomaActual()               { return idiomaActual; }
    public void   setIdiomaActual(String idioma)  { this.idiomaActual = idioma; }
 
    public long getUltimoGuardado()               { return ultimoGuardado; }
    public void setUltimoGuardado(long timestamp) { this.ultimoGuardado = timestamp; }
 
    /* @Override
    public String toString() {
        return "GameState{PPactual="  + NumberFormatter.formatear(getPPactual())
            + ", PP/click=" + PPporClick
            + ", PP/s="     + PPporSegundo
            + ", mejoras="  + mejorasAdquiridas.size() + "}";
    } */
}
