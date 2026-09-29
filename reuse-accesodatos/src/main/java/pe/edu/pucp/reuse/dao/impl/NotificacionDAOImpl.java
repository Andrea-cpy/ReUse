package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.NotificacionDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoNotificacion;
import pe.edu.pucp.reuse.modelo.enums.TipoNotificacion;
import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;

public class NotificacionDAOImpl extends RegistroDAOImpl<Notificacion> implements NotificacionDAO {

    // fecha_hora no se envia: la asigna la base de datos.
    @Override
    public int insert(Notificacion notificacion) throws SQLException {
        if (notificacion == null) {
            throw new IllegalArgumentException("La notificacion no puede ser nula");
        }
        String sql = "{call insertar_notificacion(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_mensaje", notificacion.getMensaje());
            cmd.setString("p_tipo", notificacion.getTipo().name());
            cmd.setString("p_estado", notificacion.getEstado().name());
            cmd.setInt("p_id_destinatario", notificacion.getDestinatario().getIdUsuario());
            cmd.setBoolean("p_activo", notificacion.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(notificacion.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            notificacion.setIdNotificacion(cmd.getInt("p_id"));
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
        String sql = "{call modificar_notificacion(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", notificacion.getIdNotificacion());
            cmd.setString("p_mensaje", notificacion.getMensaje());
            cmd.setString("p_tipo", notificacion.getTipo().name());
            cmd.setString("p_estado", notificacion.getEstado().name());
            cmd.setInt("p_id_destinatario", notificacion.getDestinatario().getIdUsuario());
            cmd.setBoolean("p_activo", notificacion.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(notificacion.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idNotificacion) throws SQLException {
        String sql = "{call eliminar_notificacion(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idNotificacion);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Notificacion findById(int idNotificacion) throws SQLException {
        String sql = "{call buscar_notificacion_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idNotificacion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Notificacion()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Notificacion> findAll() throws SQLException {
        String sql = "{call listar_notificaciones()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call listar_notificaciones_por_destinatario(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_destinatario", idDestinatario);
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
