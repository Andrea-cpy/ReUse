package pe.edu.pucp.reuse.rest.dto;

/** 'fechaHora' en formato ISO-8601, por ejemplo 2026-11-12T12:30:00. */
public class CitaEntregaDTO {
    private int idCita;
    private String fechaHora;
    private String estado;
    private int idPuntoEntrega;
    private String nombrePuntoEntrega;
    private int idTransaccion;

    public CitaEntregaDTO() {
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdPuntoEntrega() {
        return idPuntoEntrega;
    }

    public void setIdPuntoEntrega(int idPuntoEntrega) {
        this.idPuntoEntrega = idPuntoEntrega;
    }

    public String getNombrePuntoEntrega() {
        return nombrePuntoEntrega;
    }

    public void setNombrePuntoEntrega(String nombrePuntoEntrega) {
        this.nombrePuntoEntrega = nombrePuntoEntrega;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }
}
