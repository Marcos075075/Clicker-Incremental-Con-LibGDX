package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.audio.AudioManager;
import com.jovellanos.clicker.core.ResourceManager;
import com.jovellanos.clicker.i18n.LocaleManager;

/*
    ===============================================
    Introducción
    ===============================================
    Pantalla con la narrativa que aparece al iniciar una nueva 
    partida, antes de entrar al juego.

    Flujo: MainMenuScreen -> IntroScreen -> GameScreen

    Implementación técnica:
    - Sistema de renderizado por capas utilizando Stack para 
      separar los fondos/personajes de la interfaz de texto.
    - Fondo de texto dinámico mediante Pixmap al 75% de opacidad para legibilidad.
    - Sistema dual de avance mediante clic en pantalla o tecla ENTER.
    - Transiciones suaves (Fade In/Out) gestionadas mediante Actions y bloqueos de estado.
*/

public class IntroScreen extends BaseScreen {

    private int currentScene = 1;
    private boolean isTransitioning = false;
    private boolean canType = true;

    private Stack bgStack;
    private Table uiLayer; 
    private Label dialogLabel;
    private Image blackOverlay;
    private Image blinkIcon;

    private String targetText = "";
    private float textTimer = 0f;
    private int textIndex = 0;

    private Texture textBgTexture;
    private Texture blackOverlayTexture;

