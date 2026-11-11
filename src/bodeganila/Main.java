package bodeganila;

import bodeganila.datos.DatosQuiosco;
import bodeganila.excepciones.BodegaException;
import bodeganila.servicio.Inventario;
import bodeganila.servicio.ReporteService;
import bodeganila.servicio.VentaService;
import bodeganila.ui.Menu;

/**
 * Punto de entrada: arma los objetos y los conecta entre sí.
 * Con el argumento "demo" también carga ventas de ejemplo para ver los reportes con datos.
 */
public class Main {

    public static void main(String[] args) throws BodegaException {
        Inventario inventario = new Inventario();
        DatosQuiosco.cargarCatalogo(inventario);   // más adelante vendrá de la base de datos

        VentaService ventaService = new VentaService(inventario);
        ReporteService reporteService = new ReporteService(ventaService, inventario);

        if (args.length > 0 && args[0].equalsIgnoreCase("demo")) {
            DatosQuiosco.cargarVentasDeEjemplo(ventaService);
        }

        new Menu(inventario, ventaService, reporteService).iniciar();
    }
}
