package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;

public interface RespuestaRapidaDAO extends DAO<RespuestaRapida> {

    ArrayList<RespuestaRapida> listarPorCreador(int idCreador) throws SQLException;
}
