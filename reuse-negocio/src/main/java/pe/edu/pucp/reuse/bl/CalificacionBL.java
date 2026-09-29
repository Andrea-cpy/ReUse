package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;

/**
 * insert, update y delete recalculan la reputacion del calificado en la
 * misma transaccion (RF-18).
 */
public interface CalificacionBL extends RegistroBL<Calificacion> {
}
