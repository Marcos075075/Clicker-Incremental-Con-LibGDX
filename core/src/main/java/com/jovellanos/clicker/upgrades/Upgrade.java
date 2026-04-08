package com.jovellanos.clicker.upgrades;

import java.math.BigInteger;

/*
    ===============================================
    Upgrade — Clase base abstracta
    ===============================================
    Todas las mejoras del juego heredan de esta clase.
    Centraliza los atributos y la lógica común:

    - id:              Identificador único. Ej: "GranjaDeServidores".
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
    Devuelve double; la conversión a BigInteger se hace en los
    consumidores (PurchaseService, canAfford) con un cast seguro,
    ya que Math.floor() garantiza que el valor es un entero.

    ===============================================
    canAfford(BigInteger)
    ===============================================
    El saldo del jugador ahora es BigInteger. El coste sigue siendo
    double porque las fórmulas de progresión trabajan en punto flotante.
    La comparación convierte el coste a BigInteger via longValue():
    los costes no deberían superar Long.MAX_VALUE en gameplay normal;
    si llegaran a hacerlo, el jugador claramente puede pagarlos.
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

    // ── Getters ──────────────────────────────────────────────────────────

    public String getId()       { return id; }
    public String getNameKey()  { return nameKey; }
    public String getDescKey()  { return descKey; }
    public int    getQuantity() { return quantity; }

    // ── Lógica de coste ──────────────────────────────────────────────────

    /** Coste actual en PP según las unidades ya compradas. */
    public double getCurrentCost() {
        return Math.floor(baseCost * Math.pow(costMultiplier, quantity));
    }

    /**
     * Devuelve true si el jugador puede comprar una unidad más.
     * El saldo es BigInteger; el coste se convierte con longValue()
     * para la comparación (ver Javadoc de clase).
     */
    public boolean canAfford(BigInteger currentPP) {
        BigInteger costeBig = BigInteger.valueOf((long) getCurrentCost());
        return currentPP.compareTo(costeBig) >= 0;
    }

    // ── Lógica de compra ─────────────────────────────────────────────────

    /** Registra la compra de una unidad. */
    public void purchase() {
        quantity++;
    }

    /**
     * Restaura la cantidad desde el JSON al cargar partida.
     * Llamado por GameState.cargarDesdeSaveData().
     */
    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        }
    }
}