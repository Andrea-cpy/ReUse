package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;

public interface CalificacionDAO extends DAO<Calificacion> {

    Calificacion obtenerPorTransaccionYCalificador(int idTransaccion, int idCalificador) throws SQLException;
}
