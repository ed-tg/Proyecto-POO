package bodeganila.modelo;

public enum TipoMovimiento {
    ENTRADA,        // reposición o stock inicial
    SALIDA_VENTA,   // descuento por una venta
    AJUSTE,         // merma, vencimiento, rotura
    DEVOLUCION      // stock devuelto por una venta anulada
}
