package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;

/**
 * insert registra la insignia con sus reglas en una sola transaccion.
 */
public interface InsigniaBL extends RegistroBL<Insignia> {

    /** Todas las insignias con sus reglas cargadas (el listado simple no trae el detalle). */
    ArrayList<Insignia> listarConReglas() throws BLException;

    /** Actualiza la cabecera y reemplaza sus reglas en una sola transaccion. */
    int actualizarConReglas(Insignia insignia) throws BLException;
}
