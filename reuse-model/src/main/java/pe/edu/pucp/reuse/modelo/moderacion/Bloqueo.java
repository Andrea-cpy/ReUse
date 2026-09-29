package pe.edu.pucp.reuse.modelo.moderacion;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.EstadoBloqueo;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Bloqueo extends Registro {

    private int idBloqueo;
    private LocalDateTime fecha;
    private EstadoBloqueo estado;

    private UsuarioPUCP bloqueador;
    private UsuarioPUCP bloqueado;

    public Bloqueo() {
        this.estado = EstadoBloqueo.ACTIVO;
    }

    // El id y la fecha del bloqueo los genera la base de datos.
    public Bloqueo(UsuarioPUCP bloqueador, UsuarioPUCP bloqueado) {
        this();
        setBloqueador(bloqueador);
        setBloqueado(bloqueado);
    }

    public int getIdBloqueo() {
        return idBloqueo;
    }

    public void setIdBloqueo(int idBloqueo) {
        this.idBloqueo = idBloqueo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoBloqueo getEstado() {
        return estado;
    }

    public void setEstado(EstadoBloqueo estado) {
        this.estado = estado;
    }

    public UsuarioPUCP getBloqueador() {
        return bloqueador;
    }

    public final void setBloqueador(UsuarioPUCP bloqueador) {
        if (this.bloqueador == bloqueador) {
            return;
        }
        UsuarioPUCP anterior = this.bloqueador;
        this.bloqueador = bloqueador;
        if (anterior != null) {
            anterior.quitarBloqueoRealizado(this);
        }
        if (bloqueador != null) {
            bloqueador.agregarBloqueoRealizado(this);
        }
    }

    public UsuarioPUCP getBloqueado() {
        return bloqueado;
    }

    public final void setBloqueado(UsuarioPUCP bloqueado) {
        if (this.bloqueado == bloqueado) {
            return;
        }
        UsuarioPUCP anterior = this.bloqueado;
        this.bloqueado = bloqueado;
        if (anterior != null) {
            anterior.quitarBloqueoRecibido(this);
        }
        if (bloqueado != null) {
            bloqueado.agregarBloqueoRecibido(this);
        }
    }
}
