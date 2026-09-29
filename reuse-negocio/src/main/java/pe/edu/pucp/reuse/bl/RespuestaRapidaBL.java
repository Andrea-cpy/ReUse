package pe.edu.pucp.reuse.bl;

import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;

public interface RespuestaRapidaBL extends RegistroBL<RespuestaRapida> {

    ArrayList<RespuestaRapida> listarPorCreador(int idCreador) throws BLException;
}
