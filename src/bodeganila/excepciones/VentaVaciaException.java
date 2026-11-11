package bodeganila.excepciones;

public class VentaVaciaException extends BodegaException {

    public VentaVaciaException() {
        super("No se puede cobrar una venta sin productos");
    }
}
