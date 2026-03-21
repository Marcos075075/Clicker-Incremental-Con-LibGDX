package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.jovellanos.clicker.upgrades.AutomatedUpgrade;
import com.jovellanos.clicker.upgrades.DirectUpgrade;
import com.jovellanos.clicker.upgrades.MultiplierUpgrade;
import com.jovellanos.clicker.upgrades.Upgrade;
import com.kotcrab.vis.ui.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    ===============================================
    Juego Principal
    ===============================================
    Pantalla central del juego. Implementa el layout de 3 columnas
    definido en la especificación funcional (A1.1) y el mockup (A1.5).

    ===============================================
    Comportamiento de compra por tipo
    ===============================================
    - DirectUpgrade / MultiplierUpgrade: compra única.
      Tras comprar, la tarjeta se elimina con card.remove() para que
      las demás suban y no quede hueco en el layout.
    - AutomatedUpgrade: compra múltiple (cuando se activen).
      La tarjeta permanece y muestra el coste actualizado.

    ===============================================
    Sincronización con GameState
    ===============================================
    - shopCards:       mapa id → VisTable de la tarjeta
    - shopCostLabels:  mapa id → VisLabel del coste
    - shopBuyButtons:  mapa id → VisTextButton
    Todo se actualiza en render() → updateShop().

    ===============================================
    Nota sobre card.remove() vs setVisible(false)
    ===============================================
    setVisible(false) oculta el widget pero Scene2D sigue reservando
    su espacio en el layout, dejando un hueco vacío.
    card.remove() elimina el actor del Stage completamente,
    el layout se recalcula y las tarjetas restantes suben.
    Una vez eliminado del mapa, el id no vuelve a procesarse.
*/

public class GameScreen extends BaseScreen {

    private VisLabel labelPP;
    private VisLabel labelPPS;
    private Texture texturaHamster;
    private Texture fondoJuego;

    // Referencias a widgets de la tienda para actualizar en render()
    private final Map<String, VisTable>      shopCards      = new HashMap<String, VisTable>();
    private final Map<String, VisLabel>      shopCostLabels = new HashMap<String, VisLabel>();
    private final Map<String, VisTextButton> shopBuyButtons = new HashMap<String, VisTextButton>();
    private final Map<String, VisLabel> shopQuantityLabels = new HashMap<String, VisLabel>();

    public GameScreen(MainGame game) {
        super(game);
    }

    private LocaleManager i18n;

    @Override
    public void show() {
        fondoJuego = new Texture(Gdx.files.internal("img/FondoJuego.png"));
        super.show();
    }

