package bodeganila.modelo;

public enum UnidadMedida {
    UNIDAD("und", false),
    PAQUETE("paq", false),
    KILOGRAMO("kg", true);

    private final String abreviatura;
    private final boolean admiteDecimales;

    UnidadMedida(String abreviatura, boolean admiteDecimales) {
        this.abreviatura = abreviatura;
        this.admiteDecimales = admiteDecimales;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public boolean admiteDecimales() {
        return admiteDecimales;
    }
}
