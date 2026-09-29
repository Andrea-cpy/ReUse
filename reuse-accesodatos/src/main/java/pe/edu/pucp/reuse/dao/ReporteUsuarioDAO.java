package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.enums.EstadoRevisionReporte;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;

/**
 * Reporte contra un usuario (tablas reporte + reporte_usuario). insert,
 * update y delete requieren una transaccion abierta por la capa de negocio.
 */
public interface ReporteUsuarioDAO extends DAO<ReporteUsuario> {

    int resolver(int idReporte, EstadoRevisionReporte estado, int idRevisor) throws SQLException;

    // RF-13: sanciones recibidas por el usuario en los ultimos "dias" dias.
    int contarSancionesRecientes(int idDenunciado, int dias) throws SQLException;
}
