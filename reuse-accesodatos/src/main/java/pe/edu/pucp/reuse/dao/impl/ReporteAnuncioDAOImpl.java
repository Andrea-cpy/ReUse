package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.ReporteAnuncioDAO;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.enums.MotivoReporteAnuncio;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;

public class ReporteAnuncioDAOImpl extends ReporteDAOImpl<ReporteAnuncio> implements ReporteAnuncioDAO {

    private static final String SELECT_BASE = "SELECT " + COLUMNAS_REPORTE + """
                   , ra.motivo,
                   a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
                   a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor
            FROM reporte r
            JOIN reporte_anuncio ra ON ra.id_reporte = r.id_reporte
            JOIN usuario d ON d.id_usuario = r.id_denunciante
            LEFT JOIN usuario rv ON rv.id_usuario = r.id_revisor
            JOIN anuncio a ON a.id_anuncio = ra.id_anuncio
            """;

    @Override
    public int insert(ReporteAnuncio reporte) throws SQLException {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo");
        }
        // Supertipo y subtipo se guardan juntos: conexion de la transaccion abierta por la BL.
        Connection conn = TransactionsManager.getConnection();
        insertarReporte(conn, reporte);

        String sql = """
                INSERT INTO reporte_anuncio (id_reporte, motivo, id_anuncio, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, reporte.getIdReporte());
            cmd.setString(2, reporte.getMotivo().name());
            cmd.setInt(3, reporte.getAnuncio().getIdAnuncio());
            cmd.setBoolean(4, reporte.isActivo());
            cmd.setString(5, usuarioAuditoria(reporte.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el reporte de anuncio");
            }
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

        String sql = """
                UPDATE reporte_anuncio
                SET motivo = ?, id_anuncio = ?, activo = ?, usuario_modificacion = ?
                WHERE id_reporte = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, reporte.getMotivo().name());
            cmd.setInt(2, reporte.getAnuncio().getIdAnuncio());
            cmd.setBoolean(3, reporte.isActivo());
            cmd.setString(4, usuarioAuditoria(reporte.getUsuarioModificacion()));
            cmd.setInt(5, reporte.getIdReporte());
            cmd.executeUpdate();
        }
        return filas;
    }

    @Override
    public int delete(int idReporte) throws SQLException {
        Connection conn = TransactionsManager.getConnection();
        String sql = """
                UPDATE reporte_anuncio SET activo = 0 WHERE id_reporte = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idReporte);
            if (cmd.executeUpdate() == 0) {
                return 0;
            }
        }
        return eliminarReporte(conn, idReporte);
    }

    @Override
    public ReporteAnuncio findById(int idReporte) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.id_reporte = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idReporte);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ReporteAnuncio()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ReporteAnuncio> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE r.activo = 1 ORDER BY r.fecha_registro DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
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
