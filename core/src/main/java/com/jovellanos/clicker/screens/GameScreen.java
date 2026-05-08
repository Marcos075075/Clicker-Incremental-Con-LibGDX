package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.audio.AudioManager;
import com.jovellanos.clicker.audio.UISounds;
import com.jovellanos.clicker.core.PPFormatter;
import com.jovellanos.clicker.core.ResourceManager;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.jovellanos.clicker.logic.PurchaseService;
import com.jovellanos.clicker.persistence.OfflineProgressCalc;
import com.jovellanos.clicker.upgrades.AutomatedUpgrade;
import com.jovellanos.clicker.upgrades.DirectUpgrade;
import com.jovellanos.clicker.upgrades.MultiplierUpgrade;
import com.jovellanos.clicker.upgrades.Upgrade;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    ===============================================
    Juego Principal
    ===============================================
    Pantalla central del juego. Layout de 3 columnas según A1.1.

    ===============================================
    BigInteger y PPFormatter
    ===============================================
    GameState.getPpActual() ahora devuelve BigInteger. Para mostrarlo
    en el HUD se usa PPFormatter.format(), que produce cadenas legibles
    como "1,23M" o "456,7B" en lugar del toString() completo.

    El coste de las mejoras en la tienda también pasa por PPFormatter
    para que sea coherente con el contador principal.

    ===============================================
    Compra de mejoras
    ===============================================
    Las compras se delegan a PurchaseService.comprar(id, gameState).
    canAfford(BigInteger) en Upgrade gestiona la comparación correcta.
*/
public class GameScreen extends BaseScreen {

    private Label labelPP;
    private Label labelPPS;

    private final Map<String, Table> shopCards = new HashMap<String, Table>();
    private final Map<String, Label> shopCostLabels = new HashMap<String, Label>();

    private final Map<String, Button> shopBuyButtons = new HashMap<String, Button>();
    private final Map<String, Label> shopQuantityLabels = new HashMap<String, Label>();

    private LocaleManager i18n;
    private PurchaseService purchaseService;

    private Table colEstructuras;
    private Table colTienda;
    
    private BigInteger offlineGains = BigInteger.ZERO;

    public GameScreen(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        offlineGains = OfflineProgressCalc.procesar(game.getGameState());
        AudioManager.getInstance().playMusic(AudioManager.Track.GAME);
        purchaseService = game.getPurchaseService();
        super.show();
    }

