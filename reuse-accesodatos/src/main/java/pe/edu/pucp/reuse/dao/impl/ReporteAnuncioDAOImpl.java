package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.ReporteAnuncioDAO;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.enums.MotivoReporteAnuncio;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;

public class ReporteAnuncioDAOImpl extends ReporteDAOImpl<ReporteAnuncio> implements ReporteAnuncioDAO {

    @Override
    public int insert(ReporteAnuncio reporte) throws SQLException {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo");
        }
        // Supertipo y subtipo se guardan juntos: conexion de la transaccion abierta por la BL.
        Connection conn = TransactionsManager.getConnection();
        insertarReporte(conn, reporte);

        String sql = "{call insertar_reporte_anuncio(?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_reporte", reporte.getIdReporte());
            cmd.setString("p_motivo", reporte.getMotivo().name());
            cmd.setInt("p_id_anuncio", reporte.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", reporte.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(reporte.getUsuarioCreacion()));
            cmd.execute();
        }
        return reporte.getIdReporte();
    }

    @Override
    public int update(ReporteAnuncio reporte) throws SQLException {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        int filas = modificarReporte(conn, reporte);

        String sql = "{call modificar_reporte_anuncio(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_reporte", reporte.getIdReporte());
            cmd.setString("p_motivo", reporte.getMotivo().name());
            cmd.setInt("p_id_anuncio", reporte.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", reporte.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(reporte.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
        }
        return filas;
    }

    @Override
    public int delete(int idReporte) throws SQLException {
        Connection conn = TransactionsManager.getConnection();
        String sql = "{call eliminar_reporte_anuncio(?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_reporte", idReporte);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            if (cmd.getInt("p_filas") == 0) {
                return 0;
            }
        }
        return eliminarReporte(conn, idReporte);
    }

    @Override
    public ReporteAnuncio findById(int idReporte) throws SQLException {
        String sql = "{call buscar_reporte_anuncio_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idReporte);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ReporteAnuncio()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ReporteAnuncio> findAll() throws SQLException {
        String sql = "{call listar_reportes_anuncio()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<ReporteAnuncio> reportes = new ArrayList<>();
            while (rs.next()) {
                reportes.add(mapear(rs, new ReporteAnuncio()));
            }
            return reportes;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected ReporteAnuncio mapear(ResultSet rs, ReporteAnuncio reporte) throws SQLException {
        super.mapear(rs, reporte);
        reporte.setMotivo(MotivoReporteAnuncio.valueOf(rs.getString("motivo")));
        reporte.setAnuncio(mapearAnuncioReferencia(rs));
        return reporte;
    }
}
