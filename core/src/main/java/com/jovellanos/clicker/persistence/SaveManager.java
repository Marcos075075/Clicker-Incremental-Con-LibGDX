package com.jovellanos.clicker.persistence;

import java.math.BigInteger;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.jovellanos.clicker.core.GameStateSnapshot;

/*
    ===============================================
    SaveManager — Serialización JSON
    ===============================================
    Escribe y lee el archivo de guardado usando la clase Json de LibGDX.
    Siempre trabaja sobre un GameStateSnapshot, nunca sobre GameState.

    ===============================================
    BigInteger y JSON
    ===============================================
    LibGDX Json no serializa BigInteger de forma nativa fiable.
    La solución es almacenar ppActual y ppHistorico como String:
    su representación decimal exacta sin pérdida de precisión.

    SaveData expone getPpActual() / getPpHistorico() que convierten
    el String de vuelta a BigInteger al cargar.

    Un valor corrupto o ausente se recupera como BigInteger.ZERO
    en lugar de lanzar excepción, para que el juego siempre arranque.

    Ruta del archivo: local storage de LibGDX
    (escritorio: carpeta del ejecutable / Android: almacenamiento interno).
*/
public class SaveManager {

    private static final String ARCHIVO_GUARDADO = "guardarpartida.json";

    // ── Guardar ───────────────────────────────────────────────────────────

    public void guardar(GameStateSnapshot snapshot) {
        try {
            Json json = new Json();
            // Obliga a usar el estándar JSON con comillas dobles (no es necesario, pero visualmente queda mejor)
            json.setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json);
            String   jsonStr = json.toJson(new SaveData(snapshot));
            FileHandle file  = Gdx.files.local(ARCHIVO_GUARDADO);
            file.writeString(jsonStr, false);
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error al guardar: " + e.getMessage());
        }
    }

    // ── Cargar ────────────────────────────────────────────────────────────

    public SaveData carga() {
        try {
            FileHandle file = Gdx.files.local(ARCHIVO_GUARDADO);
            if (!file.exists()) return null;
            Json json = new Json();
            json.setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json);
            return json.fromJson(SaveData.class, file.readString());
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error al cargar: " + e.getMessage());
            return null;
        }
    }

    public boolean saveExists() {
        return Gdx.files.local(ARCHIVO_GUARDADO).exists();
    }

    // ────────────────────────────────────────────────────────────────────
    // SaveData — DTO entre GameStateSnapshot y el JSON
    // ────────────────────────────────────────────────────────────────────

    public static class SaveData {

        /**
         * BigInteger serializado como String decimal.
         * Usar getPpActual() / getPpHistorico() para obtener BigInteger.
         */
        public String ppActualStr;
        public String ppHistoricoStr;

        public double              ppPorClick;
        public double              ppPorSegundo;
        public Map<String, Integer> mejorasAdquiridas;
        public String              idiomaActual;
        public long                ultimoGuardado;
        public int                 screenMode;

        /** Constructor vacío requerido por Json de LibGDX. */
        public SaveData() {}

        public SaveData(GameStateSnapshot s) {
            // BigInteger → String para serialización segura
            this.ppActualStr        = s.ppActual.toString();
            this.ppHistoricoStr     = s.ppHistorico.toString();
            this.ppPorClick         = s.ppPorClick;
            this.ppPorSegundo       = s.ppPorSegundo;
            this.mejorasAdquiridas  = s.mejorasAdquiridas;
            this.idiomaActual       = s.idiomaActual;
            this.ultimoGuardado     = s.ultimoGuardado;
            this.screenMode         = s.screenMode;
        }

        /**
         * Recupera ppActual como BigInteger.
         * Si el String es nulo o inválido devuelve ZERO (juego siempre arranca).
         */
        public BigInteger getPpActual() {
            return parseSafe(ppActualStr);
        }

        /**
         * Recupera ppHistorico como BigInteger.
         * Si el String es nulo o inválido devuelve ZERO.
         */
        public BigInteger getPpHistorico() {
            return parseSafe(ppHistoricoStr);
        }

        private static BigInteger parseSafe(String value) {
            if (value == null || value.isEmpty()) return BigInteger.ZERO;
            try {
                return new BigInteger(value);
            } catch (NumberFormatException e) {
                return BigInteger.ZERO;
            }
        }
    }
}