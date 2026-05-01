package com.jovellanos.clicker.persistence;

import com.badlogic.gdx.Gdx;
import com.jovellanos.clicker.core.GameState;
import java.math.BigDecimal;
import java.math.BigInteger;

/*
    ===============================================
    OfflineProgressCalc — Cálculo de progreso inactivo
    ===============================================
    Se encarga de calcular los recursos generados
    en el tiempo que transcurrio desde que cerraste
    y volviste a abrir la aplicación.
    Utiliza el timestamp del último guardado (ultimoGuardado).
*/

public class OfflineProgressCalc {

    /**
     * Calcula y añade los puntos generados mientras el juego estuvo cerrado.
     * 
     * @return La cantidad de PP generados (TODO: agregar pop-up de puntos
     *         generados).
     */

    public static BigInteger procesar(GameState gameState) {
        long ultimoGuardado = gameState.getUltimoGuardado();

        if (ultimoGuardado <= 0) {
            return BigInteger.ZERO;
        }

        long tiempoActual = System.currentTimeMillis();
        
        double segundosAusente = (tiempoActual - ultimoGuardado) / 1000.0; //Convertimos la diferencia a segundos

        double pps = gameState.getPpPorSegundo();

        // Solo se otorga recompensa si han pasado más de 10 segundos (para evitar  abusos de cerrar y abrir)
        if (segundosAusente > 10.0 && pps > 0) {

            BigDecimal ganancia = BigDecimal.valueOf(pps)
                    .multiply(BigDecimal.valueOf(segundosAusente));

            BigInteger gananciaEntera = ganancia.toBigInteger();

            if (gananciaEntera.compareTo(BigInteger.ZERO) > 0) {
                // Inyectamos los puntos al núcleo de la partida
                gameState.addPP(gananciaEntera);

                Gdx.app.log("Progreso offline:", "Ausencia: " + (int) segundosAusente +
                        "s. Ganancia inyectada: " + gananciaEntera + " PP.");

                return gananciaEntera;
            }
        }

        return BigInteger.ZERO;
    }
}
