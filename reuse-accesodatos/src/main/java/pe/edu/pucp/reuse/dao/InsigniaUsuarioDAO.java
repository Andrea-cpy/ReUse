package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;

public interface InsigniaUsuarioDAO extends DAO<InsigniaUsuario> {

    // Devuelve el otorgamiento aunque este inactivo (la pareja usuario-insignia es UNIQUE).
    InsigniaUsuario obtenerPorUsuarioEInsignia(int idUsuario, int idInsignia) throws SQLException;

    ArrayList<InsigniaUsuario> listarPorUsuario(int idUsuario) throws SQLException;
}
