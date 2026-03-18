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
 
        // ── Mejoras Directas ──────────────────────────────────────────────
        register(upgrades, new DirectUpgrade(
            "MejoraCursorLv1",
            "mejora_cursor_lv1",
            "mejora_cursor_lv1_desc",
            10,
            1.15,
            1.0
        ));
 
        register(upgrades, new DirectUpgrade(
            "MejoraCursorLv2",
            "mejora_cursor_lv2",
            "mejora_cursor_lv2_desc",
            100,
            1.15,
            2.0
        ));
 
         // ── Mejoras Automatizadas ─────────────────────────────────────────
        register(upgrades, new AutomatedUpgrade(
            "RouterModificado",
            "mejora_estructura_RouterModificado",
            "mejora_estructura_basica_desc",
            50,
            1.15,
            1
        ));

                register(upgrades, new AutomatedUpgrade(
            "DataCenter",
            "mejora_estructura_datacenter",
            "mejora_estructura_datacenter_desc",
            500,
            1.15,
            8.0
        ));

        register(upgrades, new AutomatedUpgrade(
            "SateliteOrbital",
            "mejora_estructura_satelite",
            "mejora_estructura_satelite_desc",
            5000,
            1.15,
            47.0
        ));

        register(upgrades, new AutomatedUpgrade(
            "RelayCuántico",
            "estructura_relay_cuantico",
            "mejora_estructura_relay_cuantico_desc",
            50000,
            1.15,
            260.0
        ));
 
/*         register(upgrades, new AutomatedUpgrade(
            "CentroDeProcesado",
            "mejora_estructura_media",
            "mejora_estructura_media_desc",
            500,
            1.15,
            4.0
        ));
 
        register(upgrades, new AutomatedUpgrade(
            "SupercomputadorAtlas",
            "mejora_estructura_avanzada",
            "mejora_estructura_avanzada_desc",
            5000,
            1.15,
            25.0
        ));  */
 
        // ── Mejoras Multiplicadoras ───────────────────────────────────────
/*         register(upgrades, new MultiplierUpgrade(
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
     * Usada por el GameState para recalcular PPporClick.
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
 
    // -------------------------------------------------------
    // Privado
    // -------------------------------------------------------
 
    private static void register(Map<String, Upgrade> map, Upgrade upgrade) {
        map.put(upgrade.getId(), upgrade);
    }
}