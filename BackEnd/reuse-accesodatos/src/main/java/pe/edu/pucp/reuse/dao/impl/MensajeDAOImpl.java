package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.MensajeDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.Mensaje;

public class MensajeDAOImpl extends RegistroDAOImpl<Mensaje> implements MensajeDAO {

    // fecha_hora no se envia: la asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).
    @Override
    public int insert(Mensaje mensaje) throws SQLException {
        if (mensaje == null) {
            throw new IllegalArgumentException("El mensaje no puede ser nulo");
        }
        String sql = "{call insertar_mensaje(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_contenido", mensaje.getContenido());
            cmd.setBoolean("p_leido", mensaje.isLeido());
            cmd.setInt("p_id_chat", mensaje.getCanalChat().getIdChat());
            cmd.setInt("p_id_emisor", mensaje.getEmisor().getIdUsuario());
            cmd.setBoolean("p_activo", mensaje.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(mensaje.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            mensaje.setIdMensaje(cmd.getInt("p_id"));
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
        String sql = "{call modificar_mensaje(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", mensaje.getIdMensaje());
            cmd.setString("p_contenido", mensaje.getContenido());
            cmd.setBoolean("p_leido", mensaje.isLeido());
            cmd.setInt("p_id_chat", mensaje.getCanalChat().getIdChat());
            cmd.setInt("p_id_emisor", mensaje.getEmisor().getIdUsuario());
            cmd.setBoolean("p_activo", mensaje.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(mensaje.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idMensaje) throws SQLException {
        String sql = "{call eliminar_mensaje(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idMensaje);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Mensaje findById(int idMensaje) throws SQLException {
        String sql = "{call buscar_mensaje_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idMensaje);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Mensaje()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Mensaje> findAll() throws SQLException {
        String sql = "{call listar_mensajes()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call listar_mensajes_por_chat(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_chat", idChat);
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
