package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.MensajeDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.Mensaje;

public class MensajeDAOImpl extends RegistroDAOImpl<Mensaje> implements MensajeDAO {

    private static final String SELECT_BASE = """
            SELECT me.id_mensaje, me.contenido, me.fecha_hora, me.leido,
                   me.id_chat, ch.estado AS chat_estado,
                   u.id_usuario AS emisor_id, u.codigo_pucp AS emisor_codigo,
                   u.nombres AS emisor_nombres, u.apellido_paterno AS emisor_apellido,
                   me.activo, me.fecha_creacion, me.fecha_modificacion, me.usuario_creacion, me.usuario_modificacion
            FROM mensaje me
            JOIN canal_chat ch ON ch.id_chat = me.id_chat
            JOIN usuario u ON u.id_usuario = me.id_emisor
            """;

    // fecha_hora no se envia: la asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).
    @Override
    public int insert(Mensaje mensaje) throws SQLException {
        if (mensaje == null) {
            throw new IllegalArgumentException("El mensaje no puede ser nulo");
        }
        String sql = """
                INSERT INTO mensaje (contenido, leido, id_chat, id_emisor, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, mensaje.getContenido());
            cmd.setBoolean(2, mensaje.isLeido());
            cmd.setInt(3, mensaje.getCanalChat().getIdChat());
            cmd.setInt(4, mensaje.getEmisor().getIdUsuario());
            cmd.setBoolean(5, mensaje.isActivo());
            cmd.setString(6, usuarioAuditoria(mensaje.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el mensaje");
            }
            mensaje.setIdMensaje(leerIdGenerado(cmd));
            return mensaje.getIdMensaje();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Mensaje mensaje) throws SQLException {
        if (mensaje == null) {
            throw new IllegalArgumentException("El mensaje no puede ser nulo");
        }
        String sql = """
                UPDATE mensaje
                SET contenido = ?, leido = ?, id_chat = ?, id_emisor = ?, activo = ?, usuario_modificacion = ?
                WHERE id_mensaje = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, mensaje.getContenido());
            cmd.setBoolean(2, mensaje.isLeido());
            cmd.setInt(3, mensaje.getCanalChat().getIdChat());
            cmd.setInt(4, mensaje.getEmisor().getIdUsuario());
            cmd.setBoolean(5, mensaje.isActivo());
            cmd.setString(6, usuarioAuditoria(mensaje.getUsuarioModificacion()));
            cmd.setInt(7, mensaje.getIdMensaje());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idMensaje) throws SQLException {
        String sql = """
                UPDATE mensaje SET activo = 0 WHERE id_mensaje = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idMensaje);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Mensaje findById(int idMensaje) throws SQLException {
        String sql = SELECT_BASE + "WHERE me.id_mensaje = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idMensaje);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Mensaje()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Mensaje> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE me.activo = 1 ORDER BY me.fecha_hora";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Mensaje> mensajes = new ArrayList<>();
            while (rs.next()) {
                mensajes.add(mapear(rs, new Mensaje()));
            }
            return mensajes;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Mensaje> listarPorChat(int idChat) throws SQLException {
        String sql = SELECT_BASE + "WHERE me.id_chat = ? AND me.activo = 1 ORDER BY me.fecha_hora";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idChat);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<Mensaje> mensajes = new ArrayList<>();
                while (rs.next()) {
                    mensajes.add(mapear(rs, new Mensaje()));
                }
                return mensajes;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Mensaje mapear(ResultSet rs, Mensaje mensaje) throws SQLException {
        super.mapear(rs, mensaje);
        mensaje.setIdMensaje(rs.getInt("id_mensaje"));
        mensaje.setContenido(rs.getString("contenido"));
        mensaje.setFechaHora(leerFechaHora(rs, "fecha_hora"));
        mensaje.setLeido(rs.getBoolean("leido"));
        CanalChat chat = new CanalChat();
        chat.setIdChat(rs.getInt("id_chat"));
        chat.setEstado(EstadoCanalChat.valueOf(rs.getString("chat_estado")));
        mensaje.setCanalChat(chat);
        mensaje.setEmisor(mapearUsuarioReferencia(rs, "emisor"));
        return mensaje;
    }
}
