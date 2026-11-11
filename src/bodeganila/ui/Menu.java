package bodeganila.ui;

import bodeganila.excepciones.BodegaException;
import bodeganila.modelo.*;
import bodeganila.pago.BilleteraDigital;
import bodeganila.pago.Efectivo;
import bodeganila.pago.MetodoPago;
import bodeganila.servicio.Inventario;
import bodeganila.servicio.ReporteService;
import bodeganila.servicio.VentaService;
import bodeganila.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/** Interfaz de consola. No tiene reglas de negocio: solo pide datos, llama a los servicios y muestra resultados. */
public class Menu {

    private interface Accion {
        void ejecutar() throws Exception;
    }

    private final Scanner in = new Scanner(System.in);
    private final Inventario inventario;
    private final VentaService ventaService;
    private final ReporteService reporteService;

    public Menu(Inventario inventario, VentaService ventaService, ReporteService reporteService) {
        this.inventario = inventario;
        this.ventaService = ventaService;
        this.reporteService = reporteService;
    }

    // ================================================================ menús

    public void iniciar() {
        int op;
        do {
            System.out.println("\n===== BODEGA NILA =====");
            System.out.println("1. Productos\n2. Ventas\n3. Inventario\n4. Reportes\n0. Salir");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: menuProductos(); break;
                case 2: menuVentas(); break;
                case 3: menuInventario(); break;
                case 4: menuReportes(); break;
                case 0: System.out.println("Hasta luego."); break;
                default: System.out.println("Opción no válida.");
            }
        } while (op != 0);
    }

    private void menuProductos() {
        int op;
        do {
            System.out.println("\n--- PRODUCTOS ---");
            System.out.println("1. Listar\n2. Buscar por nombre\n3. Registrar\n4. Cambiar precio\n5. Dar de baja\n"
                    + "6. Listar ordenado\n7. Listar por categoría\n8. Buscar por código (búsqueda binaria)\n"
                    + "9. Último producto registrado\n0. Volver");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: ejecutarSeguro(() -> mostrarProductos(inventario.listarActivos())); break;
                case 2: ejecutarSeguro(() -> mostrarProductos(inventario.buscarPorNombre(leerTexto("Nombre: ")))); break;
                case 3: ejecutarSeguro(this::registrarProducto); break;
                case 4: ejecutarSeguro(() -> {
                            Producto p = inventario.buscarPorCodigo(leerTexto("Código: "));
                            p.setPrecio(leerBigDecimal("Nuevo precio: "));
                            System.out.println("Precio actualizado: " + p);
                        }); break;
                case 5: ejecutarSeguro(() -> {
                            inventario.darDeBaja(leerTexto("Código: "));
                            System.out.println("Producto dado de baja.");
                        }); break;
                case 6: ejecutarSeguro(this::listarOrdenado); break;
                case 7: ejecutarSeguro(this::listarPorCategoria); break;
                case 8: ejecutarSeguro(() -> {
                            Optional<Producto> p = inventario.buscarBinarioPorCodigo(leerTexto("Código: "));
                            System.out.println(p.map(Object::toString).orElse("No existe ese código."));
                        }); break;
                case 9: System.out.println(inventario.ultimoProductoRegistrado()
                            .map(Object::toString).orElse("Aún no hay productos.")); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } while (op != 0);
    }

    private void menuVentas() {
        int op;
        do {
            System.out.println("\n--- VENTAS ---");
            System.out.println("1. Nueva venta\n2. Anular venta\n3. Última venta (ticket)\n4. Ventas recientes\n0. Volver");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: nuevaVenta(); break;
                case 2: ejecutarSeguro(() -> {
                            int n = leerEntero("N° de venta a anular: ");
                            ventaService.anularVenta(n);
                            System.out.println("Venta anulada y stock devuelto.");
                        }); break;
                case 3: System.out.println(ventaService.getUltimaVenta()
                            .map(Venta::generarTicket).orElse("Aún no hay ventas.")); break;
                case 4: mostrarVentasRecientes(5); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } while (op != 0);
    }

    private void menuInventario() {
        int op;
        do {
            System.out.println("\n--- INVENTARIO ---");
            System.out.println("1. Reponer stock\n2. Registrar merma\n3. Historial de un producto\n"
                    + "4. Valor del inventario\n5. Últimos movimientos\n6. Producto más caro\n"
                    + "7. Eliminar bajas sin ventas\n0. Volver");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: ejecutarSeguro(() -> {
                            inventario.reponer(leerTexto("Código: "), leerDouble("Cantidad: "), "Reposición");
                            System.out.println("Stock repuesto.");
                        }); break;
                case 2: ejecutarSeguro(() -> {
                            String cod = leerTexto("Código: ");
                            double cant = leerDouble("Cantidad perdida: ");
                            inventario.registrarAjuste(cod, cant, leerTexto("Motivo (vencido, roto...): "));
                            System.out.println("Merma registrada.");
                        }); break;
                case 3: ejecutarSeguro(() -> {
                            List<MovimientoInventario> hist = inventario.getHistorialDe(leerTexto("Código: "));
                            if (hist.isEmpty()) System.out.println("Sin movimientos.");
                            for (MovimientoInventario m : hist) System.out.println(m);
                        }); break;
                case 4:
                    System.out.println("Valor a precio de costo: " + Dinero.formatear(inventario.calcularValorInventario()));
                    System.out.println("Valor a precio de venta: " + Dinero.formatear(inventario.calcularValorDeVenta()));
                    break;
                case 5:
                    List<MovimientoInventario> recientes = inventario.getMovimientosRecientes(5);
                    if (recientes.isEmpty()) System.out.println("Sin movimientos.");
                    for (MovimientoInventario m : recientes) System.out.println(m);
                    break;
                case 6: System.out.println(inventario.productoMasCaro()
                            .map(Object::toString).orElse("Aún no hay productos.")); break;
                case 7: System.out.println("Productos eliminados: " + inventario.eliminarBajasSinVentas()); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } while (op != 0);
    }

    private void menuReportes() {
        int op;
        do {
            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Ventas del día\n2. Productos con stock bajo\n3. Más vendidos\n"
                    + "4. Ganancia estimada del día\n5. Productos vendidos hoy\n0. Volver");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: System.out.println(reporteService.generarReporteVentasDia(LocalDate.now())); break;
                case 2: System.out.println(reporteService.generarReporteStockBajo()); break;
                case 3:
                    List<Map.Entry<String, Double>> top = reporteService.productosMasVendidos(5);
                    if (top.isEmpty()) System.out.println("Aún no hay ventas.");
                    for (Map.Entry<String, Double> e : top) System.out.println(e.getKey() + ": " + e.getValue());
                    break;
                case 4: System.out.println("Ganancia estimada hoy: "
                            + Dinero.formatear(reporteService.gananciaEstimada(LocalDate.now()))); break;
                case 5:
                    var vendidos = reporteService.productosVendidos(LocalDate.now());   // SortedSet, sin repetidos
                    if (vendidos.isEmpty()) System.out.println("Aún no hay ventas hoy.");
                    for (Producto p : vendidos) System.out.println(p.getCodigo() + " - " + p.getNombre());
                    break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } while (op != 0);
    }

    // ================================================================ flujo de venta

    private void nuevaVenta() {
        ventaService.iniciarVenta();
        int op;
        do {
            mostrarVentaActual();
            System.out.println("1. Agregar producto\n2. Quitar producto\n3. Cobrar\n0. Cancelar venta");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: ejecutarSeguro(() -> {
                            String cod = leerTexto("Código: ");
                            double cant = leerDouble("Cantidad: ");
                            ventaService.agregarProducto(cod, cant);
                        }); break;
                case 2: ejecutarSeguro(() -> ventaService.quitarProducto(leerTexto("Código a quitar: "))); break;
                case 3: if (cobrar()) return; break;
                case 0: ventaService.cancelarVenta(); System.out.println("Venta cancelada."); break;
                default: System.out.println("Opción no válida.");
            }
        } while (op != 0);
    }

    /** @return true si la venta quedó cobrada */
    private boolean cobrar() {
        final boolean[] cobrada = {false};
        ejecutarSeguro(() -> {
            BigDecimal total = ventaService.getVentaActual().getTotal();
            System.out.println("Total a cobrar: " + Dinero.formatear(total));
            System.out.println("1. Efectivo\n2. Yape\n3. Plin");
            int m = leerEntero("Método: ");
            MetodoPago metodo;
            if (m == 1) {
                metodo = new Efectivo(leerBigDecimal("Monto recibido: "));
            } else if (m == 2 || m == 3) {
                String operacion = leerTexto("N° de operación (opcional, Enter para omitir): ", true);
                metodo = new BilleteraDigital(m == 2 ? BilleteraDigital.Proveedor.YAPE
                                                     : BilleteraDigital.Proveedor.PLIN, operacion);
            } else {
                throw new IllegalArgumentException("Método de pago no válido");
            }
            Venta venta = ventaService.finalizarVenta(metodo);
            System.out.println("\n" + venta.generarTicket());
            cobrada[0] = true;
        });
        return cobrada[0];
    }

    private void mostrarVentaActual() {
        Venta v = ventaService.getVentaActual();
        System.out.println("\n--- Venta N° " + v.getNumero() + " ---");
        if (v.estaVacia()) System.out.println("(sin productos)");
        for (DetalleVenta d : v) {                       // Venta es Iterable<DetalleVenta>
            System.out.println(d.getProducto().getCodigo() + " " + d.getProducto().getNombre() + " | "
                    + d.getCantidadTexto() + " | " + Dinero.formatear(d.getSubtotal()));
        }
        System.out.println("TOTAL: " + Dinero.formatear(v.getTotal()));
    }

    private void mostrarVentasRecientes(int maximo) {
        List<Venta> recientes = ventaService.getVentasRecientes();
        if (recientes.isEmpty()) System.out.println("Aún no hay ventas.");
        int mostradas = 0;
        for (Venta v : recientes) {
            if (mostradas++ >= maximo) break;
            System.out.println("Venta N° " + v.getNumero() + " | " + v.getEstado() + " | "
                    + v.getMetodoPago().getNombre() + " | " + Dinero.formatear(v.getTotal()));
        }
    }

    // ================================================================ productos

    private void registrarProducto() throws Exception {
        System.out.println("Tipo: 1. Por unidad  2. Por paquete  3. Por peso (precio por kilo)");
        int tipo = leerEntero("Tipo: ");
        String codigo = leerTexto("Código: ");
        String nombre = leerTexto("Nombre (incluya el tamaño: 500 ml, 1 L, 400 g...): ");
        Categoria categoria = elegirCategoria();
        BigDecimal precio = leerBigDecimal("Precio de venta: ");
        BigDecimal costo = leerBigDecimal("Precio de costo (0 si no se sabe): ");
        double stock = leerDouble("Stock inicial: ");
        double minimo = leerDouble("Stock mínimo: ");

        Producto p;
        switch (tipo) {
            case 1: p = new ProductoPorUnidad(codigo, nombre, categoria, precio, stock, minimo); break;
            case 2: p = new ProductoPorPaquete(codigo, nombre, categoria, precio, stock, minimo,
                                               leerEntero("Unidades por paquete: ")); break;
            case 3: p = new ProductoPorPeso(codigo, nombre, categoria, precio, stock, minimo); break;
            default: throw new IllegalArgumentException("Tipo de producto no válido");
        }
        if (costo.signum() > 0) p.setPrecioCosto(costo);
        inventario.agregarProducto(p);
        System.out.println("Producto registrado: " + p);
    }

    private Categoria elegirCategoria() {
        Categoria[] todas = Categoria.values();
        for (int i = 0; i < todas.length; i++) System.out.println((i + 1) + ". " + todas[i]);
        int n = leerEntero("Categoría: ");
        if (n < 1 || n > todas.length) throw new IllegalArgumentException("Categoría no válida");
        return todas[n - 1];
    }

    private void listarOrdenado() {
        System.out.println("Ordenar por: 1. Nombre  2. Precio  3. Stock  4. Categoría y nombre");
        Comparator<Producto> criterio;
        switch (leerEntero("Criterio: ")) {
            case 1: criterio = ComparadoresProducto.POR_NOMBRE; break;
            case 2: criterio = ComparadoresProducto.POR_PRECIO; break;
            case 3: criterio = ComparadoresProducto.POR_STOCK; break;
            case 4: criterio = ComparadoresProducto.POR_CATEGORIA_Y_NOMBRE; break;
            default: throw new IllegalArgumentException("Criterio no válido");
        }
        mostrarProductos(inventario.listarOrdenado(criterio));
    }

    private void listarPorCategoria() {
        for (Map.Entry<Categoria, List<Producto>> grupo : inventario.agruparPorCategoria().entrySet()) {
            System.out.println("\n== " + grupo.getKey() + " (" + grupo.getValue().size() + ") ==");
            for (Producto p : grupo.getValue()) System.out.println("  " + p);
        }
    }

    private void mostrarProductos(List<Producto> lista) {
        if (lista.isEmpty()) System.out.println("No se encontraron productos.");
        for (Producto p : lista) {
            System.out.println(p + " | " + formaDeVenta(p) + (p.estaBajoStockMinimo() ? "  <-- STOCK BAJO" : ""));
        }
    }

    /** switch exhaustivo sobre la clase sellada: sin default, y si se agrega una subclase el compilador avisa. */
    private static String formaDeVenta(Producto p) {
        return switch (p) {
            case ProductoPorUnidad u -> "por unidad";
            case ProductoPorPaquete pq -> "paquete de " + pq.getUnidadesPorPaquete() + " piezas";
            case ProductoPorPeso pw -> "a granel (precio por kilo)";
        };
    }

    // ================================================================ manejo uniforme de errores

    /** Todos los errores esperables se muestran igual y el programa no se cae. */
    private void ejecutarSeguro(Accion accion) {
        try {
            accion.ejecutar();
        } catch (BodegaException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[!] Error inesperado: " + e);
        }
    }

    // ================================================================ lectura validada

    private String leerTexto(String mensaje) {
        return leerTexto(mensaje, false);
    }

    private String leerTexto(String mensaje, boolean permitirVacio) {
        while (true) {
            System.out.print(mensaje);
            if (!in.hasNextLine()) {          // fin de la entrada (Ctrl+D / archivo)
                System.out.println("\nEntrada finalizada.");
                System.exit(0);
            }
            String texto = in.nextLine().trim();
            if (!texto.isEmpty() || permitirVacio) return texto;
            System.out.println("[!] No puede quedar vacío.");
        }
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("[!] Ingrese un número entero.");
            }
        }
    }

    private double leerDouble(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("[!] Ingrese un número (ej. 2 o 1.5).");
            }
        }
    }

    private BigDecimal leerBigDecimal(String mensaje) {
        while (true) {
            try {
                return new BigDecimal(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("[!] Ingrese un monto válido (ej. 2.50).");
            }
        }
    }
}
