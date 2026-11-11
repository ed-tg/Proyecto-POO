package bodeganila.servicio;

import bodeganila.modelo.*;
import bodeganila.util.Colecciones;
import bodeganila.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Genera los reportes a partir de las ventas completadas y del inventario. */
public class ReporteService {

    private final VentaService ventaService;
    private final Inventario inventario;

    public ReporteService(VentaService ventaService, Inventario inventario) {
        this.ventaService = ventaService;
        this.inventario = inventario;
    }

    /** Ventas completadas (no anuladas) de una fecha. */
    public List<Venta> ventasDelDia(LocalDate fecha) {
        return Colecciones.filtrar(ventaService.getVentas(),
                v -> v.getEstado() == EstadoVenta.COMPLETADA && v.getFecha().toLocalDate().equals(fecha));
    }

    /** Productos distintos vendidos en el día. TreeSet: sin repetidos y ordenado (orden natural de Producto). */
    public SortedSet<Producto> productosVendidos(LocalDate fecha) {
        SortedSet<Producto> productos = new TreeSet<>();
        for (Venta v : ventasDelDia(fecha)) {
            for (DetalleVenta d : v) {
                productos.add(d.getProducto());
            }
        }
        return productos;
    }

    /** Categorías que tuvieron ventas en el día (EnumSet: sin repetidos, en el orden del enum). */
    public Set<Categoria> categoriasVendidas(LocalDate fecha) {
        Set<Categoria> categorias = EnumSet.noneOf(Categoria.class);
        for (Producto p : productosVendidos(fecha)) {
            categorias.add(p.getCategoria());
        }
        return categorias;
    }

    public BigDecimal totalVendido(List<Venta> ventas) {
        BigDecimal total = BigDecimal.ZERO;
        for (Venta v : ventas) {
            total = total.add(v.getTotal());
        }
        return total;
    }

    public Map<String, BigDecimal> totalPorMetodoPago(List<Venta> ventas) {
        Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
        for (Venta v : ventas) {
            porMetodo.merge(v.getMetodoPago().getNombre(), v.getTotal(), BigDecimal::add);
        }
        return porMetodo;
    }

    /** Productos más vendidos (cantidad acumulada), de mayor a menor. */
    public List<Map.Entry<String, Double>> productosMasVendidos(int top) {
        Map<String, Double> acumulado = new HashMap<>();
        for (Venta v : ventaService.getVentas()) {
            if (v.getEstado() == EstadoVenta.COMPLETADA) {
                for (DetalleVenta d : v.getDetalles()) {
                    acumulado.merge(d.getProducto().getNombre(), d.getCantidad(), Double::sum);
                }
            }
        }
        List<Map.Entry<String, Double>> lista = new ArrayList<>(acumulado.entrySet());
        lista.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return lista.subList(0, Math.min(top, lista.size()));
    }

    /** Ganancia estimada del día; ignora productos sin precio de costo. */
    public BigDecimal gananciaEstimada(LocalDate fecha) {
        BigDecimal ganancia = BigDecimal.ZERO;
        for (Venta v : ventasDelDia(fecha)) {
            for (DetalleVenta d : v.getDetalles()) {
                BigDecimal costo = d.getProducto().getPrecioCosto();
                if (costo != null) {
                    BigDecimal margen = d.getPrecioAplicado().subtract(costo);
                    ganancia = ganancia.add(margen.multiply(BigDecimal.valueOf(d.getCantidad())));
                }
            }
        }
        return Dinero.redondear(ganancia);
    }

    public String generarReporteVentasDia(LocalDate fecha) {
        List<Venta> ventas = ventasDelDia(fecha);
        StringBuilder sb = new StringBuilder();
        sb.append("---- VENTAS DEL ").append(fecha).append(" ----\n");
        sb.append("Cantidad de ventas: ").append(ventas.size()).append("\n");
        sb.append("Total vendido: ").append(Dinero.formatear(totalVendido(ventas))).append("\n");
        sb.append("Productos distintos vendidos: ").append(productosVendidos(fecha).size()).append("\n");
        sb.append("Categorías con ventas: ").append(categoriasVendidas(fecha)).append("\n");
        for (Map.Entry<String, BigDecimal> e : totalPorMetodoPago(ventas).entrySet()) {
            sb.append("  - ").append(e.getKey()).append(": ").append(Dinero.formatear(e.getValue())).append("\n");
        }
        return sb.toString();
    }

    public String generarReporteStockBajo() {
        List<Producto> bajos = inventario.listarStockBajo();
        StringBuilder sb = new StringBuilder("---- PRODUCTOS CON STOCK BAJO ----\n");
        if (bajos.isEmpty()) {
            sb.append("Ningún producto está por agotarse.\n");
        }
        for (Producto p : bajos) {
            sb.append(String.format("%s | stock: %s %s (mínimo %s)%n", p.getNombre(),
                    p.formatearCantidad(p.getStock()), p.getUnidadMedida().getAbreviatura(),
                    p.formatearCantidad(p.getStockMinimo())));
        }
        return sb.toString();
    }
}
