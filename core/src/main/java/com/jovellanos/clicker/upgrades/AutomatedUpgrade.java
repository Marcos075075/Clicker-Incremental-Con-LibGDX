package com.jovellanos.clicker.upgrades;

/*
    ===============================================
    AutomatedUpgrade — Mejora Automatizada (Estructura)
    ===============================================
    Representa un edificio/estructura que genera PP de forma pasiva
    y continua sin intervención del jugador. Aparece en la columna
    central de la pantalla de juego una vez adquirida.

    Ejemplos: "Granja de Servidores", "Centro de Procesado", etc.

    ===============================================
    Cálculo de PP/s
    ===============================================
    getBasePPperSecond()    = ppPerSecondBase * quantity
    getPPperSecond(mult)    = getBasePPperSecond() * mult

    El Logic Thread llama a getPPperSecond(multiplier) pasando el
    multiplicador calculado por los MultiplierUpgrade asociados.
    Si no hay multiplicadores activos, mult = 1.0.

    ===============================================
    Ciclo de actualización
    ===============================================
    El Logic Thread acumula la contribución de todas las
    AutomatedUpgrade en cada tick para actualizar el PPporSegundo
    del GameState.
*/

public class AutomatedUpgrade extends Upgrade {

    /** PP por segundo que aporta CADA unidad de esta estructura. */
    private final double ppPerSecondBase;

    public AutomatedUpgrade(String id,
                            String nameKey,
                            String descKey,
                            double baseCost,
                            double costMultiplier,
                            double ppPerSecondBase) {
        super(id, nameKey, descKey, baseCost, costMultiplier);
        this.ppPerSecondBase = ppPerSecondBase;
    }

    // -------------------------------------------------------
    // Contribución al estado del juego
    // -------------------------------------------------------

    /**
     * PP/s totales SIN aplicar multiplicadores externos.
     * Útil para mostrar en la UI el valor base de la estructura.
     */
    public double getBasePPperSecond() {
        return ppPerSecondBase * quantity;
    }

    /**
     * PP/s totales CON el multiplicador de los MultiplierUpgrade
     * asociados a esta estructura. Es el valor que usa el Logic Thread.
     *
     * @param multiplier  Factor calculado por los MultiplierUpgrade
     *                    que apuntan a esta estructura (≥ 1.0).
     */
    public double getPPperSecond(double multiplier) {
        return getBasePPperSecond() * multiplier;
    }

    /** PP/s de una sola unidad sin multiplicar (para la tienda). */
    public double getPPperSecondBase() {
        return ppPerSecondBase;
    }
}