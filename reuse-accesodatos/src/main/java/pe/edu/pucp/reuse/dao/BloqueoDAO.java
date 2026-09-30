package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;

public interface BloqueoDAO extends DAO<Bloqueo> {

    // Bloqueo ACTIVO entre dos usuarios, en cualquiera de los dos sentidos (bidireccional).
    Bloqueo obtenerActivoEntre(int idUsuarioA, int idUsuarioB) throws SQLException;
}
