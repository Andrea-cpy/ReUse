package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.academico.Carrera;

public interface CarreraDAO extends DAO<Carrera> {

    Carrera obtenerPorNombre(String nombre) throws SQLException;

    ArrayList<Carrera> listarPorFacultad(int idFacultad) throws SQLException;
}
