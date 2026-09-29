package pe.edu.pucp.reuse.modelo.mensajeria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * Las transiciones del chat (aceptar, rechazar, bloquear, cerrar) se validan
 * en la capa de negocio (CanalChatBL); las fechas las asigna la base de datos.
 */
public class CanalChat extends Registro {

    private int idChat;
    private EstadoCanalChat estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaCierre;

    private Anuncio anuncio;
    private UsuarioPUCP comprador;
    private final List<Mensaje> mensajes;

    public CanalChat() {
        this.estado = EstadoCanalChat.PENDIENTE;
        this.mensajes = new ArrayList<>();
    }

    public CanalChat(Anuncio anuncio, UsuarioPUCP comprador) {
        this();
        setAnuncio(anuncio);
        setComprador(comprador);
    }

    // El vendedor no se duplica: se obtiene navegando por el anuncio.
    public UsuarioPUCP getVendedor() {
        return anuncio == null ? null : anuncio.getVendedor();
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public EstadoCanalChat getEstado() {
        return estado;
    }

    public void setEstado(EstadoCanalChat estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
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
            anterior.quitarCanalChat(this);
        }
        if (anuncio != null) {
            anuncio.agregarCanalChat(this);
        }
    }

    public UsuarioPUCP getComprador() {
        return comprador;
    }

    public final void setComprador(UsuarioPUCP comprador) {
        if (this.comprador == comprador) {
            return;
        }
        UsuarioPUCP anterior = this.comprador;
        this.comprador = comprador;
        if (anterior != null) {
            anterior.quitarChatComoComprador(this);
        }
        if (comprador != null) {
            comprador.agregarChatComoComprador(this);
        }
    }

    public List<Mensaje> getMensajes() {
        return Collections.unmodifiableList(mensajes);
    }

    public void agregarMensaje(Mensaje mensaje) {
        if (mensaje == null || mensajes.contains(mensaje)) {
            return;
        }
        mensajes.add(mensaje);
        mensaje.setCanalChat(this);
    }

    public void quitarMensaje(Mensaje mensaje) {
        if (mensajes.remove(mensaje) && mensaje.getCanalChat() == this) {
            mensaje.setCanalChat(null);
        }
    }
}
