package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.core.PPFormatter;
import com.jovellanos.clicker.core.ResourceManager;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.jovellanos.clicker.logic.PurchaseService;
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
    private Texture texturaNucleo;
    private Texture fondoJuego;
    
    // Textura para el icono temporal de las mejoras
    private Texture texturaIconoPrueba;

    private final Map<String, Table>      shopCards          = new HashMap<String, Table>();
    private final Map<String, Label>      shopCostLabels     = new HashMap<String, Label>();
    
    // Se cambia de TextButton a Button porque ahora toda la tarjeta es el botón interactivo
    private final Map<String, Button>     shopBuyButtons     = new HashMap<String, Button>();
    private final Map<String, Label>      shopQuantityLabels = new HashMap<String, Label>();

    private LocaleManager   i18n;
    private PurchaseService purchaseService;

    public GameScreen(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        if (fondoJuego == null) {
            fondoJuego = new Texture(Gdx.files.internal("img/FondoJuego.png"));
        }
        if (texturaIconoPrueba == null) {
            texturaIconoPrueba = new Texture(Gdx.files.internal("img/iconoprueba.png"));
        }
        purchaseService    = game.getPurchaseService();
        super.show();
    }

    @Override
    protected void buildUI() {
        this.i18n = LocaleManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        root.setBackground(new TextureRegionDrawable(new TextureRegion(fondoJuego)));

        // ── HUD SUPERIOR ────────────────────────────────────────────────
        Table hud = new Table();
        TextButton btnAjustes = new TextButton(i18n.getText("menu_ajustes"), skin);
        hud.add(btnAjustes).right().padRight(16).width(150).height(50).expandX();
        root.add(hud).fillX().height(60).row();

        // ── COLUMNA IZQUIERDA — Zona de click ───────────────────────────
        Table colIzquierda = new Table();
        colIzquierda.top();

        labelPP  = new Label("0 PP", skin);
        labelPPS = new Label("0 PP/seg", skin);
        colIzquierda.add(labelPP).center().padTop(8).row();
        colIzquierda.add(labelPPS).center().padBottom(16).row();

        texturaNucleo = new Texture(Gdx.files.internal("img/nucleo.png"));
        Image btnNucleo = new Image(texturaNucleo);
        Label lblNombre = new Label(i18n.getText("juego_nombre_nucleo"), skin);
        Label lblZona   = new Label(i18n.getText("juego_zona_activa"), skin);

        colIzquierda.add(btnNucleo).size(220, 220).padBottom(16).row();
        colIzquierda.add(lblNombre).padBottom(8).row();
        colIzquierda.add(lblZona).row();

        // ── COLUMNA CENTRAL — Estructuras ───────────────────────────────
        Table colCentro = new Table();
        colCentro.top();
        colCentro.add(new Label(i18n.getText("estructuras_titulo"), skin))
                 .center().padTop(8).padBottom(16).row();

        Table colEstructuras = new Table();
        colEstructuras.top();

        Map<String, Upgrade> upgrades = game.getGameState().getUpgrades();
        for (Upgrade u : upgrades.values()) {
            if (u instanceof AutomatedUpgrade) {
                colEstructuras.add(buildDynamicShopCard(u)).fillX().padBottom(8).row();
            }
        }

        ScrollPane scrollEst = new ScrollPane(colEstructuras);
        scrollEst.setFadeScrollBars(false);
        colCentro.add(scrollEst).expand().fill().row();

        // ── COLUMNA DERECHA — Tienda dinámica ───────────────────────────
        Table colDerecha = new Table();
        colDerecha.top();
        colDerecha.add(new Label(i18n.getText("tienda_titulo"), skin))
                 .center().padTop(8).padBottom(16).row();

        Table colTienda = new Table();
        colTienda.top();

        for (Upgrade u : upgrades.values()) {
            if (u instanceof DirectUpgrade || u instanceof MultiplierUpgrade) {
                colTienda.add(buildDynamicShopCard(u)).fillX().padBottom(8).row();
            }
        }

        ScrollPane scrollTienda = new ScrollPane(colTienda);
        scrollTienda.setFadeScrollBars(false);
        colDerecha.add(scrollTienda).expand().fill().row();

        // ── TABLA PRINCIPAL — 3 columnas ────────────────────────────────
        Table mainTable = new Table();
        mainTable.add(colIzquierda).expandX().fillX().expandY().fillY().uniform().top();
        mainTable.add(colCentro).expandX().fillX().expandY().fillY().uniform().top();
        mainTable.add(colDerecha).expandX().fillX().expandY().fillY().uniform().top();
        root.add(mainTable).expand().fill();

        // ── Listeners ───────────────────────────────────────────────────
        btnNucleo.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.getGameState().addPendingClick();
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
    }

    /**
     * Construye una tarjeta de tienda.
     * El coste se formatea con PPFormatter para consistencia con el HUD.
     */
    private Table buildDynamicShopCard(final Upgrade upgrade) {
        Skin skin = ResourceManager.getSkin();
        final String id = upgrade.getId();

        Label lblNombre   = new Label(i18n.getText(upgrade.getNameKey()), skin);
        // Coste formateado: "50 PP", "1,23K PP", etc.
        Label lblCoste    = new Label(formatCoste(upgrade.getCurrentCost()), skin);
        Label lblCantidad = new Label("x" + upgrade.getQuantity(), skin);

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
        shopBuyButtons.put(id, btnCard);

        btnCard.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (upgrade.canAfford(game.getGameState().getPpActual())) {
                    purchaseService.comprar(id, game.getGameState());
                } else {
                    btnCard.clearActions();
                    if (btnCard.getParent() instanceof Table) {
                        ((Table) btnCard.getParent()).invalidate();
                        ((Table) btnCard.getParent()).layout();
                    }
                    
                    if (estiloAlerta != null) {
                        btnCard.setStyle(estiloAlerta);
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
                            }
                        })
                    ));
                }
            }
        });

        // Configuración del Tooltip para mostrar la descripción
        String descKey = upgrade.getNameKey() + "_desc";
        String desc = "";
        try {
            desc = i18n.getText(descKey);
        } catch (Exception e) {
            // Se ignora si no existe la traducción para esta mejora
        }
        
        // Si la descripción existe y es válida, se acopla el tooltip
        if (desc != null && !desc.isEmpty() && !desc.equals(descKey) && !desc.startsWith("???")) {
            TooltipManager tooltipManager = TooltipManager.getInstance();
            tooltipManager.initialTime = 0.4f; // 400ms de retardo al pasar el cursor
   
            // Estilo del tooltip
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

        // Imagen en el lado izquierdo
        Image imgIcono = new Image(texturaIconoPrueba);

        // Subtabla para apilar el nombre y el coste en el centro
        Table tablaTextos = new Table();
        tablaTextos.add(lblNombre).left().padBottom(4).row();
        tablaTextos.add(lblCoste).left();

        // Se monta la estructura interna del botón
        btnCard.pad(8);
        btnCard.add(imgIcono).size(48, 48).padRight(12);
        btnCard.add(tablaTextos).expandX().left();
        btnCard.add(lblCantidad).right().padLeft(12);

        shopCards.put(id, btnCard);
        return btnCard;
    }

    // ────────────────────────────────────────────────────────────────────
    // Actualización del HUD
    // ────────────────────────────────────────────────────────────────────

    /**
     * Actualiza el contador de PP y la tasa PP/s en el HUD.
     * PPFormatter convierte el BigInteger a una cadena legible.
     */
    private void updateHUD(BigInteger pp, double pps) {
        labelPP.setText(PPFormatter.format(pp) + " PP");
        labelPPS.setText(PPFormatter.formatRate(pps) + " PP/seg");
    }

    // ────────────────────────────────────────────────────────────────────
    // Actualización de la tienda
    // ────────────────────────────────────────────────────────────────────

    private void updateShop() {
        Map<String, Upgrade> upgrades = game.getGameState().getUpgrades();

        List<String> toRemove = new ArrayList<String>();

        for (Map.Entry<String, Table> entry : shopCards.entrySet()) {
            String  id   = entry.getKey();
            Table   card = entry.getValue();
            Upgrade u    = upgrades.get(id);
            if (u == null) continue;

            boolean isOneTime = (u instanceof DirectUpgrade)
                             || (u instanceof MultiplierUpgrade);

            if (isOneTime && u.getQuantity() >= 1) {
                card.remove();
                toRemove.add(id);
                continue;
            }

            Label quantity = shopQuantityLabels.get(id);
            if (quantity != null) {
                quantity.setText("x" + u.getQuantity());
            }

            // Coste actualizado con PPFormatter
            Label coste = shopCostLabels.get(id);
            if (coste != null) {
                coste.setText(formatCoste(u.getCurrentCost()));
            }
        }

        for (String id : toRemove) {
            shopCards.remove(id);
            shopCostLabels.remove(id);
            shopBuyButtons.remove(id);
            shopQuantityLabels.remove(id);
        }
    }

    /** Formatea el coste (double) al estilo "Coste: 1,23K PP". */
    private String formatCoste(double cost) {
        BigInteger costBig = BigInteger.valueOf((long) cost);
        return i18n.getTextVar("tienda_coste", PPFormatter.format(costBig));
    }

    // ────────────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateHUD(
            game.getGameState().getPpActual(),
            game.getGameState().getPpPorSegundo()
        );
        updateShop();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaNucleo      != null) texturaNucleo.dispose();
        if (fondoJuego         != null) fondoJuego.dispose();
        if (texturaIconoPrueba != null) texturaIconoPrueba.dispose();
    }
}