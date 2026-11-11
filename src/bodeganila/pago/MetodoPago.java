package bodeganila.pago;

import bodeganila.excepciones.PagoInsuficienteException;
import java.math.BigDecimal;

/** Contrato que cumple toda forma de pago. */
public sealed interface MetodoPago permits Efectivo, BilleteraDigital {

    /** Nombre para mostrar en el ticket y en los reportes. */
    String getNombre();

    /**
     * Procesa el pago del total indicado.
     * @return el vuelto a entregar (cero si no aplica)
     * @throws PagoInsuficienteException si el pago no cubre el total
     */
    BigDecimal procesarPago(BigDecimal total) throws PagoInsuficienteException;
}
