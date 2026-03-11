package com.jovellanos.clicker.i18n;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import java.util.Locale;

public class LocaleManager {
    private static LocaleManager instance; //Singleton
    private I18NBundle bundle;

    private LocaleManager(){
        loadLanguage("es");
    }

    public static LocaleManager getInstance(){ //Acceso al localManager
        if (instance == null) {
            instance = new LocaleManager(); //Si no hay localManager creado, lo crea
        }
        return instance;
    }

    public void loadLanguage(String idioma ){
        FileHandle baseFileHandle = Gdx.files.internal("i18n/textos"); //Busca el archivo de idioma
        Locale locale = new Locale(idioma);
        bundle = I18NBundle.createBundle(baseFileHandle, locale); //El diccionario se abre en la ram
    }

    public String getText(String key){ //Para textos simples (Sin variables)
        String clave = ("!" + key + "!");
        if (bundle != null) {
            try{
                clave = bundle.get(key);
            } catch(Exception ex){
                clave = ("!" + key + "!"); //Por si se escribe mal la clave desde el font
            }
        }
        return clave;
    }

    public String getTextVar(String key, Object... args){ //Para textos con variables (Object... te permite meter tantos valores como quieras)
        String clave = ("!" + key + "!");
        if (bundle != null) {
            try {
                clave = bundle.format(key, args);
            } catch (Exception e) {
                clave = ("!" + key + "!");
            }
        }
        return clave;
    }
    
}
