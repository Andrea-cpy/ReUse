package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;

public interface InsigniaUsuarioBL extends RegistroBL<InsigniaUsuario> {

    /**
     * Evalua todas las reglas de la insignia con las metricas reales del
     * usuario y, si las cumple y aun no la tiene, se la otorga. Devuelve
     * true si la otorgo.
     */
    boolean otorgarSiCumple(int idUsuario, int idInsignia) throws BLException;

    ArrayList<InsigniaUsuario> listarPorUsuario(int idUsuario) throws BLException;
}
