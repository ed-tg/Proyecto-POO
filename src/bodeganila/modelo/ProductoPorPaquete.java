package bodeganila.modelo;

import bodeganila.util.Dinero;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Se vende por paquete o sachet; guarda cuántas unidades trae cada uno. */
public final class ProductoPorPaquete extends Producto {

    private final int unidadesPorPaquete;

    public ProductoPorPaquete(String codigo, String nombre, Categoria categoria,
                              BigDecimal precio, double stock, double stockMinimo,
                              int unidadesPorPaquete) {
        super(codigo, nombre, categoria, precio, stock, stockMinimo);
        if (unidadesPorPaquete <= 0) {
            throw new IllegalArgumentException("Las unidades por paquete deben ser mayores a 0");
        }
        this.unidadesPorPaquete = unidadesPorPaquete;
    }

    public int getUnidadesPorPaquete() {
        return unidadesPorPaquete;
    }

    /** Precio referencial de una pieza suelta (precio del paquete / unidades). */
    public BigDecimal getPrecioPorUnidadSuelta() {
        return getPrecio().divide(BigDecimal.valueOf(unidadesPorPaquete), 2, RoundingMode.HALF_UP);
    }

    @Override
    public void validarCantidad(double cantidad) {
        super.validarCantidad(cantidad);
        if (cantidad != Math.floor(cantidad)) {
            throw new IllegalArgumentException("Este producto se vende en paquetes enteros");
        }
    }

    @Override
    public BigDecimal calcularSubtotal(double cantidad, BigDecimal precioUnitario) {
        validarCantidad(cantidad);
        return Dinero.redondear(precioUnitario.multiply(BigDecimal.valueOf((long) cantidad)));
    }

    @Override
    public UnidadMedida getUnidadMedida() {
        return UnidadMedida.PAQUETE;
    }
}
