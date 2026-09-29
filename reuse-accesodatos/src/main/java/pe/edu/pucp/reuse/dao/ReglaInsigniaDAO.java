package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public interface ReglaInsigniaDAO extends DAO<ReglaInsignia> {

    ArrayList<ReglaInsignia> listarPorInsignia(int idInsignia) throws SQLException;
}
