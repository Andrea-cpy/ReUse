package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CanalChatDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;

public class CanalChatDAOImpl extends RegistroDAOImpl<CanalChat> implements CanalChatDAO {

    private static final String SELECT_BASE = """
            SELECT ch.id_chat, ch.estado, ch.fecha_solicitud, ch.fecha_respuesta, ch.fecha_cierre,
                   a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
                   a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
                   u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
                   u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
                   ch.activo, ch.fecha_creacion, ch.fecha_modificacion, ch.usuario_creacion, ch.usuario_modificacion
            FROM canal_chat ch
            JOIN anuncio a ON a.id_anuncio = ch.id_anuncio
            JOIN usuario u ON u.id_usuario = ch.id_comprador
            """;

    // fecha_solicitud no se envia: la asigna la base de datos.
    @Override
    public int insert(CanalChat chat) throws SQLException {
        if (chat == null) {
            throw new IllegalArgumentException("El chat no puede ser nulo");
        }
        String sql = """
                INSERT INTO canal_chat (estado, id_anuncio, id_comprador, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, chat.getEstado().name());
            cmd.setInt(2, chat.getAnuncio().getIdAnuncio());
            cmd.setInt(3, chat.getComprador().getIdUsuario());
            cmd.setBoolean(4, chat.isActivo());
            cmd.setString(5, usuarioAuditoria(chat.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el chat");
            }
            chat.setIdChat(leerIdGenerado(cmd));
            return chat.getIdChat();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(CanalChat chat) throws SQLException {
        if (chat == null) {
            throw new IllegalArgumentException("El chat no puede ser nulo");
        }
        String sql = """
                UPDATE canal_chat
                SET estado = ?, id_anuncio = ?, id_comprador = ?, activo = ?, usuario_modificacion = ?
                WHERE id_chat = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, chat.getEstado().name());
            cmd.setInt(2, chat.getAnuncio().getIdAnuncio());
            cmd.setInt(3, chat.getComprador().getIdUsuario());
            cmd.setBoolean(4, chat.isActivo());
            cmd.setString(5, usuarioAuditoria(chat.getUsuarioModificacion()));
            cmd.setInt(6, chat.getIdChat());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idChat) throws SQLException {
        String sql = """
                UPDATE canal_chat SET activo = 0 WHERE id_chat = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idChat);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CanalChat findById(int idChat) throws SQLException {
        String sql = SELECT_BASE + "WHERE ch.id_chat = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idChat);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CanalChat()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<CanalChat> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE ch.activo = 1 ORDER BY ch.fecha_solicitud DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<CanalChat> chats = new ArrayList<>();
            while (rs.next()) {
                chats.add(mapear(rs, new CanalChat()));
            }
            return chats;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int responderSolicitud(int idChat, EstadoCanalChat nuevoEstado) throws SQLException {
        if (nuevoEstado != EstadoCanalChat.ACTIVO && nuevoEstado != EstadoCanalChat.RECHAZADO) {
            throw new IllegalArgumentException("Una solicitud solo se acepta (ACTIVO) o se rechaza (RECHAZADO)");
        }
        String sql = nuevoEstado == EstadoCanalChat.RECHAZADO
                ? """
                  UPDATE canal_chat
                  SET estado = ?, fecha_respuesta = CURRENT_TIMESTAMP, fecha_cierre = CURRENT_TIMESTAMP
                  WHERE id_chat = ?
                  """
                : """
                  UPDATE canal_chat
                  SET estado = ?, fecha_respuesta = CURRENT_TIMESTAMP
                  WHERE id_chat = ?
                  """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, nuevoEstado.name());
            cmd.setInt(2, idChat);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int cerrar(int idChat, EstadoCanalChat estadoCierre) throws SQLException {
        if (estadoCierre != EstadoCanalChat.CERRADO && estadoCierre != EstadoCanalChat.BLOQUEADO) {
            throw new IllegalArgumentException("Un chat solo se cierra como CERRADO o BLOQUEADO");
        }
        String sql = """
                UPDATE canal_chat SET estado = ?, fecha_cierre = CURRENT_TIMESTAMP WHERE id_chat = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estadoCierre.name());
            cmd.setInt(2, idChat);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int bloquearChatsEntre(int idUsuarioA, int idUsuarioB) throws SQLException {
        String sql = """
                UPDATE canal_chat ch
                JOIN anuncio a ON a.id_anuncio = ch.id_anuncio
                SET ch.estado = 'BLOQUEADO', ch.fecha_cierre = CURRENT_TIMESTAMP
                WHERE ch.estado IN ('PENDIENTE', 'ACTIVO') AND ch.activo = 1
                  AND ((ch.id_comprador = ? AND a.id_vendedor = ?)
                    OR (ch.id_comprador = ? AND a.id_vendedor = ?))
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuarioA);
            cmd.setInt(2, idUsuarioB);
            cmd.setInt(3, idUsuarioB);
            cmd.setInt(4, idUsuarioA);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CanalChat obtenerAbierto(int idAnuncio, int idComprador) throws SQLException {
        String sql = SELECT_BASE + """
                WHERE ch.id_anuncio = ? AND ch.id_comprador = ?
                  AND ch.estado IN ('PENDIENTE', 'ACTIVO') AND ch.activo = 1
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
            cmd.setInt(2, idComprador);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CanalChat()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected CanalChat mapear(ResultSet rs, CanalChat chat) throws SQLException {
        super.mapear(rs, chat);
        chat.setIdChat(rs.getInt("id_chat"));
        chat.setEstado(EstadoCanalChat.valueOf(rs.getString("estado")));
        chat.setFechaSolicitud(leerFechaHora(rs, "fecha_solicitud"));
        chat.setFechaRespuesta(leerFechaHora(rs, "fecha_respuesta"));
        chat.setFechaCierre(leerFechaHora(rs, "fecha_cierre"));
        chat.setAnuncio(mapearAnuncioReferencia(rs));
        chat.setComprador(mapearUsuarioReferencia(rs, "comprador"));
        return chat;
    }
}
