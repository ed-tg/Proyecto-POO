package bodeganila.datos;

import bodeganila.excepciones.BodegaException;
import bodeganila.excepciones.ProductoDuplicadoException;
import bodeganila.modelo.*;
import bodeganila.pago.BilleteraDigital;
import bodeganila.pago.Efectivo;
import bodeganila.pago.MetodoPago;
import bodeganila.servicio.Inventario;
import bodeganila.servicio.VentaService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static bodeganila.modelo.Categoria.*;

/**
 * Datos de ejemplo de un quiosco/bodega: productos con precio, costo y stock reales de referencia.
 *
 * Convención de stock (igual que en Producto): se mide en la unidad de venta.
 *   - ProductoPorUnidad  -> unidades        (gaseosas, galletas, aceite de 1 L...)
 *   - ProductoPorPaquete -> paquetes/cajas  (el último número es cuántas piezas trae)
 *   - ProductoPorPeso    -> kilogramos      (precio por kilo)
 * El volumen o peso del envase (500 ml, 1 L, 400 g...) va en el nombre del producto.
 *
 * Algunos productos tienen stock bajo A PROPÓSITO para probar las alertas.
 */
public final class DatosQuiosco {

    private DatosQuiosco() { }

    public static void cargarCatalogo(Inventario inventario) throws ProductoDuplicadoException {
        for (Producto p : crearCatalogo()) {
            inventario.agregarProducto(p);
        }
    }

