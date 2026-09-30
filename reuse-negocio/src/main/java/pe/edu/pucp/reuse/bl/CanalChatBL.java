package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;

/**
 * Insert registra la solicitud de contacto (chat PENDIENTE); el vendedor
 * la acepta o la rechaza antes de habilitar el chat.
 */
public interface CanalChatBL extends RegistroBL<CanalChat> {

    void aceptarSolicitud(int idChat) throws BLException;

    void rechazarSolicitud(int idChat) throws BLException;

    void cerrar(int idChat) throws BLException;
}
