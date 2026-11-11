package bodeganila.modelo;

import java.math.BigDecimal;

/** Una línea de la venta. Guarda el precio aplicado: si el precio cambia después, no se altera. */
public class DetalleVenta {

    private final Producto producto;
    private double cantidad;
    private final BigDecimal precioAplicado;

    /** Línea nueva: toma el precio actual del producto. */
    public DetalleVenta(Producto producto, double cantidad) {
        this(producto, cantidad, producto.getPrecio());
    }

    /** Para reconstruir una línea ya guardada (por ejemplo, desde la base de datos). */
    public DetalleVenta(Producto producto, double cantidad, BigDecimal precioAplicado) {
        producto.validarCantidad(cantidad);
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioAplicado = precioAplicado;
    }

    public void agregarCantidad(double extra) {
        producto.validarCantidad(cantidad + extra);
        cantidad += extra;
    }

    /** El cálculo lo hace la subclase de Producto (polimorfismo). */
    public BigDecimal getSubtotal() {
        return producto.calcularSubtotal(cantidad, precioAplicado);
    }

    public Producto getProducto() { return producto; }
    public double getCantidad() { return cantidad; }
    public BigDecimal getPrecioAplicado() { return precioAplicado; }

    public String getCantidadTexto() {
        return producto.formatearCantidad(cantidad) + " " + producto.getUnidadMedida().getAbreviatura();
    }
}
