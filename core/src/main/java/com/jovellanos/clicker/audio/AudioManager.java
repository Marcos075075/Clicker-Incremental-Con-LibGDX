package com.jovellanos.clicker.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/*
    ===============================================
    AudioManager — Controlador Central de Audio
    ===============================================
    Singleton que gestiona toda la reproducción de música
    y efectos de sonido del juego.

    ===============================================
    Integración en pantallas (una sola línea)
    ===============================================
    En el show() o buildUI() de cada pantalla:
        AudioManager.getInstance().playMusic(Track.MENU);

    Para efectos de sonido (ej. clic del núcleo):
        AudioManager.getInstance().playSound(SoundEffect.CLICK);

    ===============================================
    Persistencia de volumen
    ===============================================
    Los volúmenes se guardan en LibGDX Preferences
    ("AtlasMonoAudio"), independiente del savegame de partida.
    Se cargan automáticamente al crear el AudioManager.

    ===============================================
    Resiliencia ante archivos ausentes
    ===============================================
    Todos los accesos a audio están envueltos en try-catch.
    Si un archivo no existe el juego arranca igualmente,
    simplemente sin ese elemento de audio.

    ===============================================
    Rutas de assets esperadas
    ===============================================
    audio/music/menu.ogg       → menú principal
    audio/music/game.ogg       → pantalla de juego
    audio/music/intro.ogg      → cinemática de introducción
    audio/sfx/click.ogg        → clic sobre el núcleo
    audio/sfx/purchase.ogg     → compra exitosa en la tienda
    audio/sfx/error.ogg        → fondos insuficientes

    ===============================================
    Añadir nuevos tracks o efectos
    ===============================================
    1. Añade la constante al enum Track o SoundEffect.
    2. Añade la ruta en loadMusic() o loadSounds().
    3. Llama a playMusic(Track.NUEVO) o playSound(SoundEffect.NUEVO).
*/
public class AudioManager {

    // ── Preferencias de persistencia ────────────────────────────────────
    private static final String PREFS_NAME          = "AtlasMonoAudio";
    private static final String KEY_MUSIC_VOLUME    = "musicVolume";
    private static final String KEY_SFX_VOLUME      = "sfxVolume";
    private static final float  DEFAULT_MUSIC_VOLUME = 0.5f;
    private static final float  DEFAULT_SFX_VOLUME   = 0.7f;

    // ── Singleton ────────────────────────────────────────────────────────
    private static AudioManager instance;

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    // ── Enums públicos ───────────────────────────────────────────────────

    /** Tracks de música de fondo disponibles. */
    public enum Track {
        MENU,
        GAME,
        INTRO
    }

    /** Efectos de sonido cortos disponibles. */
    public enum SoundEffect {
        CLICKHOVER,
        CLICKBUTTON,
        NUCLEO,
        PURCHASE,
        ERROR
    }

    // ── Estado interno ───────────────────────────────────────────────────
    private float musicVolume;
    private float sfxVolume;

    private Music currentMusic;
    private Track currentTrack;

    // Música (streaming desde disco para no saturar memoria)
    private Music musicMenu;
    private Music musicGame;
    private Music musicIntro;

    // Efectos (cargados completos en memoria)
    private Sound soundClickHover;
    private Sound soundClickButton;
    private Sound soundNucleo;
    private Sound soundPurchase;
    private Sound soundError;

    // ── Constructor privado ──────────────────────────────────────────────

    private AudioManager() {
        loadPreferences();
        loadMusic();
        loadSounds();
    }

    // ── Carga de recursos ────────────────────────────────────────────────