    @Override
    protected void buildUI() {
        this.i18n = LocaleManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        root.setBackground(new TextureRegionDrawable(new TextureRegion(ResourceManager.fondoJuego)));

        Table hud = new Table();
        TextButton btnAjustes = new TextButton(i18n.getText("menu_ajustes"), skin);
        hud.add(btnAjustes).right().padRight(16).width(150).height(50).expandX();
        root.add(hud).fillX().height(60).row();

        Table colIzquierda = new Table();
        colIzquierda.top();

        labelPP = new Label("0 PP", skin);
        labelPPS = new Label("0 PP/seg", skin);
        colIzquierda.add(labelPP).center().padTop(8).row();
        colIzquierda.add(labelPPS).center().padBottom(16).row();

        Image btnNucleo = new Image(ResourceManager.texturaNucleo);

        btnNucleo.setSize(320, 320);
        btnNucleo.setOrigin(Align.center);

        btnNucleo.addAction(Actions.forever(Actions.sequence(
                Actions.scaleTo(1.1f, 1.1f, 1.5f),
                Actions.scaleTo(1.0f, 1.0f, 1.5f))));

        Label lblNombre = new Label(i18n.getText("juego_nombre_nucleo"), skin);
        Label lblZona = new Label(i18n.getText("juego_zona_activa"), skin);

        colIzquierda.add(btnNucleo).size(320, 320).padBottom(80).row();
        colIzquierda.add(lblNombre).padBottom(8).row();
        colIzquierda.add(lblZona).row();

        Table colCentro = new Table();
        colCentro.top();

        Label lblEstTitulo = new Label(i18n.getText("estructuras_titulo"), skin);
        lblEstTitulo.setFontScale(1.2f);
        colCentro.add(lblEstTitulo).center().padTop(8).padBottom(16).row();

        colEstructuras = new Table();
        colEstructuras.top();

        final ScrollPane scrollEst = new ScrollPane(colEstructuras);
        scrollEst.setFadeScrollBars(false);
        scrollEst.setScrollingDisabled(true, false);
        scrollEst.addListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollEst);
            }
        });
        colCentro.add(scrollEst).expand().fill().row();

        Table colDerecha = new Table();
        colDerecha.top();

        Label lblTiendaTitulo = new Label(i18n.getText("tienda_titulo"), skin);
        lblTiendaTitulo.setFontScale(1.2f);
        colDerecha.add(lblTiendaTitulo).center().padTop(8).padBottom(16).row();

        colTienda = new Table();
        colTienda.top();

        final ScrollPane scrollTienda = new ScrollPane(colTienda);
        scrollTienda.setFadeScrollBars(false);
        scrollTienda.setScrollingDisabled(true, false);
        scrollTienda.addListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollTienda);
            }
        });
        colDerecha.add(scrollTienda).expand().fill().row();

        Table mainTable = new Table();
        mainTable.add(colIzquierda).expandX().fillX().expandY().fillY().uniform().top();
        mainTable.add(colCentro).expandX().fillX().expandY().fillY().uniform().top();
        mainTable.add(colDerecha).expandX().fillX().expandY().fillY().uniform().top();
        root.add(mainTable).expand().fill();

        btnNucleo.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.getGameState().addPendingClick();

                double valorClickDouble = game.getGameState().getPpPorClick();
                BigInteger valorClickBig = BigInteger.valueOf((long) valorClickDouble);

                Label lblFloating = new Label("+" + PPFormatter.format(valorClickBig), ResourceManager.getSkin());
                lblFloating.setFontScale(0.8f);

                Vector2 coords = new Vector2(x, y);
                btnNucleo.localToStageCoordinates(coords);

                float offsetX = MathUtils.random() * 40f - 20f;
                lblFloating.setPosition(coords.x + offsetX, coords.y);

                lblFloating.addAction(Actions.sequence(
                        Actions.parallel(
                                Actions.moveBy(0, 70f, 0.8f),
                                Actions.sequence(
                                        Actions.delay(0.3f),
                                        Actions.fadeOut(0.5f))),
                        Actions.removeActor()));
                stage.addActor(lblFloating);

                return true;
            }
        });

        btnAjustes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game, true));
            }
        });

        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    game.setScreen(new SettingsScreen(game, true));
                    return true;
                }
                return false;
            }
        });

        btnAjustes.addListener(UISounds.HOVER);

        btnAjustes.addListener(UISounds.CLICK);
        btnNucleo.addListener(UISounds.NUCLEO);
        
        if (offlineGains.compareTo(BigInteger.ZERO) > 0) {
            final Table overlay = new Table();
            overlay.setFillParent(true);
            overlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            
            Pixmap pixDark = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixDark.setColor(new Color(0f, 0f, 0f, 0.75f));
            pixDark.fill();
            Texture darkeningTexture = new Texture(pixDark);
            pixDark.dispose();
            
            overlay.setBackground(new TextureRegionDrawable(new TextureRegion(darkeningTexture)));
            
            Table popup = new Table();
            Pixmap pixPopup = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixPopup.setColor(new Color(0.15f, 0.1f, 0.25f, 0.95f));
            pixPopup.fill();
            Texture popupBgTexture = new Texture(pixPopup);
            pixPopup.dispose();
            
            popup.setBackground(new TextureRegionDrawable(new TextureRegion(popupBgTexture)));
            popup.pad(40f);
            
            Label title = new Label(i18n.getText("offline_title"), skin);
            title.setFontScale(1.5f);
            
            Label desc = new Label(i18n.getText("offline_desc"), skin);
            desc.setAlignment(Align.center);
            
            Label amount = new Label("+" + PPFormatter.format(offlineGains) + " PP", skin);
            amount.setFontScale(1.8f);
            amount.setColor(Color.valueOf("1BA1E2"));
            
            TextButton btnOk = new TextButton(i18n.getText("offline_btn_ok"), skin);
            btnOk.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    overlay.remove();
                }
            });
            
            popup.add(title).padBottom(20f).row();
            popup.add(desc).padBottom(20f).row();
            popup.add(amount).padBottom(30f).row();
            popup.add(btnOk).size(150f, 50f);
            
            overlay.add(popup).center();
            stage.addActor(overlay);
        }

    }

    private Table buildDynamicShopCard(final Upgrade upgrade) {
        Skin skin = ResourceManager.getSkin();
        final String id = upgrade.getId();

        Label lblNombre = new Label(i18n.getText(upgrade.getNameKey()), skin);
        lblNombre.setWrap(true);
        Label lblCoste = new Label(formatCoste(upgrade.getCurrentCost()), skin);
        Label lblCantidad = new Label("x" + upgrade.getQuantity(), skin);
        lblCantidad.setAlignment(Align.right);

        shopCostLabels.put(id, lblCoste);
        shopQuantityLabels.put(id, lblCantidad);

        final Button.ButtonStyle estiloNormal = skin.get(TextButton.TextButtonStyle.class);
        Button.ButtonStyle estiloAlertaTemp = null;
        try {
            estiloAlertaTemp = skin.get("alerta", TextButton.TextButtonStyle.class);
        } catch (Exception e) {
            estiloAlertaTemp = skin.get("alerta", Button.ButtonStyle.class);
        }
        final Button.ButtonStyle estiloAlerta = estiloAlertaTemp;

        final Button btnCard = new Button(estiloNormal);

        if (!(upgrade instanceof AutomatedUpgrade)) {
            btnCard.setColor(Color.valueOf("1BA1E2"));
        }

        shopBuyButtons.put(id, btnCard);

        btnCard.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (upgrade.canAfford(game.getGameState().getPpActual())) {
                    AudioManager.getInstance().playSoundWithPitch(
                            AudioManager.SoundEffect.PURCHASE,
                            0.95f,
                            1.05f);
                    purchaseService.comprar(id, game.getGameState());

                    // Si es una mejora de un solo uso, forzamos reconstrucción para que desaparezca y la tabla se colapse
                    boolean isOneTime = (upgrade instanceof DirectUpgrade) || (upgrade instanceof MultiplierUpgrade);
                    if (isOneTime) {
                        forceRebuildShop();
                    }
                } else {
                    AudioManager.getInstance().playSound(AudioManager.SoundEffect.ERROR);
                    btnCard.clearActions();
                    if (btnCard.getParent() instanceof Table) {
                        ((Table) btnCard.getParent()).invalidate();
                        ((Table) btnCard.getParent()).layout();
                    }

                    if (estiloAlerta != null) {
                        btnCard.setStyle(estiloAlerta);
                        btnCard.setColor(Color.WHITE); 
                    }
                    btnCard.addAction(Actions.sequence(
                            Actions.moveBy(8, 0, 0.05f),
                            Actions.moveBy(-16, 0, 0.05f),
                            Actions.moveBy(16, 0, 0.05f),
                            Actions.moveBy(-8, 0, 0.05f),
                            Actions.delay(0.5f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    btnCard.setStyle(estiloNormal);
                                    if (!(upgrade instanceof AutomatedUpgrade)) {
                                        btnCard.setColor(Color.valueOf("1BA1E2")); 
                                    }
                                }
                            })));
                }
            }
        });

        String descKey = upgrade.getNameKey() + "_desc";
        String desc = "";
        try {
            desc = i18n.getText(descKey);
        } catch (Exception e) {
        }

        if (desc != null && !desc.isEmpty() && !desc.equals(descKey) && !desc.startsWith("???")) {
            TooltipManager tooltipManager = TooltipManager.getInstance();
            tooltipManager.initialTime = 1.0f;
            tooltipManager.subsequentTime = 1.0f;
            tooltipManager.resetTime = 0f;

            TextTooltip.TextTooltipStyle tooltipStyle = new TextTooltip.TextTooltipStyle();
            tooltipStyle.label = skin.get(Label.LabelStyle.class);

            Pixmap pixBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixBg.setColor(new Color(0.15f, 0.05f, 0.25f, 0.85f));
            pixBg.fill();
            tooltipStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(pixBg)));
            pixBg.dispose();

            tooltipStyle.wrapWidth = 250f;

            TextTooltip tooltip = new TextTooltip(desc, tooltipManager, tooltipStyle);
            tooltip.getContainer().pad(10f);
            tooltip.getActor().setFontScale(0.75f);

            btnCard.addListener(tooltip);
        }

        Image imgIcono = new Image(ResourceManager.texturaIconoPrueba);

        Table tablaTextos = new Table();
        tablaTextos.add(lblNombre).width(200).left().padBottom(4).row();
        tablaTextos.add(lblCoste).left();

        btnCard.pad(8);
        btnCard.add(imgIcono).size(48, 48).padRight(12);
        btnCard.add(tablaTextos).expandX().left();
        btnCard.add(lblCantidad).width(80).right().padLeft(12);

        shopCards.put(id, btnCard);
        return btnCard;
    }

    /**
     * Vacía las tablas de la tienda y limpia los mapas de seguimiento.
     * La próxima llamada a updateShop() rellenará todo desde cero.
     */
    private void forceRebuildShop() {
        colTienda.clear();
        colEstructuras.clear();
        shopCards.clear();
        shopCostLabels.clear();
        shopBuyButtons.clear();
        shopQuantityLabels.clear();
    }

    private void updateHUD(BigInteger pp, double pps) {
        labelPP.setText(PPFormatter.format(pp) + " PP");
        labelPPS.setText(PPFormatter.formatRate(pps) + " PP/seg");
    }

    private void updateShop() {
        Map<String, Upgrade> upgrades = game.getGameState().getUpgrades();
        double ppHist = game.getGameState().getPpHistorico().doubleValue();

        for (Upgrade u : upgrades.values()) {
            String id = u.getId();
            boolean isOneTime = (u instanceof DirectUpgrade) || (u instanceof MultiplierUpgrade);

            if (isOneTime && u.getQuantity() >= 1) {
                continue;
            }

            if (!shopCards.containsKey(id)) {
                if (ppHist >= (u.getCurrentCost() * 0.85)) {
                    Table card = buildDynamicShopCard(u);
                    if (u instanceof AutomatedUpgrade) {
                        colEstructuras.add(card).fillX().height(85).padBottom(8).row();
                    } else {
                        colTienda.add(card).fillX().height(85).padBottom(8).row();
                    }
                }
            }

            else {
                Label quantity = shopQuantityLabels.get(id);
                if (quantity != null) {
                    quantity.setText("x" + u.getQuantity());
                }

                Label coste = shopCostLabels.get(id);
                if (coste != null) {
                    coste.setText(formatCoste(u.getCurrentCost()));
                }
            }
        }
    }

    private String formatCoste(double cost) {
        BigInteger costBig = BigInteger.valueOf((long) cost);
        return i18n.getTextVar("tienda_coste", PPFormatter.format(costBig));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateHUD(
                game.getGameState().getPpActual(),
                game.getGameState().getPpPorSegundo());
        updateShop();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}