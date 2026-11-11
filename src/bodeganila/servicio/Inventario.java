package bodeganila.servicio;

import bodeganila.excepciones.ProductoDuplicadoException;
import bodeganila.excepciones.ProductoNoEncontradoException;
import bodeganila.excepciones.StockInsuficienteException;
import bodeganila.modelo.*;
import bodeganila.util.Colecciones;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Colección de productos: busca, ordena, repone, descuenta y detecta stock bajo.
 * Es Iterable<Producto>, así que se recorre con for-each: for (Producto p : inventario).
 * Cada cambio de stock genera un MovimientoInventario.
 * Por ahora guarda todo en memoria; más adelante se conectará a la base de datos.
 */
public class Inventario implements Iterable<Producto> {

    private final Repositorio<Producto> repositorio = new Repositorio<>();
    private final List<MovimientoInventario> historial = new ArrayList<>();

    @Override
    public Iterator<Producto> iterator() {
        return repositorio.iterator();
    }

    // ------------------------------------------------------------ altas y bajas

    public void agregarProducto(Producto p) throws ProductoDuplicadoException {
        repositorio.agregar(p);
        if (p.getStock() > 0) {
            registrar(p, TipoMovimiento.ENTRADA, p.getStock(), "Stock inicial");
        }
    }

    /** Baja lógica: el producto deja de aparecer, pero se conserva para el historial. */
    public void darDeBaja(String codigo) throws ProductoNoEncontradoException {
        buscarPorCodigo(codigo).setActivo(false);
    }

    /**
     * Elimina definitivamente los productos dados de baja que nunca se vendieron.
     * Usa Iterator.remove(), la forma segura de borrar mientras se recorre una colección.
     * @return cuántos productos se eliminaron
     */
    public int eliminarBajasSinVentas() {
        int eliminados = 0;
        Iterator<Producto> it = repositorio.iterator();
        while (it.hasNext()) {
            Producto p = it.next();
            if (!p.isActivo() && !fueVendido(p)) {
                it.remove();
                eliminados++;
            }
        }
        return eliminados;
    }

