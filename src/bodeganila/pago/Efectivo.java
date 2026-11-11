package bodeganila.pago;

import bodeganila.excepciones.PagoInsuficienteException;
import bodeganila.util.Dinero;
import java.math.BigDecimal;

/** Pago en efectivo: recibe el monto entregado y calcula el vuelto. */
public final class Efectivo implements MetodoPago {

    private final BigDecimal montoRecibido;

    public Efectivo(BigDecimal montoRecibido) {
        if (montoRecibido == null || montoRecibido.signum() < 0) {
            throw new IllegalArgumentException("El monto recibido no es válido");
        }
        this.montoRecibido = Dinero.redondear(montoRecibido);
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    @Override
    public String getNombre() {
        return "Efectivo";
    }

    @Override
    public BigDecimal procesarPago(BigDecimal total) throws PagoInsuficienteException {
        if (montoRecibido.compareTo(total) < 0) {
            throw new PagoInsuficienteException(total, montoRecibido);
        }
        return Dinero.redondear(montoRecibido.subtract(total));
    }
}
