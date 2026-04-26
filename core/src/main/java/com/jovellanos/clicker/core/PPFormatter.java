package com.jovellanos.clicker.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.MessageFormat;

/**
 * PPFormatter — Formateador de números grandes para HUD
 */
public final class PPFormatter {

    // ────────────── Umbrales y sufijos ──────────────
    // Mayor a menor para que el loop encuentre primero el sufijo correcto
    private static final BigInteger[] THRESHOLDS = {
            pow10(33), pow10(30), pow10(27), pow10(24),
            pow10(21), pow10(18), pow10(15), pow10(12),
            pow10(9), pow10(6), pow10(3)
    };

    private static final String[] SUFFIXES = {
            "Dc", "No", "Oc", "Sp",
            "Sx", "Qi", "Qa", "T",
            "B", "M", "K"
    };

    private PPFormatter() {
    } // No instanciable

    // ────────────── Métodos públicos ──────────────

    /** Formatea un BigInteger de PP con sufijo de escala */
    public static String format(BigInteger value) {
        if (value == null || value.signum() < 0)
            return "0";

        // Valores menores que 1000: mostrar entero
        if (value.compareTo(BigInteger.valueOf(1000)) < 0) {
            return value.toString();
        }

        // Valores gigantes: notación científica
        if (value.compareTo(THRESHOLDS[0]) >= 0) {
            return toScientific(value);
        }

        // Recorrer umbrales de mayor a menor
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (value.compareTo(THRESHOLDS[i]) >= 0) {
                BigDecimal divisor = new BigDecimal(THRESHOLDS[i]);
                BigDecimal result = new BigDecimal(value)
                        .divide(divisor, 3, RoundingMode.DOWN);
                return formatDecimal(result) + SUFFIXES[i];
            }
        }

        // Fallback (no debería llegar aquí)
        return value.toString();
    }

    /** Formatea PP/s expresado como double */
    public static String formatRate(double pps) {
        if (pps < 1000.0) {
            return String.format("%.1f", pps).replace('.', ',');
        }
        return format(BigDecimal.valueOf(pps).toBigInteger());
    }

    /** Formatea para i18n usando placeholders {0} en properties */
    public static String formatForHUD(String pattern, BigInteger value) {
        String formattedPP = format(value);
        return MessageFormat.format(pattern, formattedPP);
    }

    // ────────────── Helpers privados ──────────────

    /** Redondea y elimina ceros innecesarios, hasta 3 cifras significativas */
    private static String formatDecimal(BigDecimal value) {
        BigDecimal rounded = value.round(new MathContext(3));
        String str = rounded.stripTrailingZeros().toPlainString();
        return str.replace('.', ','); // coma decimal en español
    }

    /** Notación científica para valores ≥ 10^33 */
    private static String toScientific(BigInteger value) {
        BigDecimal bd = new BigDecimal(value);
        int exp = bd.precision() - 1;
        BigDecimal mantissa = bd.movePointLeft(exp).round(new MathContext(3));
        return mantissa.stripTrailingZeros().toPlainString().replace('.', ',') + "e" + exp;
    }

    private static BigInteger pow10(int exp) {
        return BigInteger.TEN.pow(exp);
    }
}