package com.jovellanos.clicker;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");


        System.out.println("=== INICIANDO SISTEMA I18N (A3) ===");
    
    //Prueba texto normal
    System.out.println("TEXTO SIMPLE: " + com.jovellanos.clicker.i18n.LocaleManager.getInstance().getText("menu_nueva_partida"));
    
    //Prueva texto con variable
    System.out.println("TEXTO VARIABLE: " + com.jovellanos.clicker.i18n.LocaleManager.getInstance().getTextVar("tienda_coste", "250"));
    
    System.out.println("===================================");
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
