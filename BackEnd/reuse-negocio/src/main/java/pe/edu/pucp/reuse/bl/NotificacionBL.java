package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;

public interface NotificacionBL extends RegistroBL<Notificacion> {

    ArrayList<Notificacion> listarPorDestinatario(int idDestinatario) throws BLException;

    void marcarComoLeida(int idNotificacion) throws BLException;

    /** Marca como leidas todas las notificaciones del usuario. Devuelve cuantas cambiaron. */
    int marcarTodasComoLeidas(int idDestinatario) throws BLException;

    /**
     * RF-12: citas CONFIRMADAS del usuario que ocurren dentro de las proximas horas indicadas.
     * Por cada una deja (una sola vez) una notificacion RECORDATORIO_CITA.
     */
    ArrayList<CitaEntrega> generarRecordatoriosCitas(int idUsuario, int horas)
            throws BLException;
}
