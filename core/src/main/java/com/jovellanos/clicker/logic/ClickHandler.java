package com.jovellanos.clicker.logic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

/*
    ===============================================
    ClickHandler — Procesador de Clics
    ===============================================
    Encapsula toda la lógica de negocio relacionada con los clics
    del jugador. El Logic Thread delega en este componente el cálculo
    de PP generadas por clic, incluyendo críticos y combo.

    ===============================================
    Por qué BigInteger en el retorno
    ===============================================
    ppPorClick puede ser muy grande con muchas mejoras directas.
    Multiplicado por un combo x2 y un crítico x2 sobre un lote de
    clics, el resultado puede superar Long.MAX_VALUE. Devolver
    BigInteger garantiza que ningún valor se pierde por overflow.

    El cálculo interno usa double para la aritmética de punto flotante
    (multiplicadores de combo, probabilidad de crítico) y solo convierte
    a BigInteger al final para preservar los decimales durante el cálculo.

*/
public class ClickHandler {

    private BigDecimal acumuladoDecimal = BigDecimal.ZERO;

    /**
     * Procesa un lote de clics y devuelve las PP totales.
     *
     * @param clicks     Número de clics (≥ 1)
     * @param ppPorClick PP base por clic
     * @return           PP totales como BigInteger
     */
    public BigInteger procesarClics(long clicks, double ppPorClick) {
        if (clicks <= 0) return BigInteger.ZERO;

        // 1. Añadir nuevo valor
        BigDecimal incremento = BigDecimal.valueOf(ppPorClick)
                .multiply(BigDecimal.valueOf(clicks));

        acumuladoDecimal = acumuladoDecimal.add(incremento);

        // 2. Extraer parte entera
        BigInteger parteEntera = acumuladoDecimal.toBigInteger();

        // 3. Restar lo entregado (dejar el sobrante)
        acumuladoDecimal = acumuladoDecimal.subtract(new BigDecimal(parteEntera));

        return parteEntera;
    }
}