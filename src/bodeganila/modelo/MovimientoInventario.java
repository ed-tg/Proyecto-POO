package bodeganila.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Registro inmutable de una entrada o salida de stock. */
public class MovimientoInventario {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Producto producto;
    private final TipoMovimiento tipo;
    private final double cantidad;
    private final LocalDateTime fecha;
    private final String motivo;
    private final double stockResultante;

    public MovimientoInventario(Producto producto, TipoMovimiento tipo, double cantidad,
                                String motivo, double stockResultante) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = LocalDateTime.now();
        this.motivo = motivo;
        this.stockResultante = stockResultante;
    }

    public Producto getProducto() { return producto; }
    public TipoMovimiento getTipo() { return tipo; }
    public double getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public double getStockResultante() { return stockResultante; }

    @Override
    public String toString() {
        return String.format("%s | %-12s | %-20s | cant: %s | stock: %s | %s",
                fecha.format(FORMATO), tipo, producto.getNombre(),
                producto.formatearCantidad(cantidad),
                producto.formatearCantidad(stockResultante), motivo);
    }
}
