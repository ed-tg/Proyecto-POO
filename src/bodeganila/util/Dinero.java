package bodeganila.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Utilidades para manejar dinero con BigDecimal (evita errores de redondeo de double). */
public final class Dinero {

    private Dinero() { }

    public static BigDecimal redondear(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatear(BigDecimal monto) {
        return "S/ " + redondear(monto).toPlainString();
    }
}
