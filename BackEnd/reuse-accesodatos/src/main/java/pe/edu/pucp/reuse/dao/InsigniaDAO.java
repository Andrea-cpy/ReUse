package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;

public interface InsigniaDAO extends DAO<Insignia> {

    Insignia obtenerPorNombre(String nombre) throws SQLException;

    // Usuarios que tienen la insignia otorgada (insignia_usuario activos).
    int contarOtorgamientos(int idInsignia) throws SQLException;
}
