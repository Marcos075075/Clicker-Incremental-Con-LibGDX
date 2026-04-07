package com.jovellanos.clicker.persistence;

import java.util.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.jovellanos.clicker.core.GameStateSnapshot;

/*
    ===============================================
    SaveManager — Serialización JSON
    ===============================================
    Escribe y lee el archivo de guardado usando la
    clase Json de LibGDX. Siempre trabaja sobre un
    GameStateSnapshot, nunca sobre el GameState directamente.

    Ruta del archivo: local storage de LibGDX
    (en escritorio: carpeta del ejecutable)
*/

public class SaveManager {

    private static final String ARCHIVO_GUARDADO = "guardarpartida.json";

    // Guardar
    public void guardar(GameStateSnapshot snapshot) {
        try {
            Json json = new Json();
            // Obliga a usar el estándar JSON con comillas dobles (no es necesario, pero asi no salen errores)
            json.setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json);

            SaveData dataActual = new SaveData(snapshot);
            String jsonStr = json.toJson(dataActual);
            FileHandle file = Gdx.files.local(ARCHIVO_GUARDADO);
            file.writeString(jsonStr, false); // false sobrescribe el anterior archivo de guardado

        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error al guardar: " + e.getMessage());
        }
    }

    // Cargar
    public SaveData carga() {
        try {
            FileHandle file = Gdx.files.local(ARCHIVO_GUARDADO);
            if (!file.exists()) {
                return null;
            }

            Json json = new Json();
            json.setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json);

            return json.fromJson(SaveData.class, file.readString());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean saveExists() {
        return Gdx.files.local(ARCHIVO_GUARDADO).exists();
    }

    public static class SaveData { // Sirve de intermediario entre el GameStateSnapshot y el Json
        public long ppActual;
        public long ppHistorico;
        public double ppPorClick;
        public double ppPorSegundo;
        public Map<String, Integer> mejorasAdquiridas;
        public String idiomaActual;
        public long ultimoGuardado;

        // Constructor vacío requerido por Json de LibGDX
        public SaveData() {
        }

        public SaveData(GameStateSnapshot s) {
            this.ppActual = s.ppActual;
            this.ppHistorico = s.ppHistorico;
            this.ppPorClick = s.ppPorClick;
            this.ppPorSegundo = s.ppPorSegundo;
            this.mejorasAdquiridas = s.mejorasAdquiridas;
            this.idiomaActual = s.idiomaActual;
            this.ultimoGuardado = s.ultimoGuardado;
        }
    }

}
