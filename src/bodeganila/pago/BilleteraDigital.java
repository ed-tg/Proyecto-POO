package bodeganila.pago;

import java.math.BigDecimal;

/** Yape o Plin: solo se registra (no se conecta con la app real) y nunca hay vuelto. */
public final class BilleteraDigital implements MetodoPago {

    public enum Proveedor {
        YAPE("Yape"), PLIN("Plin");

        private final String nombre;

        Proveedor(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    private final Proveedor proveedor;
    private final String numeroOperacion;   // opcional

    public BilleteraDigital(Proveedor proveedor, String numeroOperacion) {
        if (proveedor == null) {
            throw new IllegalArgumentException("Debe indicar Yape o Plin");
        }
        this.proveedor = proveedor;
        this.numeroOperacion = (numeroOperacion == null || numeroOperacion.trim().isEmpty())
                ? null : numeroOperacion.trim();
    }

    public String getNumeroOperacion() {
        return numeroOperacion;
    }

    @Override
    public String getNombre() {
        return proveedor.getNombre();
    }

    @Override
    public BigDecimal procesarPago(BigDecimal total) {
        return BigDecimal.ZERO;
    }
}
