package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
    - Adaptación móvil (Android): Se implementa lógica responsiva para
      escalar textos, reposicionar personajes y cargar fondos específicos 
      cuando se ejecuta en dispositivos táctiles de orientación vertical.
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
    private TextButton btnSkip;

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

        // Detección del entorno de ejecución para aplicar diseño responsivo
        boolean isMobile = Gdx.app.getType() == Application.ApplicationType.Android;
        
        // Se definen variables de tamaño dinámicas para optimizar la visualización en formato móvil
        float textTableHeight = isMobile ? 600f : 220f;
        float iconSize = isMobile ? 80f : 32f;
        float fontScaleDialog = isMobile ? 2.8f : 1.0f;
        float fontScaleBtnSkip = isMobile ? 1.5f : 0.8f;

        // Implementación de botón para omitir la cinemática
        btnSkip = new TextButton(LocaleManager.getInstance().getText("boton_skip"), ResourceManager.getSkin());
        btnSkip.getLabel().setFontScale(fontScaleBtnSkip);
        // Se añade padding interno para aumentar el área de pulsación y la estética del botón
        btnSkip.pad(isMobile ? 20f : 10f, isMobile ? 40f : 20f, isMobile ? 20f : 10f, isMobile ? 40f : 20f);
        btnSkip.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.GAME);
            }
        });

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
        dialogLabel.setFontScale(fontScaleDialog); // Se aplica el escalado dinámico al texto

        // Icono de avance estandarizado. Inicia oculto hasta que el texto finalice su renderizado.
        blinkIcon = new Image(ResourceManager.iconoAvance);
        blinkIcon.setVisible(false);
        blinkIcon.addAction(Actions.forever(Actions.sequence(
            Actions.fadeOut(0.5f), 
            Actions.fadeIn(0.5f)
        )));

        textTable.add(dialogLabel).expand().fill().pad(isMobile ? 60 : 30).padRight(10);
        textTable.add(blinkIcon).size(iconSize).bottom().right().pad(isMobile ? 40 : 15);

        // Alineación del botón de salto sobre la caja de diálogo
        uiLayer.add(btnSkip).right().padRight(isMobile ? 30f : 20f).padBottom(isMobile ? 30f : 15f).row();
        uiLayer.add(textTable).fillX().expandX().height(textTableHeight);
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
                // Previene el avance del texto si se está pulsando el botón de salto
                if (event.getTarget().isDescendantOf(btnSkip)) {
                    return false;
                }
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

        // Se calculan las dimensiones y márgenes específicos requeridos por la vista vertical de Android
        boolean isMobile = Gdx.app.getType() == Application.ApplicationType.Android;
        float planetSize = isMobile ? 700f : 300f;
        float planetPadBottom = isMobile ? 450f : 150f;
        float charPadBottom = isMobile ? 500f : 0f;

        // Se establece el fondo correspondiente basándose en la plataforma
        TextureRegionDrawable bgGalaxia = new TextureRegionDrawable(isMobile ? ResourceManager.fondoGalaxiaandroid : ResourceManager.fondoGalaxia);
        TextureRegionDrawable bgNave = new TextureRegionDrawable(isMobile ? ResourceManager.fondoNaveandroid : ResourceManager.fondoNave);

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
            btnSkip.setVisible(false);
        }

        switch (scene) {
            case 1:
                // Se carga el fondo de la galaxia y el planeta inicial estático con efecto de escala
                fondoPrincipal.setDrawable(bgGalaxia);
                Image tierra1 = new Image(ResourceManager.iconoTierra1);
                tierra1.setScaling(Scaling.fit);
                tierra1.setOrigin(Align.center);
                tierra1.addAction(Actions.forever(Actions.sequence(
                    Actions.scaleTo(1.05f, 1.05f, 2f),
                    Actions.scaleTo(1f, 1f, 2f)
                )));
                planetTable.add(tierra1).size(planetSize, planetSize).expand().center().padBottom(planetPadBottom);
                break;

            case 2:
                // Fondo interior nave estático; el degradado se aplica únicamente a las texturas del planeta
                fondoPrincipal.setDrawable(bgNave);

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

                planetTable.add(planetCrossfadeStack).size(planetSize, planetSize).expand().center().padBottom(planetPadBottom);
                break;

            case 3:
            case 4:
                // El fondo de la nave pierde el temblor, el planeta toma un movimiento de inercia
                fondoPrincipal.setDrawable(bgNave);
                Image tierraBg = new Image(ResourceManager.iconoTierra2);
                tierraBg.setScaling(Scaling.fit);
                tierraBg.addAction(Actions.forever(Actions.sequence(
                    Actions.moveBy(0, 10, 3f),
                    Actions.moveBy(0, -10, 3f)
                )));
                planetTable.add(tierraBg).size(planetSize, planetSize).expand().center().padBottom(planetPadBottom);
                
                Texture texMaia = (scene == 3) ? ResourceManager.maiaTablet : ResourceManager.maia1;
                Texture texMonito = (scene == 3) ? ResourceManager.monito1 : ResourceManager.monitoTablet;
                
                Image maia = new Image(texMaia);
                Image monito = new Image(texMonito);
                
                charTable.add(maia).expand().bottom().left().padLeft(isMobile ? 40 : 80).padBottom(charPadBottom);
                charTable.add(monito).expand().bottom().right().padRight(isMobile ? 40 : 80).padBottom(charPadBottom);
                break;
                
            case 5:
                // Se genera un pop-up de recompensa centrado y acotado
                fondoPrincipal.setDrawable(bgNave);
                Image tierraBg2 = new Image(ResourceManager.iconoTierra2);
                tierraBg2.setScaling(Scaling.fit);
                tierraBg2.addAction(Actions.forever(Actions.sequence(
                    Actions.moveBy(0, 10, 3f),
                    Actions.moveBy(0, -10, 3f)
                )));
                planetTable.add(tierraBg2).size(planetSize, planetSize).expand().center().padBottom(planetPadBottom);
                
                Image maiaStatic = new Image(ResourceManager.maia1);
                Image monitoTabletFinal = new Image(ResourceManager.monitoTablet);
                
                charTable.add(maiaStatic).expand().bottom().left().padLeft(isMobile ? 40 : 80).padBottom(charPadBottom);
                charTable.add(monitoTabletFinal).expand().bottom().right().padRight(isMobile ? 40 : 80).padBottom(charPadBottom);
                
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
                popupLabel.setFontScale(isMobile ? 2.3f : 1.0f);
                
                Image continueIcon = new Image(ResourceManager.iconoAvance);
                continueIcon.addAction(Actions.forever(Actions.sequence(Actions.fadeOut(0.5f), Actions.fadeIn(0.5f))));

                popupTable.add(popupLabel).expand().center().padTop(isMobile ? 60 : 20).padBottom(15).row();
                popupTable.add(continueIcon).size(isMobile ? 64 : 24).padBottom(isMobile ? 40 : 15);
                
                popupWrapper.add(popupTable).width(isMobile ? 900 : 500);
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