package pe.edu.pucp.reuse.modelo.enums;

public enum OperadorComparacion {
    MAYOR_O_IGUAL,
    MAYOR,
    MENOR_O_IGUAL,
    MENOR,
    IGUAL;

    public boolean comparar(double valorReal, double valorObjetivo) {
        switch (this) {
            case MAYOR_O_IGUAL:
                return valorReal >= valorObjetivo;
            case MAYOR:
                return valorReal > valorObjetivo;
            case MENOR_O_IGUAL:
                return valorReal <= valorObjetivo;
            case MENOR:
                return valorReal < valorObjetivo;
            case IGUAL:
                return Double.compare(valorReal, valorObjetivo) == 0;
            default:
                return false;
        }
    }
}
