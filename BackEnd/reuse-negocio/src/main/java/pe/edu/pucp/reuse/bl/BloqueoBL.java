package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;

/**
 * Insert registra el bloqueo y, en la misma transaccion, bloquea los chats
 * pendientes o activos entre ambos usuarios.
 */
public interface BloqueoBL extends RegistroBL<Bloqueo> {

    void desbloquear(int idBloqueo) throws BLException;

    /** Bloqueos ACTIVOS que registro el usuario (RF-10: "Mis bloqueos"). */
    ArrayList<Bloqueo> listarPorBloqueador(int idBloqueador) throws BLException;

    /** Bloqueo ACTIVO entre dos usuarios en cualquier sentido, o null si no existe. */
    Bloqueo obtenerActivoEntre(int idUsuarioA, int idUsuarioB) throws BLException;
}
