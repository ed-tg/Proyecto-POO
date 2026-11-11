package bodeganila.modelo;

import bodeganila.util.Dinero;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Se vende a granel (arroz, azúcar): el precio es por kilo y la cantidad es decimal. */
public final class ProductoPorPeso extends Producto {

    public static final double PESO_MINIMO = 0.05;   // 50 gramos

    public ProductoPorPeso(String codigo, String nombre, Categoria categoria,
                           BigDecimal precioPorKilo, double stock, double stockMinimo) {
        super(codigo, nombre, categoria, precioPorKilo, stock, stockMinimo);
    }

    @Override
    public void validarCantidad(double cantidad) {
        super.validarCantidad(cantidad);
        if (cantidad < PESO_MINIMO) {
            throw new IllegalArgumentException("El peso mínimo de venta es " + PESO_MINIMO + " kg");
        }
    }

    @Override
    public BigDecimal calcularSubtotal(double cantidad, BigDecimal precioPorKilo) {
        validarCantidad(cantidad);
        BigDecimal kilos = BigDecimal.valueOf(cantidad).setScale(3, RoundingMode.HALF_UP);  // al gramo
        return Dinero.redondear(precioPorKilo.multiply(kilos));
    }

    @Override
    public UnidadMedida getUnidadMedida() {
        return UnidadMedida.KILOGRAMO;
    }
}