    public IntroScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void buildUI() {
        Stack stack = new Stack();

        bgStack = new Stack();
        stack.add(bgStack);

        uiLayer = new Table();
        uiLayer.bottom();

        // Generación procedimental del fondo oscuro para la caja de diálogo
        Pixmap pixText = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixText.setColor(new Color(0f, 0f, 0f, 0.75f));
        pixText.fill();
        textBgTexture = new Texture(pixText);
        TextureRegionDrawable textBg = new TextureRegionDrawable(new TextureRegion(textBgTexture));
        pixText.dispose();

        Table textTable = new Table();
        textTable.setBackground(textBg);

        dialogLabel = new Label("", ResourceManager.getSkin(), "small");
        dialogLabel.setWrap(true);
        dialogLabel.setAlignment(Align.topLeft);

        // Icono de avance estandarizado. Inicia oculto hasta que el texto finalice su renderizado.
        blinkIcon = new Image(ResourceManager.iconoAvance);
        blinkIcon.setVisible(false);
        blinkIcon.addAction(Actions.forever(Actions.sequence(
            Actions.fadeOut(0.5f), 
            Actions.fadeIn(0.5f)
        )));

        textTable.add(dialogLabel).expand().fill().pad(30).padRight(10);
        textTable.add(blinkIcon).size(32).bottom().right().pad(15);

        uiLayer.add(textTable).fillX().expandX().height(220);
        stack.add(uiLayer);

        // Generación de capa superior de oclusión absoluta para gestionar transiciones limpias
        Pixmap pixBlack = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixBlack.setColor(Color.BLACK);
        pixBlack.fill();
        blackOverlayTexture = new Texture(pixBlack);
        blackOverlay = new Image(blackOverlayTexture);
        blackOverlay.getColor().a = 0f; 
        blackOverlay.setTouchable(Touchable.disabled);
        pixBlack.dispose();

        stack.add(blackOverlay);

        root.add(stack).expand().fill();

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                advanceScene();
                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ENTER || keycode == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
                    advanceScene();
                    return true;
                }
                return false;
            }
        });

        loadScene(currentScene);
    }

    /*
     * Controla la progresión de la cinemática.
     * Completa el texto instantáneamente si se detecta interacción antes de terminar.
     * Determina el flujo visual: avance instantáneo (escenas 1, 3 y 4) o transiciones 
     * con fundido a negro intermedio (salto de la escena 2 a la 3 y transición final al juego).
     */
    private void advanceScene() {
        if (!canType) return;

        if (textIndex < targetText.length()) {
            textIndex = targetText.length();
            dialogLabel.setText(targetText);
            if (currentScene != 5) {
                blinkIcon.setVisible(true);
            }
            return;
        }

        if (isTransitioning) return;

        if (currentScene == 2) {
            isTransitioning = true;
            canType = false;
            blackOverlay.addAction(Actions.sequence(
                Actions.fadeIn(1.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        currentScene++;
                        loadScene(currentScene);
                    }
                }),
                Actions.fadeOut(1.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        isTransitioning = false;
                        canType = true;
                    }
                })
            ));
        } else if (currentScene == 5) {
            isTransitioning = true;
            canType = false;
            blackOverlay.addAction(Actions.sequence(
                Actions.fadeIn(2.5f), 
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.changeScreen(ScreenType.GAME);
                    }
                })
            ));
        } else {
            currentScene++;
            loadScene(currentScene);
        }
    }

    /*
     * Carga y renderiza los elementos correspondientes a la escena actual.
     * Evalúa el número de escena mediante un bloque switch para asignar fondos, 
     * animaciones de temblor, escalas, posiciones de personajes y degradados de textura.
     */
    private void loadScene(int scene) {
        bgStack.clearChildren();
        LocaleManager i18n = LocaleManager.getInstance();

        Image fondoPrincipal = new Image();
        Image fondoTransicion = new Image(); 
        Table planetTable = new Table();
        Table charTable = new Table();
        charTable.bottom();

        bgStack.add(fondoPrincipal);
        bgStack.add(fondoTransicion);
        bgStack.add(planetTable);
        bgStack.add(charTable);

        if (scene == 5) {
            uiLayer.setVisible(false);
        }

        switch (scene) {
            case 1:
                // Se carga el fondo de la galaxia y el planeta inicial estático con efecto de escala
                fondoPrincipal.setDrawable(new TextureRegionDrawable(ResourceManager.fondoGalaxia));
                Image tierra1 = new Image(ResourceManager.iconoTierra1);
                tierra1.setScaling(Scaling.fit);
                tierra1.setOrigin(Align.center);
                tierra1.addAction(Actions.forever(Actions.sequence(
                    Actions.scaleTo(1.05f, 1.05f, 2f),
                    Actions.scaleTo(1f, 1f, 2f)
                )));
                planetTable.add(tierra1).size(300, 300).expand().center().padBottom(150);
                break;

            case 2:
                // Fondo interior nave estático; el degradado se aplica únicamente a las texturas del planeta
                fondoPrincipal.setDrawable(new TextureRegionDrawable(ResourceManager.fondoNave));

                Stack planetCrossfadeStack = new Stack();
                
                Image tierraAzul = new Image(ResourceManager.iconoTierra1);
                tierraAzul.setScaling(Scaling.fit);
                tierraAzul.addAction(Actions.sequence(Actions.fadeOut(7f), Actions.removeActor()));

                Image tierraMarron = new Image(ResourceManager.iconoTierra2);
                tierraMarron.setScaling(Scaling.fit);
                tierraMarron.getColor().a = 0f;
                tierraMarron.addAction(Actions.fadeIn(7f));

                planetCrossfadeStack.add(tierraAzul);
                planetCrossfadeStack.add(tierraMarron);
                
                Action temblorAction = Actions.forever(Actions.sequence(
                    Actions.moveBy(2, 2, 0.05f),
                    Actions.moveBy(-2, -2, 0.05f)
                ));
                
                fondoPrincipal.addAction(temblorAction);
                planetCrossfadeStack.addAction(temblorAction);

                planetTable.add(planetCrossfadeStack).size(300, 300).expand().center().padBottom(150);
                break;

            case 3:
            case 4:
                // El fondo de la nave pierde el temblor, el planeta toma un movimiento de inercia
                fondoPrincipal.setDrawable(new TextureRegionDrawable(ResourceManager.fondoNave));
                Image tierraBg = new Image(ResourceManager.iconoTierra2);
                tierraBg.setScaling(Scaling.fit);
                tierraBg.addAction(Actions.forever(Actions.sequence(
                    Actions.moveBy(0, 10, 3f),
                    Actions.moveBy(0, -10, 3f)
                )));
                planetTable.add(tierraBg).size(300, 300).expand().center().padBottom(150);
                
                Texture texMaia = (scene == 3) ? ResourceManager.maiaTablet : ResourceManager.maia1;
                Texture texMonito = (scene == 3) ? ResourceManager.monito1 : ResourceManager.monitoTablet;
                
                Image maia = new Image(texMaia);
                Image monito = new Image(texMonito);
                
                charTable.add(maia).expand().bottom().left().padLeft(80);
                charTable.add(monito).expand().bottom().right().padRight(80);
                break;
                
            case 5:
                // Se genera un pop-up de recompensa centrado y acotado
                fondoPrincipal.setDrawable(new TextureRegionDrawable(ResourceManager.fondoNave));
                Image tierraBg2 = new Image(ResourceManager.iconoTierra2);
                tierraBg2.setScaling(Scaling.fit);
                tierraBg2.addAction(Actions.forever(Actions.sequence(
                    Actions.moveBy(0, 10, 3f),
                    Actions.moveBy(0, -10, 3f)
                )));
                planetTable.add(tierraBg2).size(300, 300).expand().center().padBottom(150);
                
                Image maiaStatic = new Image(ResourceManager.maia1);
                Image monitoTabletFinal = new Image(ResourceManager.monitoTablet);
                
                charTable.add(maiaStatic).expand().bottom().left().padLeft(80);
                charTable.add(monitoTabletFinal).expand().bottom().right().padRight(80);
                
                Table popupWrapper = new Table();
                popupWrapper.setFillParent(true);
                popupWrapper.center();

                Table popupTable = new Table();
                Pixmap bgPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                bgPix.setColor(new Color(0.1f, 0.1f, 0.2f, 0.9f));
                bgPix.fill();
                Texture popupTex = new Texture(bgPix);
                bgPix.dispose();
                
                popupTable.setBackground(new TextureRegionDrawable(new TextureRegion(popupTex)));
                
                Label popupLabel = new Label(i18n.getText("intro_escena_5"), ResourceManager.getSkin());
                popupLabel.setAlignment(Align.center);
                
                Image continueIcon = new Image(ResourceManager.iconoAvance);
                continueIcon.addAction(Actions.forever(Actions.sequence(Actions.fadeOut(0.5f), Actions.fadeIn(0.5f))));

                popupTable.add(popupLabel).expand().center().padTop(20).padBottom(5).row();
                popupTable.add(continueIcon).size(24).padBottom(15);
                
                popupWrapper.add(popupTable).width(500);
                bgStack.add(popupWrapper);
                break;
        }

        targetText = i18n.getText("intro_escena_" + scene);
        if (targetText == null) targetText = "";
        
        textIndex = 0;
        textTimer = 0f;
        dialogLabel.setText("");
        blinkIcon.setVisible(false);
    }

    @Override
    public void render(float delta) {
        AudioManager.getInstance().playMusic(AudioManager.Track.INTRO);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        if (canType && textIndex < targetText.length()) {
            textTimer += delta;
            if (textTimer > 0.025f) { 
                textTimer = 0f;
                textIndex++;
                dialogLabel.setText(targetText.substring(0, textIndex));
                if (textIndex == targetText.length() && currentScene != 5) {
                    blinkIcon.setVisible(true);
                }
            }
        }
        
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (textBgTexture != null) textBgTexture.dispose();
        if (blackOverlayTexture != null) blackOverlayTexture.dispose();
    }
}