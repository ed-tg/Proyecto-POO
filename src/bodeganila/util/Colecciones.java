package bodeganila.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Métodos genéricos reutilizables sobre colecciones.
 * Usan comodines (? extends / ? super) y restricciones de tipo (K extends Comparable).
 */
public final class Colecciones {

    private Colecciones() { }

    /** Elementos que cumplen la condición. */
    public static <T> List<T> filtrar(Iterable<? extends T> origen, Predicate<? super T> condicion) {
        List<T> resultado = new ArrayList<>();
        for (T elemento : origen) {
            if (condicion.test(elemento)) {
                resultado.add(elemento);
            }
        }
        return resultado;
    }

    /** Elemento mayor según el comparador (Optional vacío si no hay elementos). */
    public static <T> Optional<T> maximoPor(Iterable<? extends T> origen, Comparator<? super T> comparador) {
        T mejor = null;
        boolean hay = false;
        for (T elemento : origen) {
            if (!hay || comparador.compare(elemento, mejor) > 0) {
                mejor = elemento;
                hay = true;
            }
        }
        return hay ? Optional.of(mejor) : Optional.empty();
    }

    /** Agrupa por una clave; la clave debe ser Comparable para que los grupos salgan ordenados. */
    public static <T, K extends Comparable<? super K>> Map<K, List<T>> agrupar(
            Iterable<? extends T> origen, Function<? super T, ? extends K> clasificador) {
        Map<K, List<T>> grupos = new TreeMap<>();
        for (T elemento : origen) {
            grupos.computeIfAbsent(clasificador.apply(elemento), k -> new ArrayList<>()).add(elemento);
        }
        return grupos;
    }

    /**
     * Búsqueda binaria: la lista DEBE estar ordenada por la misma clave que se busca.
     * @return la posición del elemento, o un valor negativo si no existe
     */
    public static <T, K extends Comparable<? super K>> int buscarBinario(
            List<? extends T> ordenada, K clave, Function<? super T, ? extends K> extractor) {
        int bajo = 0;
        int alto = ordenada.size() - 1;
        while (bajo <= alto) {
            int medio = (bajo + alto) >>> 1;
            int cmp = extractor.apply(ordenada.get(medio)).compareTo(clave);
            if (cmp < 0) {
                bajo = medio + 1;
            } else if (cmp > 0) {
                alto = medio - 1;
            } else {
                return medio;
            }
        }
        return -(bajo + 1);
    }
}
