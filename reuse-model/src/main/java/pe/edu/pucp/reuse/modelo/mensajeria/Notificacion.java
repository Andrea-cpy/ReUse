package pe.edu.pucp.reuse.modelo.mensajeria;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.EstadoNotificacion;
import pe.edu.pucp.reuse.modelo.enums.TipoNotificacion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Notificacion extends Registro {

    private int idNotificacion;
    private String mensaje;
    private LocalDateTime fechaHora;
    private TipoNotificacion tipo;
    private EstadoNotificacion estado;

    private UsuarioPUCP destinatario;

    public Notificacion() {
        this.estado = EstadoNotificacion.NO_LEIDA;
    }

    // El id y la fecha y hora los genera la base de datos.
    public Notificacion(String mensaje, TipoNotificacion tipo, UsuarioPUCP destinatario) {
        this();
        this.mensaje = mensaje;
        this.tipo = tipo;
        setDestinatario(destinatario);
    }

    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoNotificacion estado) {
        this.estado = estado;
    }

    public UsuarioPUCP getDestinatario() {
        return destinatario;
    }

    public final void setDestinatario(UsuarioPUCP destinatario) {
        if (this.destinatario == destinatario) {
            return;
        }
        UsuarioPUCP anterior = this.destinatario;
        this.destinatario = destinatario;
        if (anterior != null) {
            anterior.quitarNotificacion(this);
        }
        if (destinatario != null) {
            destinatario.agregarNotificacion(this);
        }
    }
}
