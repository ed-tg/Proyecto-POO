package bodeganila.excepciones;

import bodeganila.util.Dinero;
import java.math.BigDecimal;

public class PagoInsuficienteException extends BodegaException {

    private final BigDecimal faltante;

    public PagoInsuficienteException(BigDecimal total, BigDecimal recibido) {
        super("Pago insuficiente: total " + Dinero.formatear(total) + ", recibido "
              + Dinero.formatear(recibido) + ", faltan " + Dinero.formatear(total.subtract(recibido)));
        this.faltante = total.subtract(recibido);
    }

    public BigDecimal getFaltante() {
        return faltante;
    }
}
