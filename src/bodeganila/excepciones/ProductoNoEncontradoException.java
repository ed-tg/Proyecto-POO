package bodeganila.excepciones;

public class ProductoNoEncontradoException extends BodegaException {

    public ProductoNoEncontradoException(String codigo) {
        super("No existe un producto activo con el código " + codigo);
    }
}
