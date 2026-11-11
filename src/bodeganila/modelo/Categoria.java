package bodeganila.modelo;

public enum Categoria {
    GOLOSINAS("Golosinas"),
    BEBIDAS("Bebidas"),
    ABARROTES("Abarrotes"),
    LIMPIEZA("Limpieza"),
    CUIDADO_PERSONAL("Cuidado personal"),
    VARIOS("Varios");

    private final String nombre;

    Categoria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
