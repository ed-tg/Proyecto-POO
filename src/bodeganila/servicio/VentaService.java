package bodeganila.servicio;

import bodeganila.excepciones.*;
import bodeganila.modelo.*;
import bodeganila.pago.MetodoPago;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Orquesta el proceso de venta, para que Venta no se llene de lógica.
 * Regla clave: el stock se descuenta SOLO al confirmar el pago.
 */
public class VentaService {

    private final Inventario inventario;
    private final List<Venta> ventas = new ArrayList<>();
    private Venta ventaActual;
    private int siguienteNumero = 1;

    public VentaService(Inventario inventario) {
        this.inventario = inventario;
    }

    public Venta iniciarVenta() {
        ventaActual = new Venta(siguienteNumero);
        return ventaActual;
    }

    /** Valida existencia y stock (contando lo que ya está en la venta) y agrega la línea. */
    public void agregarProducto(String codigo, double cantidad)
            throws ProductoNoEncontradoException, StockInsuficienteException {
        exigirVentaActual();
        Producto producto = inventario.buscarPorCodigo(codigo);
        producto.validarCantidad(cantidad);
        double total = ventaActual.getCantidadDe(producto) + cantidad;
        if (!producto.hayStock(total)) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), total);
        }
        ventaActual.agregarDetalle(producto, cantidad);
    }

    public void quitarProducto(String codigo) {
        exigirVentaActual();
        ventaActual.quitarDetalle(codigo);
    }

    /**
     * Orden de pasos (así un fallo nunca deja el inventario a medias):
     *  1) verifica el stock de todas las líneas, sin modificarlo
     *  2) cobra: si el pago falla, no se toca nada
     *  3) recién entonces descuenta el stock
     */
    public Venta finalizarVenta(MetodoPago metodo)
            throws VentaVaciaException, StockInsuficienteException,
                   PagoInsuficienteException, ProductoNoEncontradoException {
        exigirVentaActual();
        if (ventaActual.estaVacia()) {
            throw new VentaVaciaException();
        }
        for (DetalleVenta d : ventaActual.getDetalles()) {
            inventario.verificarStock(d.getProducto().getCodigo(), d.getCantidad());
        }
        ventaActual.completar(metodo);
        for (DetalleVenta d : ventaActual.getDetalles()) {
            inventario.descontarPorVenta(d.getProducto().getCodigo(), d.getCantidad(), ventaActual.getNumero());
        }
        Venta terminada = ventaActual;
        ventas.add(terminada);
        siguienteNumero++;
        ventaActual = null;
        return terminada;
    }

    /** Descarta la venta en curso; como el stock no se había tocado, no hay nada que revertir. */
    public void cancelarVenta() {
        ventaActual = null;
    }

    /** Anula una venta ya cobrada y devuelve el stock. */
    public void anularVenta(int numero) throws ProductoNoEncontradoException {
        Venta venta = buscarVenta(numero);
        venta.anular();
        for (DetalleVenta d : venta.getDetalles()) {
            inventario.devolverPorAnulacion(d.getProducto().getCodigo(), d.getCantidad(), numero);
        }
    }

    public Venta buscarVenta(int numero) {
        for (Venta v : ventas) {
            if (v.getNumero() == numero) {
                return v;
            }
        }
        throw new IllegalArgumentException("No existe la venta N° " + numero);
    }

    /** Última venta cobrada (List es una SequencedCollection desde Java 21: getLast()). */
    public Optional<Venta> getUltimaVenta() {
        return ventas.isEmpty() ? Optional.empty() : Optional.of(ventas.getLast());
    }

    /** Ventas de la más reciente a la más antigua (reversed() es una vista, no una copia). */
    public List<Venta> getVentasRecientes() {
        return Collections.unmodifiableList(ventas.reversed());
    }

    public Venta getVentaActual() { return ventaActual; }
    public List<Venta> getVentas() { return Collections.unmodifiableList(ventas); }

    private void exigirVentaActual() {
        if (ventaActual == null) {
            throw new IllegalStateException("No hay una venta en curso");
        }
    }
}
