package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.modelo.academico.Carrera;

public interface CarreraDAO extends DAO<Carrera> {

    Carrera obtenerPorNombre(String nombre) throws SQLException;

    ArrayList<Carrera> listarPorFacultad(int idFacultad) throws SQLException;

    int contarUsuarios(int idCarrera) throws SQLException;

    // Materiales activos asociados a la carrera (material_carrera).
    int contarMateriales(int idCarrera) throws SQLException;
}
