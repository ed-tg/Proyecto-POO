package bodeganila.modelo;

import bodeganila.util.Dinero;
import java.math.BigDecimal;

/** Se vende de uno en uno (galletas, gaseosas). */
public final class ProductoPorUnidad extends Producto {

    public ProductoPorUnidad(String codigo, String nombre, Categoria categoria,
                             BigDecimal precio, double stock, double stockMinimo) {
        super(codigo, nombre, categoria, precio, stock, stockMinimo);
    }

    @Override
    public void validarCantidad(double cantidad) {
        super.validarCantidad(cantidad);
        if (cantidad != Math.floor(cantidad)) {
            throw new IllegalArgumentException("Este producto se vende en unidades enteras");
        }
    }

    @Override
    public BigDecimal calcularSubtotal(double cantidad, BigDecimal precioUnitario) {
        validarCantidad(cantidad);
        return Dinero.redondear(precioUnitario.multiply(BigDecimal.valueOf((long) cantidad)));
    }

    @Override
    public UnidadMedida getUnidadMedida() {
        return UnidadMedida.UNIDAD;
    }
}
