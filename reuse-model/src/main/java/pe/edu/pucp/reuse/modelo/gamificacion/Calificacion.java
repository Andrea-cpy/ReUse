package pe.edu.pucp.reuse.modelo.gamificacion;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.TipoCalificacion;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Calificacion extends Registro {

    private int idCalificacion;
    private int puntaje;
    private String comentario;
    private LocalDateTime fecha;
    private TipoCalificacion tipoCalificacion;

    private Transaccion transaccion;
    private UsuarioPUCP calificador;
    private UsuarioPUCP calificado;

    public Calificacion() {
    }

    // El id y la fecha los genera la base de datos. El rango 1..5 lo valida CalificacionBL.
    public Calificacion(int puntaje, String comentario, TipoCalificacion tipoCalificacion,
                        Transaccion transaccion, UsuarioPUCP calificador, UsuarioPUCP calificado) {
        this();
        this.puntaje = puntaje;
        this.comentario = comentario;
        this.tipoCalificacion = tipoCalificacion;
        setTransaccion(transaccion);
        setCalificador(calificador);
        setCalificado(calificado);
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoCalificacion getTipoCalificacion() {
        return tipoCalificacion;
    }

    public void setTipoCalificacion(TipoCalificacion tipoCalificacion) {
        this.tipoCalificacion = tipoCalificacion;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public final void setTransaccion(Transaccion transaccion) {
        if (this.transaccion == transaccion) {
            return;
        }
        Transaccion anterior = this.transaccion;
        this.transaccion = transaccion;
        if (anterior != null) {
            anterior.quitarCalificacion(this);
        }
        if (transaccion != null) {
            transaccion.agregarCalificacion(this);
        }
    }

    public UsuarioPUCP getCalificador() {
        return calificador;
    }

    public final void setCalificador(UsuarioPUCP calificador) {
        if (this.calificador == calificador) {
            return;
        }
        UsuarioPUCP anterior = this.calificador;
        this.calificador = calificador;
        if (anterior != null) {
            anterior.quitarCalificacionRealizada(this);
        }
        if (calificador != null) {
            calificador.agregarCalificacionRealizada(this);
        }
    }

    public UsuarioPUCP getCalificado() {
        return calificado;
    }

    public final void setCalificado(UsuarioPUCP calificado) {
        if (this.calificado == calificado) {
            return;
        }
        UsuarioPUCP anterior = this.calificado;
        this.calificado = calificado;
        if (anterior != null) {
            anterior.quitarCalificacionRecibida(this);
        }
        if (calificado != null) {
            calificado.agregarCalificacionRecibida(this);
        }
    }
}
