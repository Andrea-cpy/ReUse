package pe.edu.pucp.reuse.rest.dto;

/** RF-10: bloqueo entre dos usuarios. */
public class BloqueoDTO {
    private int idBloqueo;
    private String fecha;
    private String estado;
    private int idBloqueador;
    private String nombreBloqueador;
    private int idBloqueado;
    private String nombreBloqueado;
    private String codigoBloqueado;

    public BloqueoDTO() {
    }

    public int getIdBloqueo() {
        return idBloqueo;
    }

    public void setIdBloqueo(int idBloqueo) {
        this.idBloqueo = idBloqueo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdBloqueador() {
        return idBloqueador;
    }

    public void setIdBloqueador(int idBloqueador) {
        this.idBloqueador = idBloqueador;
    }

    public String getNombreBloqueador() {
        return nombreBloqueador;
    }

    public void setNombreBloqueador(String nombreBloqueador) {
        this.nombreBloqueador = nombreBloqueador;
    }

    public int getIdBloqueado() {
        return idBloqueado;
    }

    public void setIdBloqueado(int idBloqueado) {
        this.idBloqueado = idBloqueado;
    }

    public String getNombreBloqueado() {
        return nombreBloqueado;
    }

    public void setNombreBloqueado(String nombreBloqueado) {
        this.nombreBloqueado = nombreBloqueado;
    }

    public String getCodigoBloqueado() {
        return codigoBloqueado;
    }

    public void setCodigoBloqueado(String codigoBloqueado) {
        this.codigoBloqueado = codigoBloqueado;
    }
}
