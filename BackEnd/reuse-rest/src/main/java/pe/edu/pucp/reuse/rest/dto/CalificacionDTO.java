package pe.edu.pucp.reuse.rest.dto;

/** RF-15 / RF-18: calificacion de una transaccion COMPLETADA (1 a 5). */
public class CalificacionDTO {
    private int idCalificacion;
    private int puntaje;
    private String comentario;
    private String fecha;
    private String tipo;
    private int idTransaccion;
    private int idCalificador;
    private String nombreCalificador;
    private int idCalificado;
    private String nombreCalificado;

    public CalificacionDTO() {
    }

    public int getIdCalificacion() {
        return idCalificacion;
    }

    public void setIdCalificacion(int idCalificacion) {
        this.idCalificacion = idCalificacion;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public int getIdCalificador() {
        return idCalificador;
    }

    public void setIdCalificador(int idCalificador) {
        this.idCalificador = idCalificador;
    }

    public String getNombreCalificador() {
        return nombreCalificador;
    }

    public void setNombreCalificador(String nombreCalificador) {
        this.nombreCalificador = nombreCalificador;
    }

    public int getIdCalificado() {
        return idCalificado;
    }

    public void setIdCalificado(int idCalificado) {
        this.idCalificado = idCalificado;
    }

    public String getNombreCalificado() {
        return nombreCalificado;
    }

    public void setNombreCalificado(String nombreCalificado) {
        this.nombreCalificado = nombreCalificado;
    }
}
