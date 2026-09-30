package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CanalChatDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;

public class CanalChatDAOImpl extends RegistroDAOImpl<CanalChat> implements CanalChatDAO {

    @Override
    public int insert(CanalChat chat) throws SQLException {
        if (chat == null) {
            throw new IllegalArgumentException("El chat no puede ser nulo");
        }
        String sql = "{call insertar_canal_chat(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_estado", chat.getEstado().name());
            cmd.setInt("p_id_anuncio", chat.getAnuncio().getIdAnuncio());
            cmd.setInt("p_id_comprador", chat.getComprador().getIdUsuario());
            cmd.setBoolean("p_activo", chat.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(chat.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            chat.setIdChat(cmd.getInt("p_id"));
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
        String sql = "{call modificar_canal_chat(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", chat.getIdChat());
            cmd.setString("p_estado", chat.getEstado().name());
            cmd.setInt("p_id_anuncio", chat.getAnuncio().getIdAnuncio());
            cmd.setInt("p_id_comprador", chat.getComprador().getIdUsuario());
            cmd.setBoolean("p_activo", chat.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(chat.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idChat) throws SQLException {
        String sql = "{call eliminar_canal_chat(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idChat);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CanalChat findById(int idChat) throws SQLException {
        String sql = "{call buscar_canal_chat_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idChat);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CanalChat()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<CanalChat> findAll() throws SQLException {
        String sql = "{call listar_canales_chat()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call responder_solicitud_chat(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idChat);
            cmd.setString("p_estado", nuevoEstado.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int cerrar(int idChat, EstadoCanalChat estadoCierre) throws SQLException {
        if (estadoCierre != EstadoCanalChat.CERRADO && estadoCierre != EstadoCanalChat.BLOQUEADO) {
            throw new IllegalArgumentException("Un chat solo se cierra como CERRADO o BLOQUEADO");
        }
        String sql = "{call cerrar_canal_chat(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idChat);
            cmd.setString("p_estado", estadoCierre.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int bloquearChatsEntre(int idUsuarioA, int idUsuarioB) throws SQLException {
        String sql = "{call bloquear_chats_entre(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario_a", idUsuarioA);
            cmd.setInt("p_id_usuario_b", idUsuarioB);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CanalChat obtenerAbierto(int idAnuncio, int idComprador) throws SQLException {
        String sql = "{call buscar_chat_abierto(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_anuncio", idAnuncio);
            cmd.setInt("p_id_comprador", idComprador);
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
