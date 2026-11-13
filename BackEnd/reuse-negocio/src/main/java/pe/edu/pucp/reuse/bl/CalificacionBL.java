package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;

/**
 * Insert, update y delete recalculan la reputacion del calificado (como vendedor
 * o como comprador, segun el tipo de calificacion) en la
 * misma transaccion.
 */
public interface CalificacionBL extends RegistroBL<Calificacion> {

    /** Calificaciones que recibio el usuario (se muestran en su perfil). */
    ArrayList<Calificacion> listarPorCalificado(int idCalificado) throws BLException;

    /** Calificaciones que hizo el usuario (para saber que transacciones ya califico). */
    ArrayList<Calificacion> listarPorCalificador(int idCalificador) throws BLException;
}
