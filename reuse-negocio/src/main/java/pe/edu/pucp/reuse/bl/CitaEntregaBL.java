package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;

/**
 * RF-03: insert propone una cita; update la renegocia (solo si sigue PROPUESTA).
 */
public interface CitaEntregaBL extends RegistroBL<CitaEntrega> {

    CitaEntrega obtenerPorTransaccion(int idTransaccion) throws BLException;
}
