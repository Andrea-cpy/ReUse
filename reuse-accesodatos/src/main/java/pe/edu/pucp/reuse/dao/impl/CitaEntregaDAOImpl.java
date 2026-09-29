package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CitaEntregaDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCita;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class CitaEntregaDAOImpl extends RegistroDAOImpl<CitaEntrega> implements CitaEntregaDAO {

    private static final String SELECT_BASE = """
            SELECT ce.id_cita, ce.fecha_hora, ce.estado,
                   ce.id_punto_entrega, p.nombre AS punto_nombre, p.activo AS punto_activo,
                   ce.id_transaccion, t.estado AS transaccion_estado,
                   ce.activo, ce.fecha_creacion, ce.fecha_modificacion, ce.usuario_creacion, ce.usuario_modificacion
            FROM cita_entrega ce
            JOIN punto_entrega p ON p.id_punto_entrega = ce.id_punto_entrega
            JOIN transaccion t ON t.id_transaccion = ce.id_transaccion
            """;

    // fecha_hora si se envia: es la fecha acordada por los usuarios, no una marca de tiempo.
    @Override
    public int insert(CitaEntrega cita) throws SQLException {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }
        String sql = """
                INSERT INTO cita_entrega (fecha_hora, estado, id_punto_entrega, id_transaccion, activo,
                                          usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setTimestamp(1, Timestamp.valueOf(cita.getFechaHora()));
            cmd.setString(2, cita.getEstado().name());
            cmd.setInt(3, cita.getPuntoEntrega().getIdPuntoEntrega());
            cmd.setInt(4, cita.getTransaccion().getIdTransaccion());
            cmd.setBoolean(5, cita.isActivo());
            cmd.setString(6, usuarioAuditoria(cita.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la cita de entrega");
            }
            cita.setIdCita(leerIdGenerado(cmd));
            return cita.getIdCita();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(CitaEntrega cita) throws SQLException {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }
        String sql = """
                UPDATE cita_entrega
                SET fecha_hora = ?, estado = ?, id_punto_entrega = ?, id_transaccion = ?, activo = ?,
                    usuario_modificacion = ?
                WHERE id_cita = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setTimestamp(1, Timestamp.valueOf(cita.getFechaHora()));
            cmd.setString(2, cita.getEstado().name());
            cmd.setInt(3, cita.getPuntoEntrega().getIdPuntoEntrega());
            cmd.setInt(4, cita.getTransaccion().getIdTransaccion());
            cmd.setBoolean(5, cita.isActivo());
            cmd.setString(6, usuarioAuditoria(cita.getUsuarioModificacion()));
            cmd.setInt(7, cita.getIdCita());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCita) throws SQLException {
        String sql = """
                UPDATE cita_entrega SET activo = 0 WHERE id_cita = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCita);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CitaEntrega findById(int idCita) throws SQLException {
        String sql = SELECT_BASE + "WHERE ce.id_cita = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCita);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CitaEntrega()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<CitaEntrega> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE ce.activo = 1 ORDER BY ce.fecha_hora";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<CitaEntrega> citas = new ArrayList<>();
            while (rs.next()) {
                citas.add(mapear(rs, new CitaEntrega()));
            }
            return citas;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CitaEntrega obtenerPorTransaccion(int idTransaccion) throws SQLException {
        String sql = SELECT_BASE + "WHERE ce.id_transaccion = ? AND ce.activo = 1";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idTransaccion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CitaEntrega()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int cambiarEstado(int idCita, EstadoCita estado) throws SQLException {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        String sql = """
                UPDATE cita_entrega SET estado = ? WHERE id_cita = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estado.name());
            cmd.setInt(2, idCita);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected CitaEntrega mapear(ResultSet rs, CitaEntrega cita) throws SQLException {
        super.mapear(rs, cita);
        cita.setIdCita(rs.getInt("id_cita"));
        cita.setFechaHora(leerFechaHora(rs, "fecha_hora"));
        cita.setEstado(EstadoCita.valueOf(rs.getString("estado")));

        PuntoEntrega punto = new PuntoEntrega();
        punto.setIdPuntoEntrega(rs.getInt("id_punto_entrega"));
        punto.setNombre(rs.getString("punto_nombre"));
        punto.setActivo(rs.getBoolean("punto_activo"));
        cita.setPuntoEntrega(punto);

        Transaccion transaccion = new Transaccion();
        transaccion.setIdTransaccion(rs.getInt("id_transaccion"));
        transaccion.setEstado(EstadoTransaccion.valueOf(rs.getString("transaccion_estado")));
        cita.setTransaccion(transaccion);
        return cita;
    }
}
