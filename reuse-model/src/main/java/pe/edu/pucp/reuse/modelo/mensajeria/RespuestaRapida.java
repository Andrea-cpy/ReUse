package pe.edu.pucp.reuse.modelo.mensajeria;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * La fecha de creacion ya no es un atributo propio: es la columna de
 * auditoria fecha_creacion heredada de Registro.
 */
public class RespuestaRapida extends Registro {

    private int idRespuesta;
    private String texto;

    private UsuarioPUCP creador;

    public RespuestaRapida() {
    }

    public RespuestaRapida(String texto, UsuarioPUCP creador) {
        this();
        this.texto = texto;
        setCreador(creador);
    }

    public int getIdRespuesta() {
        return idRespuesta;
    }

    public void setIdRespuesta(int idRespuesta) {
        this.idRespuesta = idRespuesta;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public UsuarioPUCP getCreador() {
        return creador;
    }

    public final void setCreador(UsuarioPUCP creador) {
        if (this.creador == creador) {
            return;
        }
        UsuarioPUCP anterior = this.creador;
        this.creador = creador;
        if (anterior != null) {
            anterior.quitarRespuestaRapida(this);
        }
        if (creador != null) {
            creador.agregarRespuestaRapida(this);
        }
    }
}
