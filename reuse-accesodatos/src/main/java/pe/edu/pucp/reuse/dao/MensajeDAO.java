package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.Mensaje;

public interface MensajeDAO extends DAO<Mensaje> {

    ArrayList<Mensaje> listarPorChat(int idChat) throws SQLException;
}
