package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.BloqueoDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoBloqueo;
import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;

public class BloqueoDAOImpl extends RegistroDAOImpl<Bloqueo> implements BloqueoDAO {

    // La fecha del bloqueo no se envia: la asigna la base de datos.
    @Override
    public int insert(Bloqueo bloqueo) throws SQLException {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo no puede ser nulo");
        }
        String sql = "{call insertar_bloqueo(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_estado", bloqueo.getEstado().name());
            cmd.setInt("p_id_bloqueador", bloqueo.getBloqueador().getIdUsuario());
            cmd.setInt("p_id_bloqueado", bloqueo.getBloqueado().getIdUsuario());
            cmd.setBoolean("p_activo", bloqueo.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(bloqueo.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            bloqueo.setIdBloqueo(cmd.getInt("p_id"));
            return bloqueo.getIdBloqueo();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Bloqueo bloqueo) throws SQLException {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo no puede ser nulo");
        }
        String sql = "{call modificar_bloqueo(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", bloqueo.getIdBloqueo());
            cmd.setString("p_estado", bloqueo.getEstado().name());
            cmd.setInt("p_id_bloqueador", bloqueo.getBloqueador().getIdUsuario());
            cmd.setInt("p_id_bloqueado", bloqueo.getBloqueado().getIdUsuario());
            cmd.setBoolean("p_activo", bloqueo.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(bloqueo.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idBloqueo) throws SQLException {
        String sql = "{call eliminar_bloqueo(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idBloqueo);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Bloqueo findById(int idBloqueo) throws SQLException {
        String sql = "{call buscar_bloqueo_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idBloqueo);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Bloqueo()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Bloqueo> findAll() throws SQLException {
        String sql = "{call listar_bloqueos()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Bloqueo> bloqueos = new ArrayList<>();
            while (rs.next()) {
                bloqueos.add(mapear(rs, new Bloqueo()));
            }
            return bloqueos;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Bloqueo obtenerActivoEntre(int idUsuarioA, int idUsuarioB) throws SQLException {
        String sql = "{call buscar_bloqueo_activo_entre(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario_a", idUsuarioA);
            cmd.setInt("p_id_usuario_b", idUsuarioB);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Bloqueo()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Bloqueo mapear(ResultSet rs, Bloqueo bloqueo) throws SQLException {
        super.mapear(rs, bloqueo);
        bloqueo.setIdBloqueo(rs.getInt("id_bloqueo"));
        bloqueo.setFecha(leerFechaHora(rs, "fecha"));
        bloqueo.setEstado(EstadoBloqueo.valueOf(rs.getString("estado")));
        bloqueo.setBloqueador(mapearUsuarioReferencia(rs, "bloqueador"));
        bloqueo.setBloqueado(mapearUsuarioReferencia(rs, "bloqueado"));
        return bloqueo;
    }
}