    public static List<Producto> crearCatalogo() {
        List<Producto> c = new ArrayList<>();

        // ------------------------------------------------------------- BEBIDAS
        //                    código    nombre                         categoría  precio   costo   stock min
        c.add(porUnidad("BEB001", "Inca Kola 500 ml",               BEBIDAS, "2.50",  "1.80",  36, 12));
        c.add(porUnidad("BEB002", "Coca-Cola 500 ml",               BEBIDAS, "2.50",  "1.80",  36, 12));
        c.add(porUnidad("BEB003", "Inca Kola 1.5 L",                BEBIDAS, "6.50",  "5.00",  18, 6));
        c.add(porUnidad("BEB004", "Coca-Cola 3 L",                  BEBIDAS, "11.00", "9.00",  3,  4));   // stock bajo
        c.add(porUnidad("BEB005", "Pepsi 500 ml",                   BEBIDAS, "2.00",  "1.40",  24, 8));
        c.add(porUnidad("BEB006", "Agua mineral 625 ml",            BEBIDAS, "1.50",  "0.90",  48, 12));
        c.add(porUnidad("BEB007", "Jugo de fruta 300 ml",           BEBIDAS, "1.50",  "1.00",  24, 6));
        c.add(porUnidad("BEB008", "Bebida rehidratante 500 ml",     BEBIDAS, "3.50",  "2.60",  18, 6));
        c.add(porPaquete("BEB009", "Gaseosa 500 ml (paquete x6)",   BEBIDAS, "13.50", "10.00", 8,  2, 6));
        c.add(porPaquete("BEB010", "Agua 625 ml (paquete x12)",     BEBIDAS, "15.00", "10.50", 6,  2, 12));

        // --------------------------------------------- GOLOSINAS, GALLETAS Y SNACKS
        c.add(porUnidad("GOL001", "Galleta soda 34 g",              GOLOSINAS, "0.80", "0.50", 60, 20));
        c.add(porUnidad("GOL002", "Galleta de chocolate 40 g",      GOLOSINAS, "1.00", "0.65", 60, 20));
        c.add(porUnidad("GOL003", "Galleta rellena 36 g",           GOLOSINAS, "1.20", "0.80", 48, 15));
        c.add(porUnidad("GOL004", "Chocolate en barra 30 g",        GOLOSINAS, "1.80", "1.30", 30, 10));
        c.add(porUnidad("GOL005", "Chicle",                         GOLOSINAS, "0.50", "0.30", 100, 30));
        c.add(porUnidad("GOL006", "Caramelo masticable",            GOLOSINAS, "0.50", "0.30", 80, 20));
        c.add(porPeso  ("GOL007", "Caramelos surtidos a granel",    GOLOSINAS, "12.00", "8.50", 0.8, 1.0)); // stock bajo
        c.add(porUnidad("GOL008", "Chupetín",                       GOLOSINAS, "0.30", "0.18", 100, 30));
        c.add(porUnidad("GOL009", "Papas fritas 28 g",              GOLOSINAS, "1.50", "1.05", 40, 12));
        c.add(porUnidad("GOL010", "Piqueo de maíz 30 g",            GOLOSINAS, "1.00", "0.70", 50, 15));
        c.add(porUnidad("GOL011", "Maní salado 30 g",               GOLOSINAS, "1.00", "0.65", 40, 12));
        c.add(porUnidad("GOL012", "Gomitas de fruta 25 g",          GOLOSINAS, "1.00", "0.60", 40, 12));

        // -------------------------------------------------------------- ABARROTES
        c.add(porPeso  ("ABA001", "Arroz extra a granel",           ABARROTES, "4.20", "3.50", 50.0, 10.0));
        c.add(porPeso  ("ABA002", "Azúcar rubia a granel",          ABARROTES, "3.80", "3.10", 40.0, 10.0));
        c.add(porPeso  ("ABA003", "Lentejas a granel",              ABARROTES, "7.50", "6.20", 3.5,  4.0));  // stock bajo
        c.add(porPeso  ("ABA004", "Frijol canario a granel",        ABARROTES, "8.00", "6.50", 15.0, 4.0));
        c.add(porPeso  ("ABA013", "Harina de trigo a granel",       ABARROTES, "3.50", "2.80", 20.0, 5.0));
        c.add(porUnidad("ABA005", "Fideo spaghetti 250 g",          ABARROTES, "1.80", "1.30", 40, 12));
        c.add(porUnidad("ABA006", "Sal de mesa 1 kg",               ABARROTES, "1.50", "1.00", 24, 6));
        c.add(porUnidad("ABA007", "Aceite vegetal 1 L",             ABARROTES, "9.50", "8.00", 18, 6));
        c.add(porUnidad("ABA008", "Atún en conserva 170 g",         ABARROTES, "5.50", "4.40", 24, 8));
        c.add(porUnidad("ABA009", "Leche evaporada 400 g",          ABARROTES, "4.30", "3.60", 36, 12));
        c.add(porUnidad("ABA010", "Avena 120 g",                    ABARROTES, "1.50", "1.00", 20, 6));
        c.add(porUnidad("ABA014", "Huevo",                          ABARROTES, "0.60", "0.45", 90, 30));
        c.add(porPaquete("ABA011", "Café instantáneo sachet (caja x10)", ABARROTES, "7.00", "5.20", 8, 2, 10));
        c.add(porPaquete("ABA012", "Sazonador sachet (x12)",        ABARROTES, "4.80", "3.60", 8,  2, 12));

        // ------------------------------------------------------------------ LIMPIEZA
        c.add(porUnidad("LIM001", "Detergente en polvo 500 g",      LIMPIEZA, "5.00", "3.90", 20, 6));
        c.add(porUnidad("LIM002", "Lejía 1 L",                      LIMPIEZA, "4.50", "3.40", 4,  5));       // stock bajo
        c.add(porUnidad("LIM003", "Desinfectante 900 ml",           LIMPIEZA, "6.50", "5.00", 12, 4));
        c.add(porUnidad("LIM004", "Papel higiénico (rollo)",        LIMPIEZA, "1.00", "0.70", 50, 15));
        c.add(porPaquete("LIM005", "Papel higiénico (paquete x4)",  LIMPIEZA, "4.50", "3.40", 15, 5, 4));
        c.add(porUnidad("LIM006", "Jabón de lavar en barra",        LIMPIEZA, "2.00", "1.40", 24, 8));
        c.add(porPaquete("LIM007", "Detergente sachet (x12)",       LIMPIEZA, "6.00", "4.60", 10, 3, 12));
        c.add(porUnidad("LIM008", "Esponja lavavajilla",            LIMPIEZA, "1.00", "0.60", 30, 10));
        c.add(porUnidad("LIM009", "Lavavajilla en pasta 400 g",     LIMPIEZA, "4.00", "3.00", 12, 4));
        c.add(porPaquete("LIM010", "Bolsas de basura (paquete x10)", LIMPIEZA, "3.00", "2.10", 12, 4, 10));

        // -------------------------------------------------------- CUIDADO PERSONAL
        c.add(porPaquete("CUI001", "Shampoo sachet (paquete x12)",  CUIDADO_PERSONAL, "6.00", "4.20", 10, 3, 12));
        c.add(porUnidad ("CUI002", "Shampoo sachet suelto",         CUIDADO_PERSONAL, "0.60", "0.35", 80, 24));
        c.add(porPaquete("CUI003", "Desodorante sachet (paquete x6)", CUIDADO_PERSONAL, "5.00", "3.60", 8, 2, 6));
        c.add(porUnidad ("CUI004", "Cuchilla de afeitar",          CUIDADO_PERSONAL, "1.50", "1.00", 30, 10));
        c.add(porUnidad ("CUI005", "Pasta dental 75 ml",            CUIDADO_PERSONAL, "4.50", "3.40", 15, 5));
        c.add(porUnidad ("CUI006", "Cepillo dental",                CUIDADO_PERSONAL, "2.50", "1.70", 20, 6));
        c.add(porUnidad ("CUI007", "Jabón de tocador",              CUIDADO_PERSONAL, "2.50", "1.80", 24, 8));
        c.add(porPaquete("CUI008", "Toallas higiénicas (paquete x8)", CUIDADO_PERSONAL, "3.50", "2.60", 12, 4, 8));

        // ------------------------------------------------------------------- VARIOS
        c.add(porUnidad("VAR001", "Cuaderno 100 hojas",             VARIOS, "3.50", "2.50", 20, 6));
        c.add(porUnidad("VAR002", "Lapicero azul",                  VARIOS, "1.00", "0.60", 50, 15));
        c.add(porUnidad("VAR003", "Lápiz 2B",                       VARIOS, "0.80", "0.45", 50, 15));
        c.add(porUnidad("VAR004", "Pegamento 30 ml",                VARIOS, "2.00", "1.30", 20, 6));
        c.add(porUnidad("VAR005", "Borrador",                       VARIOS, "0.50", "0.30", 40, 12));
        c.add(porUnidad("VAR006", "Regla 30 cm",                    VARIOS, "1.50", "0.90", 20, 6));
        c.add(porPaquete("VAR007", "Pilas AA (paquete x2)",         VARIOS, "4.00", "3.00", 15, 4, 2));
        c.add(porUnidad("VAR008", "Fósforos (caja)",                VARIOS, "0.50", "0.30", 40, 12));
        c.add(porPaquete("VAR009", "Velas (paquete x6)",            VARIOS, "3.00", "2.10", 10, 3, 6));
        c.add(porUnidad("VAR010", "Bolsa de regalo",                VARIOS, "2.00", "1.20", 15, 5));
        c.add(porUnidad("VAR011", "Cinta adhesiva",                 VARIOS, "1.50", "0.95", 20, 6));

        return c;
    }

