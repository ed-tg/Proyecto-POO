package bodeganila.excepciones;

public class ProductoDuplicadoException extends BodegaException {

    public ProductoDuplicadoException(String codigo) {
        super("Ya existe un producto con el código " + codigo);
    }
}
