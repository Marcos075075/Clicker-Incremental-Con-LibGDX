package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.i18n.LocaleManager;
import com.kotcrab.vis.ui.widget.*;

/*
    ===============================================
    Juego Principal
    ===============================================
    Pantalla central del juego. Implementa el layout de 3 columnas 
    definido en la especificación funcional (A1.1) y el mockup (A1.5).

    ===============================================
    Estructura visual
    ===============================================
    HUD Superior (barra fija):
    - Botón "Ajustes" -> abre SettingsScreen con botón Reanudar

    Fila de encabezados (alineada entre las 3 columnas):
    - Izquierda: contador PP + tasa PP/seg
    - Centro: título "ESTRUCTURAS"
    - Derecha: título "TIENDA DE MEJORAS"

    Columna Izquierda — Zona de click:
    - Imagen del Núcleo ATLAS M.O.N.O. (clickeable)
    - Etiqueta con el nombre del núcleo
    - Etiqueta "ZONA DE RECOLECCIÓN ACTIVA"

    Columna Central — Estructuras:
    - ScrollPane con tarjetas de estructuras (botón Ensamblar)
    - Pendiente: se conectará con GameState cuando A1 implemente la lógica

    Columna Derecha — Tienda de Mejoras:
    - ScrollPane con tarjetas de mejoras (botón Comprar)
    - Pendiente: se conectará con UpgradeManager cuando A1 lo implemente

    ===============================================
    Conexiones con otros módulos
    ===============================================
    - updateHUD(): pendiente de uso, se llamará desde el Main Thread
      cuando A1 implemente GameState y el Logic Thread.
    - btnNucleo listener: pendiente de conectar con GameState (A1).
*/

public class GameScreen extends BaseScreen {

    // Labels del HUD, se actualizan desde fuera vía updateHUD()
    private VisLabel labelPP;
    private VisLabel labelPPS;
    private Texture texturaHamster;

    public GameScreen(MainGame game) {
        super(game);
    }

