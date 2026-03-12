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

    //Acceso al localManager
    public static LocaleManager getInstance(){ 
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

    //Para textos simples (Sin variables)
    public String getText(String key){ 
        String clave = ("!" + key + "!");
        if (bundle != null) {
            try{
                clave = bundle.get(key);
            } catch(Exception ex){
                clave = ("!" + key + "!"); //Por si se escribe mal la clave desde el front
            }
        }
        return clave;
    }

    //Para textos con variables 
    public String getTextVar(String key, Object... args){ //Object... te permite meter tantos valores como quieras del tipo que quieras
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
