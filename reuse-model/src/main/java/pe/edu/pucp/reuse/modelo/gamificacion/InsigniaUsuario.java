package pe.edu.pucp.reuse.modelo.gamificacion;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class InsigniaUsuario extends Registro {

    private int idInsigniaUsuario;
    private LocalDateTime fechaObtencion;

    private UsuarioPUCP usuario;
    private Insignia insignia;

    public InsigniaUsuario() {
    }

    // El id y la fecha de obtencion los genera la base de datos.
    public InsigniaUsuario(UsuarioPUCP usuario, Insignia insignia) {
        this();
        setUsuario(usuario);
        setInsignia(insignia);
    }

    public int getIdInsigniaUsuario() {
        return idInsigniaUsuario;
    }

    public void setIdInsigniaUsuario(int idInsigniaUsuario) {
        this.idInsigniaUsuario = idInsigniaUsuario;
    }

    public LocalDateTime getFechaObtencion() {
        return fechaObtencion;
    }

    public void setFechaObtencion(LocalDateTime fechaObtencion) {
        this.fechaObtencion = fechaObtencion;
    }

    public UsuarioPUCP getUsuario() {
        return usuario;
    }

    public final void setUsuario(UsuarioPUCP usuario) {
        if (this.usuario == usuario) {
            return;
        }
        UsuarioPUCP anterior = this.usuario;
        this.usuario = usuario;
        if (anterior != null) {
            anterior.quitarInsignia(this);
        }
        if (usuario != null) {
            usuario.agregarInsignia(this);
        }
    }

    public Insignia getInsignia() {
        return insignia;
    }

    public final void setInsignia(Insignia insignia) {
        if (this.insignia == insignia) {
            return;
        }
        Insignia anterior = this.insignia;
        this.insignia = insignia;
        if (anterior != null) {
            anterior.quitarOtorgamiento(this);
        }
        if (insignia != null) {
            insignia.agregarOtorgamiento(this);
        }
    }
}