    @Override
    protected void buildUI() {
        LocaleManager i18n = LocaleManager.getInstance();

        // HUD SUPERIOR
        VisTable hud = new VisTable();
        VisTextButton btnAjustes = new VisTextButton(i18n.getText("menu_ajustes"));
        hud.add(btnAjustes).right().padRight(16).width(150).height(50).expandX();
        root.add(hud).fillX().height(60).row();

        // COLUMNA IZQUIERDA, encabezado PP + contenido núcleo
        VisTable colIzquierda = new VisTable();
        colIzquierda.top();

        labelPP  = new VisLabel("0 PP");
        labelPPS = new VisLabel("0.0 PP/seg");
        colIzquierda.add(labelPP).center().padTop(8).row();
        colIzquierda.add(labelPPS).center().padBottom(16).row();

        // Imagen temporal del núcleo, se reemplazará por el asset definitivo
        texturaHamster = new Texture(Gdx.files.internal("img/hamster.png"));
        Image btnNucleo = new Image(texturaHamster);
        VisLabel lblNombre = new VisLabel(i18n.getText("juego_nombre_nucleo"));
        VisLabel lblZona   = new VisLabel(i18n.getText("juego_zona_activa"));

        colIzquierda.add(btnNucleo).size(220, 220).padBottom(16).row();
        colIzquierda.add(lblNombre).padBottom(8).row();
        colIzquierda.add(lblZona).row();

        // COLUMNA CENTRAL, encabezado Estructuras + contenido con scroll
        VisTable colCentro = new VisTable();
        colCentro.top();
        colCentro.add(new VisLabel(i18n.getText("estructuras_titulo"))).center().padTop(8).padBottom(16).row();

        VisTable colEstructuras = new VisTable();
        colEstructuras.top();

        // Tarjetas de ejemplo, se reemplazarán cuando se implemente UpgradeManager
        colEstructuras.add(buildEstructuraCard(
            i18n.getText("estructura_nucleo_auxiliar"),
            i18n.getTextVar("tienda_coste", "50"),
            i18n.getText("estructuras_btn_ensamblar")
        )).fillX().padBottom(8).row();

        colEstructuras.add(buildEstructuraCard(
            i18n.getText("estructura_nodo_procesador"),
            i18n.getTextVar("tienda_coste", "200"),
            i18n.getText("estructuras_btn_ensamblar")
        )).fillX().padBottom(8).row();

        colEstructuras.add(buildEstructuraCard(
            i18n.getText("estructura_relay_cuantico"),
            i18n.getTextVar("tienda_coste", "500"),
            i18n.getText("estructuras_btn_ensamblar")
        )).fillX().row();

        ScrollPane scrollEst = new ScrollPane(colEstructuras);
        scrollEst.setFadeScrollBars(false);
        colCentro.add(scrollEst).expand().fill().row();

        // COLUMNA DERECHA, encabezado Tienda + contenido con scroll
        VisTable colDerecha = new VisTable();
        colDerecha.top();
        colDerecha.add(new VisLabel(i18n.getText("tienda_titulo"))).center().padTop(8).padBottom(16).row();

        VisTable colTienda = new VisTable();
        colTienda.top();

        // Tarjetas de ejemplo, se reemplazarán cuando se implemente UpgradeManager
        colTienda.add(buildShopCard(
            i18n.getText("mejora_directa_1"),
            i18n.getTextVar("tienda_coste", "10"),
            i18n.getText("tienda_btn_comprar")
        )).fillX().padBottom(8).row();

        colTienda.add(buildShopCard(
            i18n.getText("mejora_amplificador"),
            i18n.getTextVar("tienda_coste", "100"),
            i18n.getText("tienda_btn_comprar")
        )).fillX().padBottom(8).row();

        colTienda.add(buildShopCard(
            i18n.getText("mejora_multiplicador_nucleo"),
            i18n.getTextVar("tienda_coste", "500"),
            i18n.getText("tienda_btn_comprar")
        )).fillX().row();

        ScrollPane scrollTienda = new ScrollPane(colTienda);
        scrollTienda.setFadeScrollBars(false);
        colDerecha.add(scrollTienda).expand().fill().row();

        // TABLA PRINCIPAL, 3 columnas con encabezado integrado
        VisTable mainTable = new VisTable();
        mainTable.add(colIzquierda).expandX().fillX().expandY().fillY().uniform().top();
        mainTable.add(colCentro).expandX().fillX().expandY().fillY().uniform().top();
        mainTable.add(colDerecha).expandX().fillX().expandY().fillY().uniform().top();

        root.add(mainTable).expand().fill();

        // Listeners
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
                // Abre ajustes con botón Reanudar visible
                game.setScreen(new SettingsScreen(game, true));
            }
        });
    }

    // Construye una tarjeta de estructura para la columna central
    private VisTable buildEstructuraCard(String nombre, String coste, String btnTexto) {
        VisTable card = new VisTable();
        card.pad(8);

        VisLabel lblNombre = new VisLabel(nombre);
        VisLabel lblCoste = new VisLabel(coste);
        VisTextButton btnEnsamblar = new VisTextButton(btnTexto);

        card.add(lblNombre).expandX().left().padBottom(4).row();
        card.add(lblCoste).left();
        card.add(btnEnsamblar).right().width(110).height(40);

        return card;
    }

    // Construye una tarjeta de mejora para la tienda
    private VisTable buildShopCard(String nombre, String coste, String btnTexto) {
        VisTable card = new VisTable();
        card.pad(8);

        VisLabel lblNombre = new VisLabel(nombre);
        VisLabel lblCoste = new VisLabel(coste);
        VisTextButton btnComprar = new VisTextButton(btnTexto);

        card.add(lblNombre).expandX().left().padBottom(4).row();
        card.add(lblCoste).left();
        card.add(btnComprar).right().width(110).height(40);

        return card;
    }

    /*
        Actualiza el HUD con los valores actuales de la partida.
        Pendiente de uso: se llamará desde el Main Thread cuando
        A1 implemente GameState y el Logic Thread.
        - pp:  cantidad actual de Partículas de Proceso
        - pps: tasa de generación automática en PP/segundo
    */
    public void updateHUD(long pp, double pps) {
        labelPP.setText(pp + " PP");
        labelPPS.setText(String.format("%.1f", pps) + " PP/seg");
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaHamster != null) texturaHamster.dispose();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        updateHUD(
            game.getGameState().getPpActual(),
            game.getGameState().getPpPorSegundo()
        );
    }
}