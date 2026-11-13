package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;

/**
 * Insert registra la solicitud de contacto (chat PENDIENTE); el vendedor
 * la acepta o la rechaza antes de habilitar el chat.
 */
public interface CanalChatBL extends RegistroBL<CanalChat> {

    void aceptarSolicitud(int idChat) throws BLException;

    void rechazarSolicitud(int idChat) throws BLException;

    void cerrar(int idChat) throws BLException;

    /** Chats donde el usuario es el comprador o el vendedor del anuncio. */
    ArrayList<CanalChat> listarPorUsuario(int idUsuario) throws BLException;
}
