package com.jovellanos.clicker.upgrades;

/*
    ===============================================
    MultiplierUpgrade — Mejora Multiplicadora
    ===============================================
    Aumenta el rendimiento de una AutomatedUpgrade concreta.
    No genera PP directamente; actúa como un factor de escala
    sobre la producción de la estructura objetivo.

    Ejemplo: "Multiplicador de Núcleo" — cada unidad comprada
    añade un +5 % a la producción de "GranjaDeServidores".

    ===============================================
    Cálculo del multiplicador total
    ===============================================
    getTotalMultiplier() = 1.0 + (multiplierPerUnit * quantity)

    Con 0 unidades → ×1.0  (sin efecto)
    Con 3 unidades al 5 % → 1.0 + 0.05 * 3 = ×1.15

    El Logic Thread consulta getTotalMultiplier() y lo pasa a
    AutomatedUpgrade.getPPperSecond(multiplier) de la estructura
    cuyo id coincide con targetUpgradeId.

    ===============================================
    Vinculación
    ===============================================
    targetUpgradeId debe coincidir exactamente con el id de la
    mejora objetivo (Direct o Automated). El UpgradeFactory
    garantiza que esta correspondencia es válida al crear las mejoras.
*/

public class MultiplierUpgrade extends Upgrade {

    /** Id de la mejora (Direct o Automated) cuya producción se multiplica. */
    private final String targetUpgradeId;

    /** Fracción de PP/s extra que añade cada unidad. Ej: 0.05 = +5 % por unidad. */
    private final double multiplierPerUnit;

    public MultiplierUpgrade(String id,
                             String nameKey,
                             String descKey,
                             double baseCost,
                             double costMultiplier,
                             String targetUpgradeId,
                             double multiplierPerUnit) {
        super(id, nameKey, descKey, baseCost, costMultiplier);
        this.targetUpgradeId  = targetUpgradeId;
        this.multiplierPerUnit = multiplierPerUnit;
    }

    // -------------------------------------------------------
    // Getters
    // -------------------------------------------------------

    /** Id de la estructura objetivo de este multiplicador. */
    public String getTargetUpgradeId() {
        return targetUpgradeId;
    }

    /** Incremento fraccional por unidad (0.05 = 5 %). */
    public double getMultiplierPerUnit() {
        return multiplierPerUnit;
    }

    // -------------------------------------------------------
    // Contribución al estado del juego
    // -------------------------------------------------------

    /**
     * Factor total a aplicar sobre la producción de la estructura objetivo.
     * Nunca baja de 1.0 (con 0 unidades el multiplicador es neutro).
     */
    public double getTotalMultiplier() {
        return 1.0 + (multiplierPerUnit * quantity);
    }
}