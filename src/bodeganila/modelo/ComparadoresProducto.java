package bodeganila.modelo;

import java.util.Comparator;

/** Criterios de orden para listar productos (se pasan a List.sort o a Inventario.listarOrdenado). */
public final class ComparadoresProducto {

    private ComparadoresProducto() { }

    public static final Comparator<Producto> POR_CODIGO =
            Comparator.comparing(Producto::getCodigo);

    public static final Comparator<Producto> POR_NOMBRE =
            Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER);

    public static final Comparator<Producto> POR_PRECIO =
            Comparator.comparing(Producto::getPrecio);

    public static final Comparator<Producto> POR_STOCK =
            Comparator.comparingDouble(Producto::getStock);

    public static final Comparator<Producto> POR_CATEGORIA_Y_NOMBRE =
            Comparator.comparing(Producto::getCategoria).thenComparing(POR_NOMBRE);
}
