package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pe.edu.pucp.reuse.dao.NotificacionDAO;
import pe.edu.pucp.reuse.dao.impl.NotificacionDAOImpl;
import pe.edu.pucp.reuse.modelo.enums.EstadoNotificacion;
import pe.edu.pucp.reuse.modelo.enums.TipoNotificacion;
import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * Avisos que el sistema deja al usuario (resolucion de reportes, suspension, insignias).
 * Se envian despues del commit de la operacion principal: si el aviso falla, la
 * operacion ya quedo registrada y solo se anota el error en el log.
 */
final class Notificador {
    private static final Logger LOG = Logger.getLogger(Notificador.class.getName());
    private static final int LONGITUD_MAXIMA = 500;

    private final NotificacionDAO notificacionDAO = new NotificacionDAOImpl();

    void avisar(int idDestinatario, TipoNotificacion tipo, String mensaje) {
        if (idDestinatario <= 0) {
            return;
        }
        UsuarioPUCP destinatario = new UsuarioPUCP();
        destinatario.setIdUsuario(idDestinatario);
        String texto = mensaje.length() > LONGITUD_MAXIMA ? mensaje.substring(0, LONGITUD_MAXIMA) : mensaje;
        Notificacion notificacion = new Notificacion(texto, tipo, destinatario);
        notificacion.setEstado(EstadoNotificacion.NO_LEIDA);
        try {
            notificacionDAO.insert(notificacion);
        } catch (SQLException | RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo registrar la notificacion para el usuario " + idDestinatario, e);
        }
    }
}
