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
    - Botón "Configuración" -> navega a PauseScreen

    Columna Izquierda — Zona de click:
    - Contador de PP grande (solo número + PP)
    - Tasa de generación (solo número + PP/seg)
    - Imagen del Núcleo ATLAS M.O.N.O. (clickeable)
    - Etiqueta con el nombre del núcleo
    - Etiqueta "ZONA DE RECOLECCIÓN ACTIVA"

    Columna Central — Estructuras:
    - Título "ESTRUCTURAS"
    - ScrollPane para la lista de estructuras activas
    - Pendiente: se conectará con GameState cuando A1 implemente la lógica

    Columna Derecha — Tienda de Mejoras:
    - Título "TIENDA DE MEJORAS"
    - ScrollPane con tarjetas de mejoras (nombre, coste, botón)
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

        // HUD SUPERIOR, de momento solo botón de configuración
        VisTable hud = new VisTable();
        VisTextButton btnConfig = new VisTextButton(i18n.getText("pausa_configuracion"));
        hud.add(btnConfig).right().padRight(16).width(150).height(50).expandX();
        root.add(hud).fillX().height(60).row();

        // 3 COLUMNAS
        VisTable content = new VisTable();

        // Columna izquierda: contador PP + núcleo
        VisTable colClick = new VisTable();
        colClick.center();

        // Contador de PP
        labelPP  = new VisLabel("0 PP");
        // Tasa de generación
        labelPPS = new VisLabel("0.0 PP/seg");

        // Imagen temporal del núcleo, se reemplazará por el asset definitivo
        texturaHamster = new Texture(Gdx.files.internal("img/hamster.png"));
        Image btnNucleo = new Image(texturaHamster);

        VisLabel lblNombre = new VisLabel(i18n.getText("juego_nombre_nucleo"));
        VisLabel lblZona   = new VisLabel(i18n.getText("juego_zona_activa"));

        colClick.add(labelPP).padBottom(4).row();
        colClick.add(labelPPS).padBottom(16).row();
        colClick.add(btnNucleo).size(220, 220).padBottom(16).row();
        colClick.add(lblNombre).padBottom(8).row();
        colClick.add(lblZona).row();

        // Columna central: estructuras con scroll
        VisTable colEstructuras = new VisTable();
        colEstructuras.top().padTop(12);
        colEstructuras.add(new VisLabel(i18n.getText("estructuras_titulo"))).padBottom(16).row();
        // Pendiente: se rellenará con las estructuras activas cuando se implemente GameState
        colEstructuras.add(new VisLabel("—")).row();

        ScrollPane scrollEst = new ScrollPane(colEstructuras);
        scrollEst.setFadeScrollBars(false);

        // Columna derecha: tienda con scroll
        VisTable colTienda = new VisTable();
        colTienda.top().padTop(12);
        colTienda.add(new VisLabel(i18n.getText("tienda_titulo"))).padBottom(16).row();

        // Tarjetas basadas en el mockup, se reemplazarán cuando se implemente UpgradeManager
        colTienda.add(buildShopCard(
            i18n.getText("mejora_directa_1"),
            i18n.getTextVar("tienda_coste", "10"),
            i18n.getText("tienda_btn_comprar")
        )).fillX().padBottom(8).row();

        colTienda.add(buildShopCard(
            i18n.getText("mejora_estructura_basica"),
            i18n.getTextVar("tienda_coste", "50"),
            i18n.getText("estructuras_btn_ensamblar")
        )).fillX().padBottom(8).row();

        colTienda.add(buildShopCard(
            i18n.getText("mejora_multiplicador_nucleo"),
            i18n.getTextVar("tienda_coste", "500"),
            i18n.getText("tienda_btn_comprar")
        )).fillX().row();

        ScrollPane scrollTienda = new ScrollPane(colTienda);
        scrollTienda.setFadeScrollBars(false);

        // Proporciones: las 3 columnas ocupan el mismo espacio
        content.add(colClick).expand().fill().uniform();
        content.add(scrollEst).expand().fill().uniform();
        content.add(scrollTienda).expand().fill().uniform();

        root.add(content).expand().fill();

        // Listeners
        btnNucleo.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Pendiente: conectar con GameState cuando A1 implemente la lógica de clics
                return true;
            }
        });

        btnConfig.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.PAUSE);
            }
        });
    }

    // Construye una tarjeta de mejora para la tienda
    private VisTable buildShopCard(String nombre, String coste, String btnTexto) {
        VisTable card = new VisTable();
        card.pad(8);

        VisLabel lblNombre   = new VisLabel(nombre);
        VisLabel lblCoste    = new VisLabel(coste);
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
}