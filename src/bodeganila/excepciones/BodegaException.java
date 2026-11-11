package bodeganila.excepciones;

/** Raíz de las excepciones del negocio: el Menu puede capturarlas todas con un solo catch. */
public class BodegaException extends Exception {

    public BodegaException(String mensaje) {
        super(mensaje);
    }
}
