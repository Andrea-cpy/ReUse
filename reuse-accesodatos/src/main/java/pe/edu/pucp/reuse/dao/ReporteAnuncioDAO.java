package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.enums.EstadoRevisionReporte;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;

/**
 * Reporte contra un anuncio (tablas reporte + reporte_anuncio). insert,
 * update y delete requieren una transaccion abierta por la capa de negocio.
 */
public interface ReporteAnuncioDAO extends DAO<ReporteAnuncio> {

    int resolver(int idReporte, EstadoRevisionReporte estado, int idRevisor) throws SQLException;
}
