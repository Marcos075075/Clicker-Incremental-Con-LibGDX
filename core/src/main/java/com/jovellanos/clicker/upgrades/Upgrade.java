package com.jovellanos.clicker.upgrades;

/*
    ===============================================
    Upgrade — Clase base abstracta
    ===============================================
    Todas las mejoras del juego heredan de esta clase.
    Centraliza los atributos y la lógica común:
 
    - id:              Identificador único. Ej: "GranjaDeServidores", "MejoraCursor".
    - nameKey:         Clave i18n para el nombre visible en la tienda.
    - descKey:         Clave i18n para la descripción.
    - baseCost:        Coste en PP de la primera unidad.
    - costMultiplier:  Factor de escalado del precio (por defecto 1.15).
                       Fórmula: baseCost * costMultiplier^quantity
    - quantity:        Unidades adquiridas; se persiste en el JSON.
 
    ===============================================
    Fórmula de coste
    ===============================================
    getCurrentCost() = floor(baseCost * costMultiplier ^ quantity)
    Es el estándar del género clicker (Cookie Clicker, AdVenture Capitalist).
    floor() asegura que el coste siempre sea un número entero de PP.
*/

public class Upgrade {

    protected final String id;
    protected final String nameKey;
    protected final String descKey;
    protected final double baseCost;
    protected final double costMultiplier;
    protected int quantity;
 
    public Upgrade(String id,
                   String nameKey,
                   String descKey,
                   double baseCost,
                   double costMultiplier) {
        this.id             = id;
        this.nameKey        = nameKey;
        this.descKey        = descKey;
        this.baseCost       = baseCost;
        this.costMultiplier = costMultiplier;
        this.quantity       = 0;
    }

    // -------------------------------------------------------
    // Getters
    // -------------------------------------------------------
 
    public String getId()       { return id; }
    public String getNameKey()  { return nameKey; }
    public String getDescKey()  { return descKey; }
    public int    getQuantity() { return quantity; }
 
    // -------------------------------------------------------
    // Lógica de compra
    // -------------------------------------------------------
 
    /** Coste actual en PP según las unidades ya compradas. */
    public double getCurrentCost() {
        return Math.floor(baseCost * Math.pow(costMultiplier, quantity));
    }
 
    /** Devuelve true si el jugador puede permitirse una unidad más. */
    public boolean canAfford(double currentPP) {
        return currentPP >= getCurrentCost();
    }
 
    /**
     * Registra la compra de una unidad.
     */
    public void purchase() {
        quantity++;
    }
 
    /**
     * Restaura la cantidad desde el JSON al cargar partida.
     * Llamado por SaveManager durante la deserialización.
     */
    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        }
    }

}
