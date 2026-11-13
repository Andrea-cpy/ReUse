package pe.edu.pucp.reuse.rest.dto;

public class PuntoEntregaDTO {
    private int idPuntoEntrega;
    private String nombre;
    private String referencia;
    private String ubicacion;
    private boolean activo;

    public PuntoEntregaDTO() {
    }

    public int getIdPuntoEntrega() {
        return idPuntoEntrega;
    }

    public void setIdPuntoEntrega(int idPuntoEntrega) {
        this.idPuntoEntrega = idPuntoEntrega;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