    private boolean fueVendido(Producto p) {
        for (MovimientoInventario m : historial) {
            if (m.getTipo() == TipoMovimiento.SALIDA_VENTA && m.getProducto().equals(p)) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------- búsquedas

    /** Solo productos activos. */
    public Producto buscarPorCodigo(String codigo) throws ProductoNoEncontradoException {
        Producto p = obtener(codigo);
        if (!p.isActivo()) {
            throw new ProductoNoEncontradoException(codigo);
        }
        return p;
    }

    public List<Producto> buscarPorNombre(String texto) {
        String t = texto.trim().toLowerCase();
        return Colecciones.filtrar(this, p -> p.isActivo() && p.getNombre().toLowerCase().contains(t));
    }

    public List<Producto> buscarPorCategoria(Categoria categoria) {
        return Colecciones.filtrar(this, p -> p.isActivo() && p.getCategoria() == categoria);
    }

    /** Búsqueda binaria por código: ordena por código y luego divide el rango a la mitad. */
    public Optional<Producto> buscarBinarioPorCodigo(String codigo) {
        List<Producto> ordenada = listarOrdenado(ComparadoresProducto.POR_CODIGO);
        int posicion = Colecciones.buscarBinario(ordenada, codigo.trim().toUpperCase(), Producto::getCodigo);
        return posicion >= 0 ? Optional.of(ordenada.get(posicion)) : Optional.empty();
    }

    // ------------------------------------------------------------------ listados

    public List<Producto> listarActivos() {
        return Colecciones.filtrar(this, Producto::isActivo);
    }

    public List<Producto> listarStockBajo() {
        return Colecciones.filtrar(this, p -> p.isActivo() && p.estaBajoStockMinimo());
    }

    public List<Producto> listarOrdenado(Comparator<? super Producto> criterio) {
        List<Producto> lista = new ArrayList<>(listarActivos());
        lista.sort(criterio);
        return lista;
    }

    /** Productos activos agrupados por categoría, en el orden del enum. */
    public Map<Categoria, List<Producto>> agruparPorCategoria() {
        return Colecciones.agrupar(listarActivos(), Producto::getCategoria);
    }

    /** Conjunto de categorías que tienen al menos un producto con stock (EnumSet: rápido y ordenado). */
    public Set<Categoria> categoriasConStock() {
        Set<Categoria> categorias = EnumSet.noneOf(Categoria.class);
        for (Producto p : this) {
            if (p.isActivo() && p.getStock() > 0) {
                categorias.add(p.getCategoria());
            }
        }
        return categorias;
    }

    public Optional<Producto> productoMasCaro() {
        return Colecciones.maximoPor(listarActivos(), ComparadoresProducto.POR_PRECIO);
    }

    /** Último producto registrado (SequencedMap.lastEntry en el repositorio). */
    public Optional<Producto> ultimoProductoRegistrado() {
        return repositorio.ultimo();
    }

    // ------------------------------------------------------------ movimientos de stock

    public void reponer(String codigo, double cantidad, String motivo) throws ProductoNoEncontradoException {
        Producto p = buscarPorCodigo(codigo);
        p.aumentarStock(cantidad);
        registrar(p, TipoMovimiento.ENTRADA, cantidad, motivo);
    }

    /** Merma, vencimiento, rotura: descuenta stock sin que haya venta. */
    public void registrarAjuste(String codigo, double cantidad, String motivo)
            throws ProductoNoEncontradoException, StockInsuficienteException {
        Producto p = buscarPorCodigo(codigo);
        p.descontarStock(cantidad);
        registrar(p, TipoMovimiento.AJUSTE, cantidad, motivo);
    }

    /** Comprueba el stock SIN modificarlo (se usa antes de cobrar). */
    public void verificarStock(String codigo, double cantidad)
            throws ProductoNoEncontradoException, StockInsuficienteException {
        Producto p = buscarPorCodigo(codigo);
        p.validarCantidad(cantidad);
        if (!p.hayStock(cantidad)) {
            throw new StockInsuficienteException(p.getNombre(), p.getStock(), cantidad);
        }
    }

    public void descontarPorVenta(String codigo, double cantidad, int numeroVenta)
            throws ProductoNoEncontradoException, StockInsuficienteException {
        Producto p = obtener(codigo);
        p.descontarStock(cantidad);
        registrar(p, TipoMovimiento.SALIDA_VENTA, cantidad, "Venta #" + numeroVenta);
    }

    public void devolverPorAnulacion(String codigo, double cantidad, int numeroVenta)
            throws ProductoNoEncontradoException {
        Producto p = obtener(codigo);
        p.aumentarStock(cantidad);
        registrar(p, TipoMovimiento.DEVOLUCION, cantidad, "Anulación de venta #" + numeroVenta);
    }

    // ------------------------------------------------------------------ valores

    /** Valor del stock actual a precio de costo (ignora productos sin costo registrado). */
    public BigDecimal calcularValorInventario() {
        BigDecimal total = BigDecimal.ZERO;
        for (Producto p : this) {
            if (p.isActivo() && p.getPrecioCosto() != null) {
                total = total.add(p.getPrecioCosto().multiply(BigDecimal.valueOf(p.getStock())));
            }
        }
        return total;
    }

    /** Valor del stock actual si todo se vendiera al precio de venta. */
    public BigDecimal calcularValorDeVenta() {
        return sumarPrecioVenta(listarActivos());
    }

    /** Comodín "? extends Producto": acepta una lista de Producto o de cualquier subclase, solo para lectura. */
    private static BigDecimal sumarPrecioVenta(Collection<? extends Producto> productos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Producto p : productos) {
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(p.getStock())));
        }
        return total;
    }

    // ------------------------------------------------------------------ historial

    public List<MovimientoInventario> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public List<MovimientoInventario> getHistorialDe(String codigo) {
        return Colecciones.filtrar(historial, m -> m.getProducto().getCodigo().equalsIgnoreCase(codigo.trim()));
    }

    public List<MovimientoInventario> getHistorialDelDia(LocalDate fecha) {
        return Colecciones.filtrar(historial, m -> m.getFecha().toLocalDate().equals(fecha));
    }

    /** SequencedCollection (Java 21): getLast() devuelve el último elemento sin buscarlo a mano. */
    public Optional<MovimientoInventario> getUltimoMovimiento() {
        return historial.isEmpty() ? Optional.empty() : Optional.of(historial.getLast());
    }

    /** Los n movimientos más recientes, del último al primero (reversed() recorre al revés). */
    public List<MovimientoInventario> getMovimientosRecientes(int n) {
        List<MovimientoInventario> recientes = new ArrayList<>();
        for (MovimientoInventario m : historial.reversed()) {
            if (recientes.size() >= n) {
                break;
            }
            recientes.add(m);
        }
        return recientes;
    }

    // -------------------------------------------------------------------- privados

    /** Busca incluso productos dados de baja (necesario para anular ventas antiguas). */
    private Producto obtener(String codigo) throws ProductoNoEncontradoException {
        return repositorio.buscar(codigo).orElseThrow(() -> new ProductoNoEncontradoException(codigo));
    }

    private void registrar(Producto p, TipoMovimiento tipo, double cantidad, String motivo) {
        historial.add(new MovimientoInventario(p, tipo, cantidad, motivo, p.getStock()));
    }
}
