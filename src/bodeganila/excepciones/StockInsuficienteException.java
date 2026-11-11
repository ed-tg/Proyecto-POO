package bodeganila.excepciones;

import java.util.Locale;

public class StockInsuficienteException extends BodegaException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public StockInsuficienteException(String producto, double disponible, double solicitado) {
        super("Stock insuficiente de " + producto + ": hay " + fmt(disponible)
              + ", se pidieron " + fmt(solicitado));
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.US, "%.2f", v);
    }
}
