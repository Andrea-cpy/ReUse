package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.academico.Facultad;

public interface FacultadDAO extends DAO<Facultad> {

    Facultad obtenerPorNombre(String nombre) throws SQLException;
}
