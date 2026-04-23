package com.jovellanos.clicker.upgrades;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
    ===============================================
    UpgradeFactory — Fábrica de Mejoras
    ===============================================
    Centraliza la definición y creación de TODAS las mejoras del juego.
    Ninguna otra clase crea instancias de Upgrade directamente;
    todas las obtienen a través de esta fábrica.

    ===============================================
    Por qué un LinkedHashMap
    ===============================================
    - Acceso O(1) por id (necesario para SaveManager y el Logic Thread).
    - Mantiene el orden de inserción (orden de aparición en la tienda).

    ===============================================
    Cómo añadir una mejora nueva
    ===============================================
    1. Decide su tipo (Direct / Automated / Multiplier).
    2. Elige un id único en camelCase (es la clave del JSON).
    3. Añade la clave de nombre y descripción en textos.properties.
    4. Registra la mejora en el método build() de esta clase.

    ===============================================
    Escalado de costes (referencia)
    ===============================================
    Todas las mejoras usan costMultiplier = 1.15 por defecto,
    el estándar del género clicker. Ajusta baseCost para calibrar
    el ritmo de progresión.

    ===============================================
    Balanceo inicial
    ===============================================

    DIRECTAS
    ─────────────────────────────────────────────
    MejoraCursor          10 PP   +1 PP/clic
    CursorAvanzado       100 PP   +5 PP/clic

    AUTOMATIZADAS
    ─────────────────────────────────────────────
    GranjaDeServidores    50 PP   +0.5 PP/s
    CentroDeProcesado    500 PP   +4 PP/s
    SupercomputadorAtlas 5000 PP  +25 PP/s

    MULTIPLICADORAS (pueden apuntar a Direct o Automated)
    ─────────────────────────────────────────────
    MultiCursor          200 PP   ×1.10/ud → MejoraCursor
    MultiNucleo          500 PP   ×1.05/ud → GranjaDeServidores
    MultiCentro         2500 PP   ×1.08/ud → CentroDeProcesado
*/

public class UpgradeFactory {

