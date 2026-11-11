package bodeganila.modelo;

import bodeganila.excepciones.PagoInsuficienteException;
import bodeganila.excepciones.VentaVaciaException;
import bodeganila.pago.BilleteraDigital;
import bodeganila.pago.Efectivo;
import bodeganila.pago.MetodoPago;
import bodeganila.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Una transacción completa. Solo guarda datos y reglas propias; el proceso lo orquesta VentaService. */
public class Venta implements Iterable<DetalleVenta> {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int numero;
    private final LocalDateTime fecha;
    private final List<DetalleVenta> detalles = new ArrayList<>();
    private MetodoPago metodoPago;
    private BigDecimal vuelto = BigDecimal.ZERO;
    private EstadoVenta estado = EstadoVenta.EN_CURSO;

    public Venta(int numero) {
        this.numero = numero;
        this.fecha = LocalDateTime.now();
    }

    /** Si el producto ya está en la venta, suma la cantidad en lugar de duplicar la línea. */
    public void agregarDetalle(Producto producto, double cantidad) {
        exigirEnCurso();
        for (DetalleVenta d : detalles) {
            if (d.getProducto().equals(producto)) {
                d.agregarCantidad(cantidad);
                return;
            }
        }
        detalles.add(new DetalleVenta(producto, cantidad));
    }

    public void quitarDetalle(String codigo) {
        exigirEnCurso();
        boolean quitado = detalles.removeIf(d -> d.getProducto().getCodigo().equalsIgnoreCase(codigo.trim()));
        if (!quitado) {
            throw new IllegalArgumentException("El producto " + codigo + " no está en la venta");
        }
    }

    public double getCantidadDe(Producto producto) {
        for (DetalleVenta d : detalles) {
            if (d.getProducto().equals(producto)) {
                return d.getCantidad();
            }
        }
        return 0;
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : detalles) {
            total = total.add(d.getSubtotal());
        }
        return total;
    }

    /** Cobra la venta. Si el pago falla, la venta sigue en curso y nada cambia. */
    public void completar(MetodoPago metodo) throws VentaVaciaException, PagoInsuficienteException {
        exigirEnCurso();
        if (detalles.isEmpty()) {
            throw new VentaVaciaException();
        }
        BigDecimal vueltoCalculado = metodo.procesarPago(getTotal());
        this.metodoPago = metodo;
        this.vuelto = vueltoCalculado;
        this.estado = EstadoVenta.COMPLETADA;
    }

    public void anular() {
        if (estado != EstadoVenta.COMPLETADA) {
            throw new IllegalStateException("Solo se puede anular una venta completada");
        }
        estado = EstadoVenta.ANULADA;
    }

    private void exigirEnCurso() {
        if (estado != EstadoVenta.EN_CURSO) {
            throw new IllegalStateException("La venta ya no admite cambios (estado: " + estado + ")");
        }
    }

    public String generarTicket() {
        StringBuilder sb = new StringBuilder();
        sb.append("=============== BODEGA NILA ===============\n");
        sb.append(String.format("Venta N° %d      %s%n", numero, fecha.format(FORMATO)));
        sb.append("Estado: ").append(estado).append("\n");
        sb.append("-------------------------------------------\n");
        for (DetalleVenta d : detalles) {
            sb.append(String.format("%s%n   %s x %s = %s%n",
                    d.getProducto().getNombre(), d.getCantidadTexto(),
                    Dinero.formatear(d.getPrecioAplicado()), Dinero.formatear(d.getSubtotal())));
        }
        sb.append("-------------------------------------------\n");
        sb.append("TOTAL: ").append(Dinero.formatear(getTotal())).append("\n");
        if (metodoPago != null) {
            sb.append("Pago: ").append(metodoPago.getNombre()).append("\n");
            // switch exhaustivo: el compilador sabe que MetodoPago solo tiene estas dos formas
            String extra = switch (metodoPago) {
                case Efectivo e -> "Recibido: " + Dinero.formatear(e.getMontoRecibido());
                case BilleteraDigital b -> b.getNumeroOperacion() == null
                        ? "Sin N° de operación" : "N° de operación: " + b.getNumeroOperacion();
            };
            sb.append(extra).append("\n");
            sb.append("Vuelto: ").append(Dinero.formatear(vuelto)).append("\n");
        }
        sb.append("===========================================");
        return sb.toString();
    }

    /** Permite recorrer la venta con for-each: for (DetalleVenta d : venta). */
    @Override
    public Iterator<DetalleVenta> iterator() {
        return Collections.unmodifiableList(detalles).iterator();
    }

    public int getNumero() { return numero; }
    public LocalDateTime getFecha() { return fecha; }
    public List<DetalleVenta> getDetalles() { return Collections.unmodifiableList(detalles); }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public BigDecimal getVuelto() { return vuelto; }
    public EstadoVenta getEstado() { return estado; }
    public boolean estaVacia() { return detalles.isEmpty(); }
}
