package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;

public interface CanalChatDAO extends DAO<CanalChat> {

    // ACTIVO o RECHAZADO; registra fecha_respuesta (y fecha_cierre si se rechaza) con la hora de la BD.
    int responderSolicitud(int idChat, EstadoCanalChat nuevoEstado) throws SQLException;

    // CERRADO o BLOQUEADO; registra fecha_cierre con la hora de la BD.
    int cerrar(int idChat, EstadoCanalChat estadoCierre) throws SQLException;

    // RF-10: bloquea los chats pendientes o activos entre dos usuarios (en ambos sentidos).
    int bloquearChatsEntre(int idUsuarioA, int idUsuarioB) throws SQLException;

    // Chat PENDIENTE o ACTIVO de un comprador sobre un anuncio.
    CanalChat obtenerAbierto(int idAnuncio, int idComprador) throws SQLException;
}
