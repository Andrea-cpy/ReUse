package pe.edu.pucp.reuse.modelo.mensajeria;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Mensaje extends Registro {

    private int idMensaje;
    private String contenido;
    private LocalDateTime fechaHora;
    private boolean leido;

    private CanalChat canalChat;
    private UsuarioPUCP emisor;

    public Mensaje() {
        this.leido = false;
    }

    // El id y la fecha y hora del mensaje los genera la base de datos.
    public Mensaje(String contenido, CanalChat canalChat, UsuarioPUCP emisor) {
        this();
        this.contenido = contenido;
        this.emisor = emisor;
        setCanalChat(canalChat);
    }

    public int getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(int idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public CanalChat getCanalChat() {
        return canalChat;
    }

    public final void setCanalChat(CanalChat canalChat) {
        if (this.canalChat == canalChat) {
            return;
        }
        CanalChat anterior = this.canalChat;
        this.canalChat = canalChat;
        if (anterior != null) {
            anterior.quitarMensaje(this);
        }
        if (canalChat != null) {
            canalChat.agregarMensaje(this);
        }
    }

    public UsuarioPUCP getEmisor() {
        return emisor;
    }

    public void setEmisor(UsuarioPUCP emisor) {
        this.emisor = emisor;
    }
}
