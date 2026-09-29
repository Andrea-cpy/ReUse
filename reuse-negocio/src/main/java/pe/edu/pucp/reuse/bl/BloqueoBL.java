package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;

/**
 * RF-10: insert registra el bloqueo y, en la misma transaccion, bloquea los
 * chats pendientes o activos entre ambos usuarios.
 */
public interface BloqueoBL extends RegistroBL<Bloqueo> {

    void desbloquear(int idBloqueo) throws BLException;
}
