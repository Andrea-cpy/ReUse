package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
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

    @Override
    public int insert(ReporteUsuario reporte) throws SQLException {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo");
        }
        // Supertipo y subtipo se guardan juntos: conexion de la transaccion abierta por la BL.
        Connection conn = TransactionsManager.getConnection();
        insertarReporte(conn, reporte);

        String sql = "{call insertar_reporte_usuario(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_reporte", reporte.getIdReporte());
            cmd.setString("p_motivo", reporte.getMotivo().name());
            cmd.setInt("p_id_denunciado", reporte.getDenunciado().getIdUsuario());
            if (reporte.getTransaccion() != null) {
                cmd.setInt("p_id_transaccion", reporte.getTransaccion().getIdTransaccion());
            } else {
                cmd.setNull("p_id_transaccion", Types.INTEGER);
            }
            cmd.setBoolean("p_activo", reporte.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(reporte.getUsuarioCreacion()));
            cmd.execute();
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

        String sql = "{call modificar_reporte_usuario(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_reporte", reporte.getIdReporte());
            cmd.setString("p_motivo", reporte.getMotivo().name());
            cmd.setInt("p_id_denunciado", reporte.getDenunciado().getIdUsuario());
            if (reporte.getTransaccion() != null) {
                cmd.setInt("p_id_transaccion", reporte.getTransaccion().getIdTransaccion());
            } else {
                cmd.setNull("p_id_transaccion", Types.INTEGER);
            }
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
        String sql = "{call eliminar_reporte_usuario(?, ?)}";
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
    public ReporteUsuario findById(int idReporte) throws SQLException {
        String sql = "{call buscar_reporte_usuario_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idReporte);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ReporteUsuario()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ReporteUsuario> findAll() throws SQLException {
        String sql = "{call listar_reportes_usuario()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call contar_sanciones_recientes(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_denunciado", idDenunciado);
            cmd.setInt("p_dias", dias);
            cmd.registerOutParameter("p_total", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_total");
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
