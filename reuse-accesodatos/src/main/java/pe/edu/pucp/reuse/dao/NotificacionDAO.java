package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;

public interface NotificacionDAO extends DAO<Notificacion> {

    ArrayList<Notificacion> listarPorDestinatario(int idDestinatario) throws SQLException;
}
