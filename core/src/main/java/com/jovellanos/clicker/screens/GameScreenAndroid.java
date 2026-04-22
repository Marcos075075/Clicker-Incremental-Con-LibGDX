package com.jovellanos.clicker.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.jovellanos.clicker.MainGame;
import com.jovellanos.clicker.MainGame.ScreenType;
import com.jovellanos.clicker.audio.AudioManager;
import com.jovellanos.clicker.audio.UISounds;
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
    Juego Principal (Versión Android)
    ===============================================
    Pantalla central del juego estructurada exclusivamente 
    para dispositivos móviles.

    Implementación técnica:
    - Layout dinámico con navegación inferior (Bottom Nav).
    - Fondos independientes para cada pestaña (Núcleo, Estructuras, Mejoras).
    - Capas superpuestas mediante Stack para el menú modal de configuración.
    - Desplazamiento bloqueado en el eje horizontal para las listas 
      de la tienda (setScrollingDisabled).
    - Fondos semitransparentes generados proceduralmente (Pixmap) para
      evitar dependencias estrictas del archivo Skin.json.
    - Se implementa un sistema de contadores duales (HUD superior y central) 
      que alterna su visibilidad según la pestaña activa.
*/
public class GameScreenAndroid extends BaseScreen {

    private Label labelPP_hud;
    private Label labelPPS_hud;
    private Label labelPP_nucleo;
    private Label labelPPS_nucleo;
    private Table scoreTableHud;

    private final Map<String, Table>      shopCards          = new HashMap<String, Table>();
    private final Map<String, Label>      shopCostLabels     = new HashMap<String, Label>();
    private final Map<String, Button>     shopBuyButtons     = new HashMap<String, Button>();
    private final Map<String, Label>      shopQuantityLabels = new HashMap<String, Label>();

    private LocaleManager   i18n;
    private PurchaseService purchaseService;

    private enum ViewState { NUCLEO, ESTRUCTURAS, MEJORAS }
    private ViewState currentViewState = ViewState.NUCLEO;
    private Table dynamicAreaTable;
    private Table nucleoPageTable;
    private Table estructurasPageTable;
    private Table mejorasPageTable;
    private Stack modalLayer;
    
    private TextButton btnNavNucleo;
    private TextButton btnNavEstructuras;
    private TextButton btnNavMejoras;
    
    private Texture darkeningTexture;
    private Texture navBgTexture;
    private Texture popupBgTexture;
    private Texture tooltipBgTexture;

    public GameScreenAndroid(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        AudioManager.getInstance().playMusic(AudioManager.Track.GAME);
        purchaseService = game.getPurchaseService();
        super.show();
    }

