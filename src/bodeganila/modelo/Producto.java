package bodeganila.modelo;

import bodeganila.excepciones.StockInsuficienteException;
import bodeganila.util.Dinero;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * Clase abstracta: contiene lo común a todo producto. Las subclases definen la forma de venta
 * (por unidad, por paquete, por peso). La categoría es un dato (enum), no una subclase.
 *
 * Convención de stock: se mide en la misma unidad en que se vende
 * (unidades, paquetes o kilogramos según el tipo de producto).
 */
public abstract sealed class Producto implements Comparable<Producto>
        permits ProductoPorUnidad, ProductoPorPaquete, ProductoPorPeso {

    private final String codigo;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precio;
    private BigDecimal precioCosto;     // opcional (null = no se conoce)
    private double stock;
    private double stockMinimo;
    private boolean activo = true;      // baja lógica

    protected Producto(String codigo, String nombre, Categoria categoria,
                       BigDecimal precio, double stock, double stockMinimo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede estar vacío");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.codigo = codigo.trim().toUpperCase();
        setNombre(nombre);
        setCategoria(categoria);
        setPrecio(precio);
        setStockMinimo(stockMinimo);
        this.stock = stock;
    }

    // ---------------------------------------------------- comportamiento polimórfico

    /** Cada subclase define cómo calcula el subtotal con un precio unitario dado. */
    public abstract BigDecimal calcularSubtotal(double cantidad, BigDecimal precioUnitario);

    public abstract UnidadMedida getUnidadMedida();

    /** Subtotal con el precio actual del producto. */
    public final BigDecimal calcularSubtotal(double cantidad) {
        return calcularSubtotal(cantidad, precio);
    }

    /** Validación base; las subclases la refuerzan (entera, peso mínimo, etc.). */
    public void validarCantidad(double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }

    // --------------------------------------------------------------------- stock

    public boolean hayStock(double cantidad) {
        return stock >= cantidad;
    }

    public void aumentarStock(double cantidad) {
        validarCantidad(cantidad);
        stock = redondearStock(stock + cantidad);
    }

    public void descontarStock(double cantidad) throws StockInsuficienteException {
        validarCantidad(cantidad);
        if (!hayStock(cantidad)) {
            throw new StockInsuficienteException(nombre, stock, cantidad);
        }
        stock = redondearStock(stock - cantidad);
    }

    public boolean estaBajoStockMinimo() {
        return stock <= stockMinimo;
    }

    /** Evita errores de coma flotante al sumar/restar kilos (0.1 + 0.2). */
    private static double redondearStock(double valor) {
        return Math.round(valor * 1000.0) / 1000.0;
    }

    // ------------------------------------------------------------------- ganancia

    /** Ganancia por unidad vendida, o null si no se conoce el costo. */
    public BigDecimal getMargenUnitario() {
        return precioCosto == null ? null : precio.subtract(precioCosto);
    }

    // ------------------------------------------------------ getters y setters

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Categoria getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getPrecioCosto() { return precioCosto; }
    public double getStock() { return stock; }
    public double getStockMinimo() { return stockMinimo; }
    public boolean isActivo() { return activo; }

    public final void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public final void setCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        this.categoria = categoria;
    }

    public final void setPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        this.precio = Dinero.redondear(precio);
    }

    public final void setPrecioCosto(BigDecimal precioCosto) {
        if (precioCosto != null && precioCosto.signum() < 0) {
            throw new IllegalArgumentException("El precio de costo no puede ser negativo");
        }
        this.precioCosto = precioCosto == null ? null : Dinero.redondear(precioCosto);
    }

    public final void setStockMinimo(double stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        this.stockMinimo = stockMinimo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** Cantidad como texto según la unidad: 3 und, 1.25 kg. */
    public String formatearCantidad(double cantidad) {
        return getUnidadMedida().admiteDecimales()
                ? String.format(Locale.US, "%.2f", cantidad)
                : String.valueOf((long) cantidad);
    }

    /** Orden natural: alfabético por nombre y, si empatan, por código. */
    @Override
    public int compareTo(Producto otro) {
        int c = nombre.compareToIgnoreCase(otro.nombre);
        return c != 0 ? c : codigo.compareTo(otro.codigo);
    }

    // ------------------------------------------------------- equals / hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        return codigo.equals(((Producto) o).codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s por %s | stock: %s %s",
                codigo, nombre, categoria, Dinero.formatear(precio),
                getUnidadMedida().getAbreviatura(),
                formatearCantidad(stock), getUnidadMedida().getAbreviatura());
    }
}
