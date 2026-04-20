package com.jovellanos.clicker.logic;

import com.jovellanos.clicker.audio.AudioManager;
import com.jovellanos.clicker.core.GameState;
import com.jovellanos.clicker.upgrades.Upgrade;
import com.jovellanos.clicker.upgrades.UpgradeFactory;

import java.math.BigInteger;
import java.util.Map;

/*
    ===============================================
    PurchaseService — Servicio de Compras
    ===============================================
    Centraliza toda la lógica de negocio de las compras.
    GameState solo expone primitivas de estado (subtractPP,
    setPpPorClick); PurchaseService orquesta el flujo completo.

    ===============================================
    BigInteger en la comparación de saldo
    ===============================================
    El saldo del jugador es BigInteger. El coste de las mejoras
    sigue siendo double (fórmula exponencial de punto flotante).
    La conversión coste → BigInteger usa (long) getCurrentCost(),
    que es segura porque getCurrentCost() aplica Math.floor() y los
    costes no deberían superar Long.MAX_VALUE en gameplay normal.

    ===============================================
    Sincronización
    ===============================================
    comprar() sincroniza sobre el monitor del GameState para garantizar
    la atomicidad del patrón check-then-act (comprobar saldo → descontar).
*/
public class PurchaseService {

    /**
     * Intenta comprar una unidad de la mejora indicada.
     *
     * @param id        Identificador único de la mejora.
     * @param gameState Estado central de la partida.
     * @return          {@code true} si la compra se realizó con éxito.
     */
    public boolean comprar(String id, GameState gameState) {
        synchronized (gameState) {
            Map<String, Upgrade> upgrades = gameState.getUpgrades();
            Upgrade upgrade = upgrades.get(id);
            if (upgrade == null) return false;

            // Convertir coste double → BigInteger para comparar con el saldo
            BigInteger coste = BigInteger.valueOf((long) upgrade.getCurrentCost());

            if (gameState.getPpActual().compareTo(coste) < 0) return false;

            // 1. Descontar coste
            gameState.subtractPP(coste);

            // 2. Registrar la unidad comprada
            upgrade.purchase();

            // 3. Recalcular PP/clic con UpgradeFactory (lógica fuera de GameState)
            double nuevoPPporClick = UpgradeFactory.calcularPPporClick(upgrades);
            gameState.setPpPorClick(nuevoPPporClick);

            return true;
        }
    }
}