    @Override
    protected void buildUI() {
        this.i18n = LocaleManager.getInstance();
        Skin skin = ResourceManager.getSkin();

        Pixmap pixNav = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixNav.setColor(new Color(0.15f, 0.1f, 0.25f, 1f));
        pixNav.fill();
        navBgTexture = new Texture(pixNav);
        pixNav.dispose();

        Pixmap pixTooltip = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixTooltip.setColor(new Color(0.15f, 0.05f, 0.25f, 0.85f));
        pixTooltip.fill();
        tooltipBgTexture = new Texture(pixTooltip);
        pixTooltip.dispose();

        Stack mainStack = new Stack();
        mainStack.setFillParent(true);

        Table mainContentTable = new Table();
        mainContentTable.top();

        Table hudTable = new Table();
        scoreTableHud = new Table();
        labelPP_hud = new Label("0 PP", skin, "large");
        labelPP_hud.setFontScale(1.8f);
        labelPPS_hud = new Label("0 PP/seg", skin);
        labelPPS_hud.setFontScale(1.2f);
        scoreTableHud.add(labelPP_hud).left().row();
        scoreTableHud.add(labelPPS_hud).left().padTop(5);
        scoreTableHud.setVisible(false); 

        TextButton btnAjustes;
        if (skin.has("large", TextButton.TextButtonStyle.class)) {
            btnAjustes = new TextButton(i18n.getText("menu_ajustes"), skin, "large");
        } else {
            btnAjustes = new TextButton(i18n.getText("menu_ajustes"), skin);
        }
        btnAjustes.getLabel().setFontScale(2.5f);

        hudTable.add(scoreTableHud).expandX().left().padTop(60).padLeft(40);
        hudTable.add(btnAjustes).expandX().right().padTop(60).padRight(40).height(140);

        dynamicAreaTable = new Table();

        Table bottomNavTable = new Table();
        bottomNavTable.setBackground(new TextureRegionDrawable(new TextureRegion(navBgTexture)));

        TextButton.TextButtonStyle navStyle = skin.get(TextButton.TextButtonStyle.class);
        if (skin.has("large", TextButton.TextButtonStyle.class)) {
            navStyle = skin.get("large", TextButton.TextButtonStyle.class);
        }
        
        btnNavNucleo = new TextButton(i18n.getText("juego_nombre_nucleo"), navStyle);
        btnNavEstructuras = new TextButton(i18n.getText("estructuras_titulo"), navStyle);
        btnNavMejoras = new TextButton(i18n.getText("tienda_titulo"), navStyle);

        btnNavNucleo.getLabel().setFontScale(1.3f);
        btnNavEstructuras.getLabel().setFontScale(1.3f);
        btnNavMejoras.getLabel().setFontScale(1.3f);

        Label sep1 = new Label("|", skin);
        sep1.setFontScale(1.8f);
        sep1.setColor(Color.GRAY);
        
        Label sep2 = new Label("|", skin);
        sep2.setFontScale(1.8f);
        sep2.setColor(Color.GRAY);

        bottomNavTable.defaults().height(160);
        bottomNavTable.add(btnNavNucleo).expandX().fillX();
        bottomNavTable.add(sep1).pad(10);
        bottomNavTable.add(btnNavEstructuras).expandX().fillX();
        bottomNavTable.add(sep2).pad(10);
        bottomNavTable.add(btnNavMejoras).expandX().fillX();

        mainContentTable.add(hudTable).fillX().row();
        mainContentTable.add(dynamicAreaTable).expand().fill().row();
        mainContentTable.add(bottomNavTable).fillX();

        initMobilePages(skin);

        modalLayer = new Stack();
        modalLayer.setVisible(false);

        Pixmap pixDark = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixDark.setColor(new Color(0f, 0f, 0f, 0.75f));
        pixDark.fill();
        darkeningTexture = new Texture(pixDark);
        pixDark.dispose();
        
        Image darkBg = new Image(darkeningTexture);
        darkBg.setTouchable(Touchable.enabled);
        modalLayer.add(darkBg);

        Table settingsModal = createMobileSettingsModal(skin);
        modalLayer.add(settingsModal);

        mainStack.add(mainContentTable);
        mainStack.add(modalLayer);
        root.add(mainStack).expand().fill();

        btnNavNucleo.addListener(UISounds.CLICK);
        btnNavNucleo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { swapView(ViewState.NUCLEO); }
        });

        btnNavEstructuras.addListener(UISounds.CLICK);
        btnNavEstructuras.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { swapView(ViewState.ESTRUCTURAS); }
        });

        btnNavMejoras.addListener(UISounds.CLICK);
        btnNavMejoras.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) { swapView(ViewState.MEJORAS); }
        });

        btnAjustes.addListener(UISounds.CLICK);
        btnAjustes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                modalLayer.setVisible(true);
            }
        });

        swapView(ViewState.NUCLEO);
    }

    private void initMobilePages(Skin skin) {
        nucleoPageTable = new Table();
        nucleoPageTable.top(); 
        
        Table scoreTableNucleo = new Table();
        labelPP_nucleo = new Label("0 PP", skin, "large");
        labelPP_nucleo.setFontScale(2.8f);
        labelPP_nucleo.setAlignment(Align.center);
        labelPPS_nucleo = new Label("0 PP/seg", skin);
        labelPPS_nucleo.setFontScale(1.8f);
        labelPPS_nucleo.setAlignment(Align.center);
        
        scoreTableNucleo.add(labelPP_nucleo).center().row();
        scoreTableNucleo.add(labelPPS_nucleo).center().padTop(10);
        
        final Image btnNucleoMobile = new Image(ResourceManager.texturaNucleo);
        btnNucleoMobile.setSize(850, 850);
        btnNucleoMobile.setOrigin(425, 425);
        btnNucleoMobile.addAction(Actions.forever(Actions.sequence(
            Actions.scaleTo(1.05f, 1.05f, 1.5f),
            Actions.scaleTo(1.0f, 1.0f, 1.5f)
        )));
        
        btnNucleoMobile.addListener(UISounds.NUCLEO);
        btnNucleoMobile.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                triggerClickEffect(x, y, btnNucleoMobile, 1.6f);
                return true;
            }
        });

        nucleoPageTable.add(scoreTableNucleo).padTop(100).padBottom(15).row();
        nucleoPageTable.add(btnNucleoMobile).size(850, 850).expandY().center();

        estructurasPageTable = new Table();
        estructurasPageTable.top();
        
        Table colEstructurasMobile = new Table();
        colEstructurasMobile.top();

        Map<String, Upgrade> upgrades = game.getGameState().getUpgrades();
        for (Upgrade u : upgrades.values()) {
            if (u instanceof AutomatedUpgrade) {
                colEstructurasMobile.add(buildDynamicShopCard(u)).fillX().padBottom(24).row();
            }
        }

        ScrollPane scrollEst = new ScrollPane(colEstructurasMobile);
        scrollEst.setFadeScrollBars(false);
        scrollEst.setScrollingDisabled(true, false); 
        estructurasPageTable.add(scrollEst).expand().fill().pad(30);

        mejorasPageTable = new Table();
        mejorasPageTable.top();
        
        Table colTiendaMobile = new Table();
        colTiendaMobile.top();

        for (Upgrade u : upgrades.values()) {
            if (u instanceof DirectUpgrade || u instanceof MultiplierUpgrade) {
                colTiendaMobile.add(buildDynamicShopCard(u)).fillX().padBottom(24).row();
            }
        }

        ScrollPane scrollTienda = new ScrollPane(colTiendaMobile);
        scrollTienda.setFadeScrollBars(false);
        scrollTienda.setScrollingDisabled(true, false); 
        mejorasPageTable.add(scrollTienda).expand().fill().pad(30);
    }

    private void swapView(ViewState newState) {
        dynamicAreaTable.clearChildren();
        currentViewState = newState;

        btnNavNucleo.getColor().a = (newState == ViewState.NUCLEO) ? 1.0f : 0.4f;
        btnNavEstructuras.getColor().a = (newState == ViewState.ESTRUCTURAS) ? 1.0f : 0.4f;
        btnNavMejoras.getColor().a = (newState == ViewState.MEJORAS) ? 1.0f : 0.4f;

        scoreTableHud.setVisible(newState != ViewState.NUCLEO);

        switch (currentViewState) {
            case NUCLEO:
                root.setBackground(new TextureRegionDrawable(new TextureRegion(ResourceManager.FondoNucleoAndroid)));
                dynamicAreaTable.add(nucleoPageTable).expand().fill();
                break;
            case ESTRUCTURAS:
                root.setBackground(new TextureRegionDrawable(new TextureRegion(ResourceManager.FondoEstructurasAndroid)));
                dynamicAreaTable.add(estructurasPageTable).expand().fill();
                break;
            case MEJORAS:
                root.setBackground(new TextureRegionDrawable(new TextureRegion(ResourceManager.FondoMejorasAndroid)));
                dynamicAreaTable.add(mejorasPageTable).expand().fill();
                break;
        }
    }

    private Table createMobileSettingsModal(Skin skin) {
        final LocaleManager i18n = LocaleManager.getInstance();
        final AudioManager audio = AudioManager.getInstance();

        Table wrapper = new Table();
        wrapper.center();
        
        Pixmap pixPopup = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixPopup.setColor(new Color(0.15f, 0.1f, 0.25f, 0.95f));
        pixPopup.fill();
        popupBgTexture = new Texture(pixPopup);
        pixPopup.dispose();

        Table popup = new Table();
        popup.setBackground(new TextureRegionDrawable(new TextureRegion(popupBgTexture)));
        popup.pad(50);

        Table header = new Table();
        final Label lblTitle = new Label(i18n.getText("menu_ajustes"), skin, "large");
        lblTitle.setFontScale(2.2f);

        TextButton btnClose = new TextButton("X", skin);
        btnClose.getLabel().setFontScale(2.2f);

        popup.add(lblTitle).left().expandX().padBottom(60);
        popup.add(btnClose).size(120).right().padBottom(60).row();
        
        popup.add(header).fillX().padBottom(60).row();

        Slider.SliderStyle estiloSlider = new Slider.SliderStyle();
        Pixmap bgPix = new Pixmap(1, 15, Pixmap.Format.RGBA8888);
        bgPix.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        bgPix.fill();
        estiloSlider.background = new TextureRegionDrawable(new TextureRegion(new Texture(bgPix)));
        bgPix.dispose();

        Pixmap knobPix = new Pixmap(60, 60, Pixmap.Format.RGBA8888);
        knobPix.setColor(Color.valueOf("1BA1E2"));
        knobPix.fill();
        estiloSlider.knob = new TextureRegionDrawable(new TextureRegion(new Texture(knobPix)));
        knobPix.dispose();

        final Label lblEfectos = new Label(i18n.getText("ajustes_volumen_efectos"), skin);
        lblEfectos.setFontScale(1.4f);
        final Slider sliderEfectos = new Slider(0, 100, 1, false, estiloSlider);
        sliderEfectos.setValue(audio.getSfxVolume() * 100f);
        final Label lblEfectosPct = new Label((int) sliderEfectos.getValue() + "%", skin);
        lblEfectosPct.setFontScale(1.4f);

        final Label lblMusica = new Label(i18n.getText("ajustes_musica"), skin);
        lblMusica.setFontScale(1.4f);
        final Slider sliderMusica = new Slider(0, 100, 1, false, estiloSlider);
        sliderMusica.setValue(audio.getMusicVolume() * 100f);
        final Label lblMusicaPct = new Label((int) sliderMusica.getValue() + "%", skin);
        lblMusicaPct.setFontScale(1.4f);

        final Label lblIdioma = new Label(i18n.getText("ajustes_idioma_label"), skin);
        lblIdioma.setFontScale(1.4f);

        SelectBox.SelectBoxStyle estiloSelect = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        pixmap.fill();
        TextureRegionDrawable bgSelect = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        bgSelect.setLeftWidth(30f);
        bgSelect.setRightWidth(30f);
        estiloSelect.background = bgSelect;
        
        if (skin.has("large", Label.LabelStyle.class)) {
            estiloSelect.font = skin.get("large", Label.LabelStyle.class).font;
        }

        if (estiloSelect.listStyle != null) {
            Pixmap pixListBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixListBg.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
            pixListBg.fill();
            estiloSelect.listStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(pixListBg)));
            pixListBg.dispose();

            Pixmap pixSel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixSel.setColor(new Color(0.3f, 0.3f, 0.5f, 1f));
            pixSel.fill();
            TextureRegionDrawable selectionBg = new TextureRegionDrawable(new TextureRegion(new Texture(pixSel)));
            selectionBg.setTopHeight(25f);
            selectionBg.setBottomHeight(25f);
            selectionBg.setLeftWidth(30f);
            selectionBg.setRightWidth(30f);
            estiloSelect.listStyle.selection = selectionBg;
            
            if (skin.has("large", Label.LabelStyle.class)) {
                estiloSelect.listStyle.font = skin.get("large", Label.LabelStyle.class).font;
            }
        }

        final SelectBox<String> selectIdioma = new SelectBox<String>(estiloSelect);
        pixmap.dispose();
        selectIdioma.setItems("Español", "English");
        selectIdioma.setSelected(game.getGameState().getIdiomaActual().equals("es") ? "Español" : "English");

        final Label lblOrientacion = new Label("Orientación", skin);
        lblOrientacion.setFontScale(1.4f);

        TextButton.TextButtonStyle btnStyle = skin.get(TextButton.TextButtonStyle.class);
        final TextButton btnOrientLeft = new TextButton("<", btnStyle);
        final TextButton btnOrientRight = new TextButton(">", btnStyle);
        btnOrientLeft.getLabel().setFontScale(1.5f);
        btnOrientRight.getLabel().setFontScale(1.5f);

        final Label lblOrientStatus = new Label("Vertical", skin);
        lblOrientStatus.setFontScale(1.4f);
        lblOrientStatus.setAlignment(Align.center);

        Table tableOrientControls = new Table();
        tableOrientControls.add(btnOrientLeft).width(100).height(100);
        tableOrientControls.add(lblOrientStatus).expandX().fillX();
        tableOrientControls.add(btnOrientRight).width(100).height(100);

        popup.add(lblEfectos).left().expandX();
        popup.add(lblEfectosPct).right().padBottom(10).row();
        popup.add(sliderEfectos).colspan(2).fillX().height(60).padBottom(30).row();

        popup.add(lblMusica).left().expandX();
        popup.add(lblMusicaPct).right().padBottom(10).row();
        popup.add(sliderMusica).colspan(2).fillX().height(60).padBottom(30).row();

        popup.add(lblIdioma).left().padBottom(10).row();
        popup.add(selectIdioma).colspan(2).fillX().height(80).padBottom(30).row();

        popup.add(lblOrientacion).left().padBottom(10).row();
        popup.add(tableOrientControls).colspan(2).fillX().height(80).padBottom(40).row();
        
        final TextButton btnMainMenu = new TextButton(i18n.getText("menu_salir"), skin); 
        btnMainMenu.getLabel().setFontScale(2.0f);
        popup.add(btnMainMenu).colspan(2).fillX().height(140).padTop(10);
        
        btnClose.addListener(UISounds.CLICK);
        btnClose.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                modalLayer.setVisible(false);
            }
        });

        sliderEfectos.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderEfectos.getValue() / 100f;
                lblEfectosPct.setText((int) sliderEfectos.getValue() + "%");
                audio.setSfxVolume(vol); 
            }
        });

        sliderMusica.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sliderMusica.getValue() / 100f;
                lblMusicaPct.setText((int) sliderMusica.getValue() + "%");
                audio.setMusicVolume(vol); 
            }
        });

        selectIdioma.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String idioma = selectIdioma.getSelected().equals("Español") ? "es" : "en";
                game.getGameState().setIdiomaActual(idioma);
                i18n.loadLanguage(idioma);

                lblTitle.setText(i18n.getText("menu_ajustes"));
                lblEfectos.setText(i18n.getText("ajustes_volumen_efectos"));
                lblMusica.setText(i18n.getText("ajustes_musica"));
                lblIdioma.setText(i18n.getText("ajustes_idioma_label"));
                btnMainMenu.setText(i18n.getText("menu_salir"));
            }
        });

        btnMainMenu.addListener(UISounds.CLICK);
        btnMainMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.changeScreen(ScreenType.MAIN_MENU);
            }
        });
        
        wrapper.add(popup).width(900);
        return wrapper;
    }

    private void triggerClickEffect(float x, float y, Actor targetActor, float fontScale) {
        game.getGameState().addPendingClick();
        
        double valorClickDouble = game.getGameState().getPpPorClick();
        BigInteger valorClickBig = BigInteger.valueOf((long) valorClickDouble);
        
        Label lblFloating = new Label("+" + PPFormatter.format(valorClickBig), ResourceManager.getSkin());
        lblFloating.setFontScale(fontScale);
        
        Vector2 coords = new Vector2(x, y);
        targetActor.localToStageCoordinates(coords);
        
        float offsetX = MathUtils.random() * 60f - 30f; 
        lblFloating.setPosition(coords.x + offsetX, coords.y);
        
        lblFloating.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0, 100f, 0.8f),
                Actions.sequence(
                    Actions.delay(0.3f),
                    Actions.fadeOut(0.5f)
                )
            ),
            Actions.removeActor()
        ));
        stage.addActor(lblFloating);
    }

    private Table buildDynamicShopCard(final Upgrade upgrade) {
        Skin skin = ResourceManager.getSkin();
        final String id = upgrade.getId();

        float fontScale = 1.8f;
        float iconSize = 140f;
        float padCard = 32f;

        Label lblNombre   = new Label(i18n.getText(upgrade.getNameKey()), skin);
        lblNombre.setFontScale(fontScale);
        
        Label lblCoste    = new Label(formatCoste(upgrade.getCurrentCost()), skin);
        lblCoste.setFontScale(fontScale);

        Label lblCantidad = new Label("x" + upgrade.getQuantity(), skin);
        lblCantidad.setFontScale(fontScale);
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
        shopBuyButtons.put(id, btnCard);

        btnCard.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (upgrade.canAfford(game.getGameState().getPpActual())) {
                    AudioManager.getInstance().playSoundWithPitch(AudioManager.SoundEffect.PURCHASE, 0.95f, 1.05f);
                    purchaseService.comprar(id, game.getGameState());
                } else {
                    AudioManager.getInstance().playSound(AudioManager.SoundEffect.ERROR);
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

        String descKey = upgrade.getNameKey() + "_desc";
        String desc = "";
        try {
            desc = i18n.getText(descKey);
        } catch (Exception e) { }
        
        if (desc != null && !desc.isEmpty() && !desc.equals(descKey) && !desc.startsWith("???")) {
            TooltipManager tooltipManager = TooltipManager.getInstance();
            tooltipManager.initialTime = 1.0f; 
            tooltipManager.subsequentTime = 1.0f; 
            tooltipManager.resetTime = 0f; 
   
            TextTooltip.TextTooltipStyle tooltipStyle = new TextTooltip.TextTooltipStyle();
            tooltipStyle.label = skin.get(Label.LabelStyle.class);
            tooltipStyle.background = new TextureRegionDrawable(new TextureRegion(tooltipBgTexture));
            tooltipStyle.wrapWidth = 500f; 
            
            TextTooltip tooltip = new TextTooltip(desc, tooltipManager, tooltipStyle);
            tooltip.getContainer().pad(10f); 
            tooltip.getActor().setFontScale(1.5f);
            
            btnCard.addListener(tooltip);
        }

        Image imgIcono = new Image(ResourceManager.texturaIconoPrueba);

        Table tablaTextos = new Table();
        tablaTextos.add(lblNombre).left().padBottom(10).row();
        tablaTextos.add(lblCoste).left();

        btnCard.pad(padCard);
        btnCard.add(imgIcono).size(iconSize, iconSize).padRight(padCard * 2);
        btnCard.add(tablaTextos).expandX().left();
        btnCard.add(lblCantidad).width(160).right().padLeft(padCard);

        shopCards.put(id, btnCard);
        return btnCard;
    }

    private void updateHUD(BigInteger pp, double pps) {
        String ppText = PPFormatter.format(pp) + " PP";
        String ppsText = PPFormatter.formatRate(pps) + " PP/seg";
        
        labelPP_hud.setText(ppText);
        labelPPS_hud.setText(ppsText);
        labelPP_nucleo.setText(ppText);
        labelPPS_nucleo.setText(ppsText);
    }

    private void updateShop() {
        Map<String, Upgrade> upgrades = game.getGameState().getUpgrades();
        List<String> toRemove = new ArrayList<String>();

        for (Map.Entry<String, Table> entry : shopCards.entrySet()) {
            String  id   = entry.getKey();
            Table   card = entry.getValue();
            Upgrade u    = upgrades.get(id);
            if (u == null) continue;

            boolean isOneTime = (u instanceof DirectUpgrade) || (u instanceof MultiplierUpgrade);

            if (isOneTime && u.getQuantity() >= 1) {
                card.remove();
                toRemove.add(id);
                continue;
            }

            Label quantity = shopQuantityLabels.get(id);
            if (quantity != null) {
                quantity.setText("x" + u.getQuantity());
            }

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

    private String formatCoste(double cost) {
        BigInteger costBig = BigInteger.valueOf((long) cost);
        return i18n.getTextVar("tienda_coste", PPFormatter.format(costBig));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Se actualiza el progreso y los puntos constantemente, no se detiene por el modal
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
        if (darkeningTexture != null) darkeningTexture.dispose();
        if (navBgTexture != null) navBgTexture.dispose();
        if (popupBgTexture != null) popupBgTexture.dispose();
        if (tooltipBgTexture != null) tooltipBgTexture.dispose();
    }
}