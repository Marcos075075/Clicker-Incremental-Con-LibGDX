package com.jovellanos.clicker.audio;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.jovellanos.clicker.audio.AudioManager;

public class UISounds {

    public static final InputListener HOVER = new InputListener() {
        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (fromActor == null) {
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.CLICKHOVER);
            }
        }
    };

    public static final InputListener CLICK = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            AudioManager.getInstance().playSound(AudioManager.SoundEffect.CLICKBUTTON);
            return false;
        }
    };

    public static final InputListener NUCLEO = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

            AudioManager.getInstance().playSoundWithPitch(
                    AudioManager.SoundEffect.NUCLEO,
                    0.95f,
                    1.05f);

            return false;
        }
    };

        public static final InputListener PURCHASE = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

            AudioManager.getInstance().playSoundWithPitch(
                    AudioManager.SoundEffect.PURCHASE,
                    0.95f,
                    1.05f);

            return false;
        }
    };
}