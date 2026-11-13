package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.RespuestaRapidaDAO;
import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;

public class RespuestaRapidaDAOImpl extends RegistroDAOImpl<RespuestaRapida> implements RespuestaRapidaDAO {

    @Override
    public int insert(RespuestaRapida respuesta) throws SQLException {
        if (respuesta == null) {
            throw new IllegalArgumentException("La respuesta rapida no puede ser nula");
        }
        String sql = "{call insertar_respuesta_rapida(?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_texto", respuesta.getTexto());
            cmd.setInt("p_id_creador", respuesta.getCreador().getIdUsuario());
            cmd.setBoolean("p_activo", respuesta.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(respuesta.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            respuesta.setIdRespuesta(cmd.getInt("p_id"));
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
        String sql = "{call modificar_respuesta_rapida(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", respuesta.getIdRespuesta());
            cmd.setString("p_texto", respuesta.getTexto());
            cmd.setInt("p_id_creador", respuesta.getCreador().getIdUsuario());
            cmd.setBoolean("p_activo", respuesta.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(respuesta.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idRespuesta) throws SQLException {
        String sql = "{call eliminar_respuesta_rapida(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idRespuesta);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public RespuestaRapida findById(int idRespuesta) throws SQLException {
        String sql = "{call buscar_respuesta_rapida_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idRespuesta);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new RespuestaRapida()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<RespuestaRapida> findAll() throws SQLException {
        String sql = "{call listar_respuestas_rapidas()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call listar_respuestas_rapidas_por_creador(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_creador", idCreador);
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
