package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;

public interface NotificacionBL extends RegistroBL<Notificacion> {

    ArrayList<Notificacion> listarPorDestinatario(int idDestinatario) throws BLException;

    void marcarComoLeida(int idNotificacion) throws BLException;
}
