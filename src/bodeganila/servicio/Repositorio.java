package bodeganila.servicio;

import bodeganila.excepciones.ProductoDuplicadoException;
import bodeganila.modelo.Producto;
import bodeganila.util.Colecciones;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.Predicate;

/**
 * Repositorio genérico en memoria. T está restringido a Producto (T extends Producto),
 * así que puede guardar Producto o cualquiera de sus subclases (Repositorio<ProductoPorPeso>).
 * Es Iterable: se recorre con for-each y su iterador permite remove().
 */
public class Repositorio<T extends Producto> implements Iterable<T> {

    /** SequencedMap (Java 21): mantiene el orden de inserción y permite firstEntry()/lastEntry(). */
    private final SequencedMap<String, T> elementos = new LinkedHashMap<>();

    public void agregar(T elemento) throws ProductoDuplicadoException {
        if (elementos.containsKey(elemento.getCodigo())) {
            throw new ProductoDuplicadoException(elemento.getCodigo());
        }
        elementos.put(elemento.getCodigo(), elemento);
    }

    public Optional<T> buscar(String codigo) {
        return Optional.ofNullable(elementos.get(normalizar(codigo)));
    }

    public boolean contiene(String codigo) {
        return elementos.containsKey(normalizar(codigo));
    }

    public List<T> filtrar(Predicate<? super T> condicion) {
        return Colecciones.filtrar(elementos.values(), condicion);
    }

    public List<T> ordenado(Comparator<? super T> criterio) {
        List<T> copia = new ArrayList<>(elementos.values());
        copia.sort(criterio);
        return copia;
    }

    public Optional<T> primero() {
        return elementos.isEmpty() ? Optional.empty() : Optional.of(elementos.firstEntry().getValue());
    }

    public Optional<T> ultimo() {
        return elementos.isEmpty() ? Optional.empty() : Optional.of(elementos.lastEntry().getValue());
    }

    public int tamano() {
        return elementos.size();
    }

    @Override
    public Iterator<T> iterator() {
        return elementos.values().iterator();   // su remove() elimina también del mapa
    }

    private static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}