    @Override
    protected void buildUI() {
        this.i18n = LocaleManager.getInstance();

        // ── HUD SUPERIOR ────────────────────────────────────────────────
        VisTable hud = new VisTable();
        VisTextButton btnAjustes = new VisTextButton(i18n.getText("menu_ajustes"), crearEstiloBoton());
        hud.add(btnAjustes).right().padRight(16).width(150).height(50).expandX();
        root.add(hud).fillX().height(60).row();

        // ── COLUMNA IZQUIERDA — Zona de click ───────────────────────────
        VisTable colIzquierda = new VisTable();
        colIzquierda.top();

        labelPP  = new VisLabel("0 PP");
        labelPPS = new VisLabel("0.0 PP/seg");
        colIzquierda.add(labelPP).center().padTop(8).row();
        colIzquierda.add(labelPPS).center().padBottom(16).row();

        texturaHamster = new Texture(Gdx.files.internal("img/hamster.png"));
        Image btnNucleo = new Image(texturaHamster);
        VisLabel lblNombre = new VisLabel(i18n.getText("juego_nombre_nucleo"));
        VisLabel lblZona   = new VisLabel(i18n.getText("juego_zona_activa"));

        colIzquierda.add(btnNucleo).size(220, 220).padBottom(16).row();
        colIzquierda.add(lblNombre).padBottom(8).row();
        colIzquierda.add(lblZona).row();

        // ── COLUMNA CENTRAL — Estructuras ───────────────────────────────
        VisTable colCentro = new VisTable();
        colCentro.top();
        colCentro.add(new VisLabel(i18n.getText("estructuras_titulo")))
                 .center().padTop(8).padBottom(16).row();

        VisTable colEstructuras = new VisTable();
        colEstructuras.top();

        Map<String, Upgrade> upgrades = game.getGameState().getUpgrades();
        for (Upgrade u : upgrades.values()) {
            if (u instanceof AutomatedUpgrade) {
                colEstructuras.add(buildDynamicShopCard(u, i18n)).fillX().padBottom(8).row();
            }
        }

        ScrollPane scrollEst = new ScrollPane(colEstructuras);
        scrollEst.setFadeScrollBars(false);
        colCentro.add(scrollEst).expand().fill().row();

        // ── COLUMNA DERECHA — Tienda dinámica ───────────────────────────
        VisTable colDerecha = new VisTable();
        colDerecha.top();
        colDerecha.add(new VisLabel(i18n.getText("tienda_titulo")))
                  .center().padTop(8).padBottom(16).row();

        VisTable colTienda = new VisTable();
        colTienda.top();

        for (Upgrade u : upgrades.values()) {
            if (u instanceof DirectUpgrade || u instanceof MultiplierUpgrade) {
                colTienda.add(buildDynamicShopCard(u, i18n)).fillX().padBottom(8).row();
            }
            // AutomatedUpgrade: se añadirá aquí cuando se activen
        }

        ScrollPane scrollTienda = new ScrollPane(colTienda);
        scrollTienda.setFadeScrollBars(false);
        colDerecha.add(scrollTienda).expand().fill().row();

        // ── TABLA PRINCIPAL — 3 columnas ────────────────────────────────
        VisTable mainTable = new VisTable();
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

        // Esc abre el menú de configuración
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
     * Construye una tarjeta de tienda conectada a una mejora real del GameState.
     *
     * Para Direct y Multiplier (compra única) la tarjeta se elimina con
     * card.remove() en updateShop() cuando quantity >= 1, de forma que
     * las tarjetas restantes suben sin dejar hueco.
     */
    private VisTable buildDynamicShopCard(final Upgrade upgrade, LocaleManager i18n) {
        final String id = upgrade.getId();

        VisLabel lblNombre = new VisLabel(i18n.getText(upgrade.getNameKey()));

        VisLabel lblCoste = new VisLabel(
            i18n.getTextVar("tienda_coste", (long) upgrade.getCurrentCost()));

        shopCostLabels.put(id, lblCoste);

        VisLabel lblCantidad = new VisLabel("x" + upgrade.getQuantity());
        shopQuantityLabels.put(id, lblCantidad);

        VisTextButton btnComprar = new VisTextButton(i18n.getText("tienda_btn_comprar"), crearEstiloBoton());
        shopBuyButtons.put(id, btnComprar);

        btnComprar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getGameState().purchaseUpgrade(id);
            }
        });

        VisTable card = new VisTable();
        card.pad(8);
        card.add(lblNombre).expandX().left().padBottom(2).row();
        card.add(lblCantidad).right().padBottom(2).row();
        card.add(lblCoste).left();
        card.add(btnComprar).right().width(110).height(40);

        shopCards.put(id, card);

        return card;
    }

    /** Construye una tarjeta estática para la columna de estructuras. */
    private VisTable buildEstructuraCard(String nombre, String coste, String btnTexto) {
        VisTable card = new VisTable();
        card.pad(8);
        card.add(new VisLabel(nombre)).expandX().left().padBottom(4).row();
        card.add(new VisLabel(coste)).left();
        card.add(new VisTextButton(btnTexto, crearEstiloBoton())).right().width(110).height(40);
        return card;
    }

    /** Actualiza el HUD con los valores actuales del GameState. */
    public void updateHUD(long pp, double pps) {
        labelPP.setText(pp + " PP");
        labelPPS.setText(String.format("%.1f", pps) + " PP/seg");
    }

    /**
     * Refresca la tienda en cada frame.
     *
     * Para Direct / Multiplier ya compradas: elimina la tarjeta con remove()
     * y la borra de los mapas para no volver a procesarla. Al eliminar el actor
     * del Stage, Scene2D recalcula el layout y las tarjetas restantes suben.
     *
     * Para todas las visibles: deshabilita el botón si no hay PP suficientes.
     */
    private void updateShop() {
        Map<String, Upgrade> upgrades = game.getGameState().getUpgrades();
        long ppActual = game.getGameState().getPpActual();

        // Recoger ids a eliminar para no modificar el mapa mientras se itera
        List<String> toRemove = new ArrayList<String>();

        for (Map.Entry<String, VisTable> entry : shopCards.entrySet()) {
            String   id   = entry.getKey();
            VisTable card = entry.getValue();
            Upgrade  u    = upgrades.get(id);
            if (u == null) continue;

            boolean isOneTime = (u instanceof DirectUpgrade)
                             || (u instanceof MultiplierUpgrade);

            if (isOneTime && u.getQuantity() >= 1) {
                // Eliminar físicamente del Stage: el layout se recalcula solo
                card.remove();
                toRemove.add(id);
                continue;
            }

            // Deshabilitar botón si no hay PP suficientes
            VisTextButton btn = shopBuyButtons.get(id);
            if (btn != null) {
                btn.setDisabled(!u.canAfford(ppActual));
            }

            //Actualizar la cantidad
            VisLabel quantity = shopQuantityLabels.get(id);
            if (quantity != null) {
                quantity.setText("x" + u.getQuantity());
            }

            //Actualizar coste
            VisLabel coste = shopCostLabels.get(id);
            if (coste != null) {
                coste.setText(i18n.getTextVar("tienda_coste", (long) u.getCurrentCost()));
            }
        }

        // Limpiar referencias de las tarjetas ya eliminadas
        for (String id : toRemove) {
            shopCards.remove(id);
            shopCostLabels.remove(id);
            shopBuyButtons.remove(id);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getBatch().begin();
        stage.getBatch().draw(fondoJuego, 0, 0,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();
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
        if (texturaHamster != null) texturaHamster.dispose();
        if (fondoJuego != null) fondoJuego.dispose();
    }
}