    private void loadPreferences() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        musicVolume = prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
        sfxVolume   = prefs.getFloat(KEY_SFX_VOLUME,   DEFAULT_SFX_VOLUME);
    }

    private void loadMusic() {
        musicMenu  = loadMusicSafe("audio/music/menu.ogg");
        musicGame  = loadMusicSafe("audio/music/game.ogg");
        musicIntro = loadMusicSafe("audio/music/intro.ogg");
    }

    private void loadSounds() {
        soundClickHover    = loadSoundSafe("audio/sfx/clickHover.ogg");
        soundClickButton   = loadSoundSafe("audio/sfx/clickButton.ogg");
        soundNucleo        = loadSoundSafe("audio/sfx/Coffee2.ogg");
        soundPurchase = loadSoundSafe("audio/sfx/Modern13.ogg");
        soundError    = loadSoundSafe("audio/sfx/Abstract2.ogg");
    }

    private Music loadMusicSafe(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                return Gdx.audio.newMusic(Gdx.files.internal(path));
            }
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "No se pudo cargar música: " + path);
        }
        return null;
    }

    private Sound loadSoundSafe(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                return Gdx.audio.newSound(Gdx.files.internal(path));
            }
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "No se pudo cargar sonido: " + path);
        }
        return null;
    }

    // ── API pública: Música ──────────────────────────────────────────────

    /**
     * Reproduce el track indicado en bucle. Si ya está sonando ese mismo
     * track no hace nada (evita reiniciar la música al navegar entre
     * pantallas que comparten track).
     *
     * @param track  Track de música a reproducir.
     */
    public void playMusic(Track track) {
        if (track == currentTrack && currentMusic != null && currentMusic.isPlaying()) {
            return; // Ya está sonando, no interrumpir
        }

        stopMusic();
        currentTrack = track;

        switch (track) {
            case MENU:  currentMusic = musicMenu;  break;
            case GAME:  currentMusic = musicGame;  break;
            case INTRO: currentMusic = musicIntro; break;
            default:    currentMusic = null;       break;
        }

        if (currentMusic != null) {
            currentMusic.setLooping(true);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        }
    }

    /** Pausa la música actual sin perder la posición. */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    /** Reanuda la música pausada. */
    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    /** Detiene la música actual y olvida el track en curso. */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = null;
        currentTrack = null;
    }

    // ── API pública: Efectos de sonido ───────────────────────────────────

    /**
     * Reproduce un efecto de sonido puntual.
     * Si el archivo no estaba disponible al cargar, no hace nada.
     *
     * @param effect  Efecto de sonido a reproducir.
     */
    public void playSound(SoundEffect effect) {
        Sound sound = null;
        switch (effect) {
            case CLICKHOVER:    sound = soundClickHover;    break;
            case CLICKBUTTON:   sound = soundClickButton;   break;
            case NUCLEO:        sound = soundNucleo;        break;
            case PURCHASE: sound = soundPurchase; break;
            case ERROR:    sound = soundError;    break;
        }
        if (sound != null) {
            sound.play(sfxVolume);
        }
    }

    public void playSoundWithPitch(SoundEffect effect, float minPitch, float maxPitch) {
        Sound sound = null;

        switch (effect) {
            case NUCLEO:
                sound = soundNucleo;
                break;
            case PURCHASE:
                sound = soundPurchase;
                break;
        }

        if (sound != null) {
            float pitch = minPitch + (float) Math.random() * (maxPitch - minPitch);
            sound.play(sfxVolume, pitch, 0f);
        }
}

    // ── API pública: Volúmenes ───────────────────────────────────────────

    /**
     * Ajusta el volumen de la música en tiempo real (0.0 – 1.0).
     * El valor se persiste en Preferences automáticamente.
     */
    public void setMusicVolume(float volume) {
        musicVolume = clamp(volume);
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
        savePreferences();
    }

    /**
     * Ajusta el volumen de los efectos de sonido (0.0 – 1.0).
     * El valor se persiste en Preferences automáticamente.
     */
    public void setSfxVolume(float volume) {
        sfxVolume = clamp(volume);
        savePreferences();
    }

    public float getMusicVolume() { return musicVolume; }
    public float getSfxVolume()   { return sfxVolume;   }

    // ── Persistencia de volumen ──────────────────────────────────────────

    private void savePreferences() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putFloat(KEY_MUSIC_VOLUME, musicVolume);
        prefs.putFloat(KEY_SFX_VOLUME,   sfxVolume);
        prefs.flush();
    }

    // ── Ciclo de vida ────────────────────────────────────────────────────

    /**
     * Libera todos los recursos de audio. Llamado desde MainGame.dispose().
     * Resetea la instancia para que el siguiente create() empiece limpio.
     */
    public void dispose() {
        stopMusic();
        disposeMusic(musicMenu);
        disposeMusic(musicGame);
        disposeMusic(musicIntro);
        disposeSound(soundClickHover);
        disposeSound(soundPurchase);
        disposeSound(soundError);
        instance = null;
    }

    private void disposeMusic(Music m) { if (m != null) m.dispose(); }
    private void disposeSound(Sound  s) { if (s != null) s.dispose(); }

    // ── Helpers ──────────────────────────────────────────────────────────

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}