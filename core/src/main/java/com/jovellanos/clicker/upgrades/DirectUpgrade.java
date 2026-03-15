package com.jovellanos.clicker.upgrades;

/*
    ===============================================
    DirectUpgrade — Mejora Directa
    ===============================================
    Aumenta la cantidad de PP que el jugador obtiene por cada clic.
 
    ===============================================
    Cálculo
    ===============================================
    getPPperClick()            = ppPerClickBase * quantity
    getPPperClick(multiplier)  = ppPerClickBase * quantity * multiplier
 
    El GameState suma el resultado de getPPperClick(multiplier)
    al PPporClick total, pasando el factor obtenido de
    UpgradeFactory.getMultiplierFor(id).
 
    Si no hay ningún MultiplierUpgrade apuntando a esta mejora,
    getMultiplierFor() devuelve 1.0 y el resultado es idéntico
    a llamar a getPPperClick() sin argumentos.
*/

public class DirectUpgrade extends Upgrade {

     /** PP adicionales por clic que aporta cada unidad de esta mejora. */
    private final double ppPerClickBase;
 
    public DirectUpgrade(String id,
                         String nameKey,
                         String descKey,
                         double baseCost,
                         double costMultiplier,
                         double ppPerClickBase) {
        super(id, nameKey, descKey, baseCost, costMultiplier);
        this.ppPerClickBase = ppPerClickBase;
    }

    // -------------------------------------------------------
    // Contribución al estado del juego
    // -------------------------------------------------------
 
    /**
     * PP extra por clic de todas las unidades CON multiplicador externo.
     * Es el método que debe usar el GameState para calcular PPporClick.
     *
     * @param multiplier  Factor calculado por los MultiplierUpgrade
     *                    que apuntan a esta mejora (≥ 1.0).
     */
    public double getPPperClick(double multiplier) {
        return ppPerClickBase * quantity * multiplier;
    }

    /**
     * PP extra por clic de todas las unidades SIN multiplicador.
     * Sobrecarga de conveniencia equivalente a getPPperClick(1.0).
     * Útil para mostrar el valor base en la tienda.
     */
    public double getPPperClick() {
        return getPPperClick(1.0);
    }

    /** PP por clic de una sola unidad sin multiplicar (para la tienda). */
    public double getPPperClickBase() {
        return ppPerClickBase;
    }

}
