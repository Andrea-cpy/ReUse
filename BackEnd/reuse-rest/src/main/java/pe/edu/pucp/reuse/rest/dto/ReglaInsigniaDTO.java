package pe.edu.pucp.reuse.rest.dto;

/** RF-18: regla de una insignia (METRICA operador valorObjetivo). */
public class ReglaInsigniaDTO {
    private int idRegla;
    private String tipoMetrica;
    private String operador;
    private double valorObjetivo;

    public ReglaInsigniaDTO() {
    }

    public int getIdRegla() {
        return idRegla;
    }

    public void setIdRegla(int idRegla) {
        this.idRegla = idRegla;
    }

    public String getTipoMetrica() {
        return tipoMetrica;
    }

    public void setTipoMetrica(String tipoMetrica) {
        this.tipoMetrica = tipoMetrica;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public double getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(double valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }
}