    /**
     * Crea todas las mejoras del juego y las devuelve indexadas por id.
     * El GameState llama a este método una única vez al iniciar una
     * partida nueva, antes de que SaveManager restaure las cantidades.
     *
     * @return  Mapa ordenado id → Upgrade con todas las mejoras del juego.
     */
    public static Map<String, Upgrade> build() {
        Map<String, Upgrade> upgrades = new LinkedHashMap<String, Upgrade>();

        //TODO Migrar los register a un paquete que lea para que no se haga gigante esta parte del código.
        // ── Mejoras Directas ──────────────────────────────────────────────
        register(upgrades, new DirectUpgrade("Hook1", "mejora_cursor_lv1", "mejora_cursor_lv1_desc", 15, 1.15, 1.0));
        register(upgrades, new DirectUpgrade("Hook2", "mejora_cursor_lv2", "mejora_cursor_lv2_desc", 100, 1.15, 3.0));
        register(upgrades, new DirectUpgrade("Hook3", "mejora_cursor_lv3", "mejora_cursor_lv3_desc", 500, 1.15, 10.0));
        register(upgrades, new DirectUpgrade("Hook4", "mejora_cursor_lv4", "mejora_cursor_lv4_desc", 2000, 1.15, 35.0));
        register(upgrades, new DirectUpgrade("Hook5", "mejora_cursor_lv5", "mejora_cursor_lv5_desc", 7500, 1.15, 100.0));
        register(upgrades, new DirectUpgrade("Hook6", "mejora_cursor_lv6", "mejora_cursor_lv6_desc", 25000, 1.15, 300.0));
        register(upgrades, new DirectUpgrade("Hook7", "mejora_cursor_lv7", "mejora_cursor_lv7_desc", 100000, 1.15, 1000.0));
        register(upgrades, new DirectUpgrade("Hook8", "mejora_cursor_lv8", "mejora_cursor_lv8_desc", 400000, 1.15, 3500.0));
        register(upgrades, new DirectUpgrade("Hook9", "mejora_cursor_lv9", "mejora_cursor_lv9_desc", 1500000, 1.15, 10000.0));
        register(upgrades, new DirectUpgrade("Hook10", "mejora_cursor_lv10", "mejora_cursor_lv10_desc", 7500000, 1.15, 35000.0));
        register(upgrades, new DirectUpgrade("Hook11", "mejora_cursor_lv11", "mejora_cursor_lv11_desc", 35000000, 1.15, 120000.0));
        register(upgrades, new DirectUpgrade("Hook12", "mejora_cursor_lv12", "mejora_cursor_lv12_desc", 150000000, 1.15, 450000.0));
        register(upgrades, new DirectUpgrade("Hook13", "mejora_cursor_lv13", "mejora_cursor_lv13_desc", 750000000, 1.15, 2000000.0));
        register(upgrades, new DirectUpgrade("Hook14", "mejora_cursor_lv14", "mejora_cursor_lv14_desc", 3500000000.0, 1.15, 8000000.0));
        register(upgrades, new DirectUpgrade("Hook15", "mejora_cursor_lv15", "mejora_cursor_lv15_desc", 15000000000.0, 1.15, 30000000.0));

        // ── Mejoras Automatizadas ─────────────────────────────────────────
        register(upgrades, new AutomatedUpgrade("Router", "mejora_estructura_RouterModificado", "mejora_estructura_RouterModificado_desc", 50, 1.15, 1.0));
        register(upgrades, new AutomatedUpgrade("DataCenter", "mejora_estructura_datacenter", "mejora_estructura_datacenter_desc", 400, 1.15, 5.0));
        register(upgrades, new AutomatedUpgrade("Cloud", "mejora_estructura_nube", "mejora_estructura_nube_desc", 2500, 1.15, 20.0));
        register(upgrades, new AutomatedUpgrade("Satellite", "mejora_estructura_satelite", "mejora_estructura_satelite_desc", 12000, 1.15, 80.0));
        register(upgrades, new AutomatedUpgrade("AI", "mejora_estructura_ia", "mejora_estructura_ia_desc", 50000, 1.15, 300.0));
        register(upgrades, new AutomatedUpgrade("Relay", "estructura_relay_cuantico", "mejora_estructura_relay_cuantico_desc", 250000, 1.15, 1200.0));
        register(upgrades, new AutomatedUpgrade("Station", "mejora_estructura_estacion", "mejora_estructura_estacion_desc", 1000000, 1.15, 5000.0));
        register(upgrades, new AutomatedUpgrade("Crypto", "mejora_estructura_cripto", "mejora_estructura_cripto_desc", 5000000, 1.15, 20000.0));
        register(upgrades, new AutomatedUpgrade("Collider", "mejora_estructura_colisionador", "mejora_estructura_colisionador_desc", 25000000, 1.15, 80000.0));
        register(upgrades, new AutomatedUpgrade("Warp", "mejora_estructura_motor", "mejora_estructura_motor_desc", 120000000, 1.15, 350000.0));
        register(upgrades, new AutomatedUpgrade("Dyson", "mejora_estructura_dyson", "mejora_estructura_dyson_desc", 600000000, 1.15, 1500000.0));
        register(upgrades, new AutomatedUpgrade("StellarCore", "mejora_estructura_nucleo", "mejora_estructura_nucleo_desc", 3000000000.0, 1.15, 6500000.0));
        register(upgrades, new AutomatedUpgrade("BlackHole", "mejora_estructura_agujero", "mejora_estructura_agujero_desc", 15000000000.0, 1.15, 30000000.0));
        register(upgrades, new AutomatedUpgrade("Matrix", "mejora_estructura_conciencia", "mejora_estructura_conciencia_desc", 80000000000.0, 1.15, 150000000.0));
        register(upgrades, new AutomatedUpgrade("Eye", "mejora_estructura_ojo", "mejora_estructura_ojo_desc", 400000000000.0, 1.15, 800000000.0));

        // ── Mejoras Multiplicadoras ───────────────────────────────────────
/* register(upgrades, new MultiplierUpgrade(
            "MultiCursor",
            "mejora_multiplicador_cursor",
            "mejora_multiplicador_cursor_desc",
            200,
            1.15,
            "MejoraCursor",         // apunta a una DirectUpgrade
            0.10                    // +10 % por unidad
        ));

        register(upgrades, new MultiplierUpgrade(
            "MultiNucleo",
            "mejora_multiplicador_nucleo",
            "mejora_multiplicador_nucleo_desc",
            500,
            1.15,
            "GranjaDeServidores",   // apunta a una AutomatedUpgrade
            0.05                    // +5 % por unidad
        ));

        register(upgrades, new MultiplierUpgrade(
            "MultiCentro",
            "mejora_multiplicador_centro",
            "mejora_multiplicador_centro_desc",
            2500,
            1.15,
            "CentroDeProcesado",    // apunta a una AutomatedUpgrade
            0.08                    // +8 % por unidad
        )); */

        return upgrades;
    }

