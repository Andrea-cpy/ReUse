package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.ReporteUsuarioDAO;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.enums.MotivoReporteUsuario;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class ReporteUsuarioDAOImpl extends ReporteDAOImpl<ReporteUsuario> implements ReporteUsuarioDAO {

    private static final String SELECT_BASE = "SELECT " + COLUMNAS_REPORTE + """
                   , ru.motivo, ru.id_transaccion,
                   dn.id_usuario AS denunciado_id, dn.codigo_pucp AS denunciado_codigo,
                   dn.nombres AS denunciado_nombres, dn.apellido_paterno AS denunciado_apellido
            FROM reporte r
            JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
            JOIN usuario d ON d.id_usuario = r.id_denunciante
            LEFT JOIN usuario rv ON rv.id_usuario = r.id_revisor
            JOIN usuario dn ON dn.id_usuario = ru.id_denunciado
            """;

    @Override
    public int insert(ReporteUsuario reporte) throws SQLException {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo");
        }
        // Supertipo y subtipo se guardan juntos: conexion de la transaccion abierta por la BL.
        Connection conn = TransactionsManager.getConnection();
        insertarReporte(conn, reporte);

        String sql = """
                INSERT INTO reporte_usuario (id_reporte, motivo, id_denunciado, id_transaccion, activo,
                                             usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, reporte.getIdReporte());
            cmd.setString(2, reporte.getMotivo().name());
            cmd.setInt(3, reporte.getDenunciado().getIdUsuario());
            if (reporte.getTransaccion() != null) {
                cmd.setInt(4, reporte.getTransaccion().getIdTransaccion());
            } else {
                cmd.setNull(4, Types.INTEGER);
            }
            cmd.setBoolean(5, reporte.isActivo());
            cmd.setString(6, usuarioAuditoria(reporte.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el reporte de usuario");
            }
        }
        return reporte.getIdReporte();
    }

    @Override
    public int update(ReporteUsuario reporte) throws SQLException {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        int filas = modificarReporte(conn, reporte);

        String sql = """
                UPDATE reporte_usuario
                SET motivo = ?, id_denunciado = ?, id_transaccion = ?, activo = ?, usuario_modificacion = ?
                WHERE id_reporte = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, reporte.getMotivo().name());
            cmd.setInt(2, reporte.getDenunciado().getIdUsuario());
            if (reporte.getTransaccion() != null) {
                cmd.setInt(3, reporte.getTransaccion().getIdTransaccion());
            } else {
                cmd.setNull(3, Types.INTEGER);
            }
            cmd.setBoolean(4, reporte.isActivo());
            cmd.setString(5, usuarioAuditoria(reporte.getUsuarioModificacion()));
            cmd.setInt(6, reporte.getIdReporte());
            cmd.executeUpdate();
        }
        return filas;
    }

    @Override
    public int delete(int idReporte) throws SQLException {
        Connection conn = TransactionsManager.getConnection();
        String sql = """
                UPDATE reporte_usuario SET activo = 0 WHERE id_reporte = ?
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
    public ReporteUsuario findById(int idReporte) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.id_reporte = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idReporte);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ReporteUsuario()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ReporteUsuario> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE r.activo = 1 ORDER BY r.fecha_registro DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<ReporteUsuario> reportes = new ArrayList<>();
            while (rs.next()) {
                reportes.add(mapear(rs, new ReporteUsuario()));
            }
            return reportes;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int contarSancionesRecientes(int idDenunciado, int dias) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM reporte r
                JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
                WHERE ru.id_denunciado = ? AND r.estado_revision = 'SANCIONADO' AND r.activo = 1
                  AND r.fecha_revision >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? DAY)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idDenunciado);
            cmd.setInt(2, dias);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected ReporteUsuario mapear(ResultSet rs, ReporteUsuario reporte) throws SQLException {
        super.mapear(rs, reporte);
        reporte.setMotivo(MotivoReporteUsuario.valueOf(rs.getString("motivo")));
        reporte.setDenunciado(mapearUsuarioReferencia(rs, "denunciado"));
        int idTransaccion = rs.getInt("id_transaccion");
        if (!rs.wasNull()) {
            Transaccion transaccion = new Transaccion();
            transaccion.setIdTransaccion(idTransaccion);
            reporte.setTransaccion(transaccion);
        }
        return reporte;
    }
}
