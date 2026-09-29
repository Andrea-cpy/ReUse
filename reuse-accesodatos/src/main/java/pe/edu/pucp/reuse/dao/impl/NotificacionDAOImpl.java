package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.NotificacionDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoNotificacion;
import pe.edu.pucp.reuse.modelo.enums.TipoNotificacion;
import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;

public class NotificacionDAOImpl extends RegistroDAOImpl<Notificacion> implements NotificacionDAO {

    private static final String SELECT_BASE = """
            SELECT n.id_notificacion, n.mensaje, n.fecha_hora, n.tipo, n.estado,
                   u.id_usuario AS destinatario_id, u.codigo_pucp AS destinatario_codigo,
                   u.nombres AS destinatario_nombres, u.apellido_paterno AS destinatario_apellido,
                   n.activo, n.fecha_creacion, n.fecha_modificacion, n.usuario_creacion, n.usuario_modificacion
            FROM notificacion n
            JOIN usuario u ON u.id_usuario = n.id_destinatario
            """;

    // fecha_hora no se envia: la asigna la base de datos.
    @Override
    public int insert(Notificacion notificacion) throws SQLException {
        if (notificacion == null) {
            throw new IllegalArgumentException("La notificacion no puede ser nula");
        }
        String sql = """
                INSERT INTO notificacion (mensaje, tipo, estado, id_destinatario, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, notificacion.getMensaje());
            cmd.setString(2, notificacion.getTipo().name());
            cmd.setString(3, notificacion.getEstado().name());
            cmd.setInt(4, notificacion.getDestinatario().getIdUsuario());
            cmd.setBoolean(5, notificacion.isActivo());
            cmd.setString(6, usuarioAuditoria(notificacion.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la notificacion");
            }
            notificacion.setIdNotificacion(leerIdGenerado(cmd));
            return notificacion.getIdNotificacion();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Notificacion notificacion) throws SQLException {
        if (notificacion == null) {
            throw new IllegalArgumentException("La notificacion no puede ser nula");
        }
        String sql = """
                UPDATE notificacion
                SET mensaje = ?, tipo = ?, estado = ?, id_destinatario = ?, activo = ?, usuario_modificacion = ?
                WHERE id_notificacion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, notificacion.getMensaje());
            cmd.setString(2, notificacion.getTipo().name());
            cmd.setString(3, notificacion.getEstado().name());
            cmd.setInt(4, notificacion.getDestinatario().getIdUsuario());
            cmd.setBoolean(5, notificacion.isActivo());
            cmd.setString(6, usuarioAuditoria(notificacion.getUsuarioModificacion()));
            cmd.setInt(7, notificacion.getIdNotificacion());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idNotificacion) throws SQLException {
        String sql = """
                UPDATE notificacion SET activo = 0 WHERE id_notificacion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idNotificacion);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Notificacion findById(int idNotificacion) throws SQLException {
        String sql = SELECT_BASE + "WHERE n.id_notificacion = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idNotificacion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Notificacion()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Notificacion> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE n.activo = 1 ORDER BY n.fecha_hora DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Notificacion> notificaciones = new ArrayList<>();
            while (rs.next()) {
                notificaciones.add(mapear(rs, new Notificacion()));
            }
            return notificaciones;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Notificacion> listarPorDestinatario(int idDestinatario) throws SQLException {
        String sql = SELECT_BASE + "WHERE n.id_destinatario = ? AND n.activo = 1 ORDER BY n.fecha_hora DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idDestinatario);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<Notificacion> notificaciones = new ArrayList<>();
                while (rs.next()) {
                    notificaciones.add(mapear(rs, new Notificacion()));
                }
                return notificaciones;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Notificacion mapear(ResultSet rs, Notificacion notificacion) throws SQLException {
        super.mapear(rs, notificacion);
        notificacion.setIdNotificacion(rs.getInt("id_notificacion"));
        notificacion.setMensaje(rs.getString("mensaje"));
        notificacion.setFechaHora(leerFechaHora(rs, "fecha_hora"));
        notificacion.setTipo(TipoNotificacion.valueOf(rs.getString("tipo")));
        notificacion.setEstado(EstadoNotificacion.valueOf(rs.getString("estado")));
        notificacion.setDestinatario(mapearUsuarioReferencia(rs, "destinatario"));
        return notificacion;
    }
}