    // -------------------------------------------------------
    // Helpers de consulta (para el Logic Thread y la UI)
    // -------------------------------------------------------

    /**
     * Devuelve la lista de todas las DirectUpgrade registradas.
     * Usada por calcularPPporClick() y el GameState para obtener
     * la contribución de cada mejora directa.
     */
    public static List<DirectUpgrade> getDirectUpgrades(Map<String, Upgrade> upgrades) {
        List<DirectUpgrade> result = new ArrayList<DirectUpgrade>();
        for (Upgrade u : upgrades.values()) {
            if (u instanceof DirectUpgrade) {
                result.add((DirectUpgrade) u);
            }
        }
        return result;
    }

    /**
     * Devuelve la lista de todas las AutomatedUpgrade registradas.
     * Usada por el Logic Thread para calcular PPporSegundo total.
     */
     public static List<AutomatedUpgrade> getAutomatedUpgrades(Map<String, Upgrade> upgrades) {
        List<AutomatedUpgrade> result = new ArrayList<AutomatedUpgrade>();
        for (Upgrade u : upgrades.values()) {
            if (u instanceof AutomatedUpgrade) {
                result.add((AutomatedUpgrade) u);
            }
        }
        return result;
    }

    /**
     * Devuelve el multiplicador total que aplica a una mejora concreta
     * (Direct o Automated), sumando la contribución de todos los
     * MultiplierUpgrade que apuntan a ese id.
     *
     * @param targetId  Id de la mejora objetivo.
     * @param upgrades  Mapa completo de mejoras.
     * @return          Factor ≥ 1.0 a pasar a getPPperClick() o getPPperSecond().
     */
    public static double getMultiplierFor(String targetId, Map<String, Upgrade> upgrades) {
        double multiplier = 1.0;
        for (Upgrade u : upgrades.values()) {
            if (u instanceof MultiplierUpgrade) {
                MultiplierUpgrade mu = (MultiplierUpgrade) u;
                if (mu.getTargetUpgradeId().equals(targetId)) {
                    // getTotalMultiplier() ya incluye el 1.0 base;
                    // encadenamos multiplicadores de forma aditiva:
                    // mult = 1.0 + suma(multiplierPerUnit * quantity)
                    multiplier += mu.getTotalMultiplier() - 1.0;
                }
            }
        }
        return multiplier;
    }

    /**
     * Calcula el total de PP por clic a partir de las DirectUpgrade activas
     * y sus MultiplierUpgrade asociados. Incluye siempre el PP base (1.0).
     *
     * Extraído de GameState.recalculatePPperClick() para que GameState no
     * contenga lógica de negocio ni dependa de UpgradeFactory en sus métodos.
     * Llamado por PurchaseService tras cada compra, y por GameState al cargar
     * una partida guardada.
     *
     * @param upgrades  Mapa completo de mejoras del GameState.
     * @return          PP totales por clic (mínimo 1.0).
     */
    public static double calcularPPporClick(Map<String, Upgrade> upgrades) {
        double total = 1.0; // base: 1 PP por clic sin mejoras
        for (DirectUpgrade du : getDirectUpgrades(upgrades)) {
            double mult = getMultiplierFor(du.getId(), upgrades);
            total += du.getPPperClick(mult);
        }
        return total;
    }

    // -------------------------------------------------------
    // Privado
    // -------------------------------------------------------

    private static void register(Map<String, Upgrade> map, Upgrade upgrade) {
        map.put(upgrade.getId(), upgrade);
    }
}