    /**
     * Registra algunas ventas (pasando por VentaService, como en el uso real) para que los reportes
     * tengan datos. La última queda anulada, para mostrar la devolución de stock.
     */
    public static void cargarVentasDeEjemplo(VentaService ventas) throws BodegaException {
        vender(ventas, new Efectivo(new BigDecimal("20")),
                "BEB001", 2, "GOL001", 3, "ABA001", 1.5);
        vender(ventas, new BilleteraDigital(BilleteraDigital.Proveedor.YAPE, "00123456"),
                "BEB003", 1, "GOL009", 2, "ABA005", 2);
        vender(ventas, new Efectivo(new BigDecimal("50")),
                "ABA002", 2.5, "ABA007", 1, "ABA009", 3, "ABA014", 6);
        vender(ventas, new BilleteraDigital(BilleteraDigital.Proveedor.PLIN, null),
                "LIM001", 1, "LIM004", 4, "CUI005", 1);
        vender(ventas, new Efectivo(new BigDecimal("20")),
                "BEB009", 1, "GOL007", 0.25);
        Venta anulada = vender(ventas, new Efectivo(new BigDecimal("10")),
                "VAR001", 1, "VAR002", 2, "VAR003", 3);
        ventas.anularVenta(anulada.getNumero());
    }

    // ------------------------------------------------------------------ ayudas

    /** lineas = código, cantidad, código, cantidad... */
    private static Venta vender(VentaService ventas, MetodoPago pago, Object... lineas) throws BodegaException {
        ventas.iniciarVenta();
        for (int i = 0; i < lineas.length; i += 2) {
            ventas.agregarProducto((String) lineas[i], ((Number) lineas[i + 1]).doubleValue());
        }
        return ventas.finalizarVenta(pago);
    }

    private static Producto porUnidad(String codigo, String nombre, Categoria categoria,
                                      String precio, String costo, int stock, int minimo) {
        Producto p = new ProductoPorUnidad(codigo, nombre, categoria, new BigDecimal(precio), stock, minimo);
        p.setPrecioCosto(new BigDecimal(costo));
        return p;
    }

    private static Producto porPaquete(String codigo, String nombre, Categoria categoria,
                                       String precio, String costo, int stock, int minimo, int unidadesPorPaquete) {
        Producto p = new ProductoPorPaquete(codigo, nombre, categoria, new BigDecimal(precio),
                stock, minimo, unidadesPorPaquete);
        p.setPrecioCosto(new BigDecimal(costo));
        return p;
    }

    /** precio y costo son POR KILO; stock y mínimo en kilogramos. */
    private static Producto porPeso(String codigo, String nombre, Categoria categoria,
                                    String precioKilo, String costoKilo, double stockKg, double minimoKg) {
        Producto p = new ProductoPorPeso(codigo, nombre, categoria, new BigDecimal(precioKilo), stockKg, minimoKg);
        p.setPrecioCosto(new BigDecimal(costoKilo));
        return p;
    }
}
