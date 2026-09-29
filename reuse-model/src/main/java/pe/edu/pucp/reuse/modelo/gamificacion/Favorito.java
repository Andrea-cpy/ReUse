package pe.edu.pucp.reuse.modelo.gamificacion;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Favorito extends Registro {

    private int idFavorito;
    private LocalDateTime fechaGuardado;

    private UsuarioPUCP usuario;
    private Anuncio anuncio;

    public Favorito() {
    }

    // El id y la fecha en que se guardo los genera la base de datos.
    public Favorito(UsuarioPUCP usuario, Anuncio anuncio) {
        this();
        setUsuario(usuario);
        setAnuncio(anuncio);
    }

    public int getIdFavorito() {
        return idFavorito;
    }

    public void setIdFavorito(int idFavorito) {
        this.idFavorito = idFavorito;
    }

    public LocalDateTime getFechaGuardado() {
        return fechaGuardado;
    }

    public void setFechaGuardado(LocalDateTime fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
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
            anterior.quitarFavorito(this);
        }
        if (usuario != null) {
            usuario.agregarFavorito(this);
        }
    }

    public Anuncio getAnuncio() {
        return anuncio;
    }

    public final void setAnuncio(Anuncio anuncio) {
        if (this.anuncio == anuncio) {
            return;
        }
        Anuncio anterior = this.anuncio;
        this.anuncio = anuncio;
        if (anterior != null) {
            anterior.quitarFavorito(this);
        }
        if (anuncio != null) {
            anuncio.agregarFavorito(this);
        }
    }
}
