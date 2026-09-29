package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.RespuestaRapidaDAO;
import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;

public class RespuestaRapidaDAOImpl extends RegistroDAOImpl<RespuestaRapida> implements RespuestaRapidaDAO {

    private static final String SELECT_BASE = """
            SELECT rr.id_respuesta, rr.texto,
                   u.id_usuario AS creador_id, u.codigo_pucp AS creador_codigo,
                   u.nombres AS creador_nombres, u.apellido_paterno AS creador_apellido,
                   rr.activo, rr.fecha_creacion, rr.fecha_modificacion, rr.usuario_creacion, rr.usuario_modificacion
            FROM respuesta_rapida rr
            JOIN usuario u ON u.id_usuario = rr.id_creador
            """;

    @Override
    public int insert(RespuestaRapida respuesta) throws SQLException {
        if (respuesta == null) {
            throw new IllegalArgumentException("La respuesta rapida no puede ser nula");
        }
        String sql = """
                INSERT INTO respuesta_rapida (texto, id_creador, activo, usuario_creacion)
                VALUES (?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, respuesta.getTexto());
            cmd.setInt(2, respuesta.getCreador().getIdUsuario());
            cmd.setBoolean(3, respuesta.isActivo());
            cmd.setString(4, usuarioAuditoria(respuesta.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la respuesta rapida");
            }
            respuesta.setIdRespuesta(leerIdGenerado(cmd));
            return respuesta.getIdRespuesta();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(RespuestaRapida respuesta) throws SQLException {
        if (respuesta == null) {
            throw new IllegalArgumentException("La respuesta rapida no puede ser nula");
        }
        String sql = """
                UPDATE respuesta_rapida
                SET texto = ?, id_creador = ?, activo = ?, usuario_modificacion = ?
                WHERE id_respuesta = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, respuesta.getTexto());
            cmd.setInt(2, respuesta.getCreador().getIdUsuario());
            cmd.setBoolean(3, respuesta.isActivo());
            cmd.setString(4, usuarioAuditoria(respuesta.getUsuarioModificacion()));
            cmd.setInt(5, respuesta.getIdRespuesta());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idRespuesta) throws SQLException {
        String sql = """
                UPDATE respuesta_rapida SET activo = 0 WHERE id_respuesta = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idRespuesta);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public RespuestaRapida findById(int idRespuesta) throws SQLException {
        String sql = SELECT_BASE + "WHERE rr.id_respuesta = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idRespuesta);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new RespuestaRapida()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<RespuestaRapida> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE rr.activo = 1 ORDER BY rr.id_creador, rr.id_respuesta";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<RespuestaRapida> respuestas = new ArrayList<>();
            while (rs.next()) {
                respuestas.add(mapear(rs, new RespuestaRapida()));
            }
            return respuestas;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<RespuestaRapida> listarPorCreador(int idCreador) throws SQLException {
        String sql = SELECT_BASE + "WHERE rr.id_creador = ? AND rr.activo = 1 ORDER BY rr.id_respuesta";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCreador);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<RespuestaRapida> respuestas = new ArrayList<>();
                while (rs.next()) {
                    respuestas.add(mapear(rs, new RespuestaRapida()));
                }
                return respuestas;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected RespuestaRapida mapear(ResultSet rs, RespuestaRapida respuesta) throws SQLException {
        super.mapear(rs, respuesta);
        respuesta.setIdRespuesta(rs.getInt("id_respuesta"));
        respuesta.setTexto(rs.getString("texto"));
        respuesta.setCreador(mapearUsuarioReferencia(rs, "creador"));
        return respuesta;
    }
}
