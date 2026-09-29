package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.InsigniaUsuarioDAO;
import pe.edu.pucp.reuse.modelo.enums.TipoInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;

public class InsigniaUsuarioDAOImpl extends RegistroDAOImpl<InsigniaUsuario> implements InsigniaUsuarioDAO {

    // fecha_obtencion no se envia: la asigna la base de datos.
    @Override
    public int insert(InsigniaUsuario insigniaUsuario) throws SQLException {
        if (insigniaUsuario == null) {
            throw new IllegalArgumentException("La insignia del usuario no puede ser nula");
        }
        String sql = "{call insertar_insignia_usuario(?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", insigniaUsuario.getUsuario().getIdUsuario());
            cmd.setInt("p_id_insignia", insigniaUsuario.getInsignia().getIdInsignia());
            cmd.setBoolean("p_activo", insigniaUsuario.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(insigniaUsuario.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            insigniaUsuario.setIdInsigniaUsuario(cmd.getInt("p_id"));
            return insigniaUsuario.getIdInsigniaUsuario();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(InsigniaUsuario insigniaUsuario) throws SQLException {
        if (insigniaUsuario == null) {
            throw new IllegalArgumentException("La insignia del usuario no puede ser nula");
        }
        String sql = "{call modificar_insignia_usuario(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", insigniaUsuario.getIdInsigniaUsuario());
            cmd.setInt("p_id_usuario", insigniaUsuario.getUsuario().getIdUsuario());
            cmd.setInt("p_id_insignia", insigniaUsuario.getInsignia().getIdInsignia());
            cmd.setBoolean("p_activo", insigniaUsuario.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(insigniaUsuario.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idInsigniaUsuario) throws SQLException {
        String sql = "{call eliminar_insignia_usuario(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idInsigniaUsuario);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public InsigniaUsuario findById(int idInsigniaUsuario) throws SQLException {
        String sql = "{call buscar_insignia_usuario_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idInsigniaUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new InsigniaUsuario()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<InsigniaUsuario> findAll() throws SQLException {
        String sql = "{call listar_insignias_usuario()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<InsigniaUsuario> insignias = new ArrayList<>();
            while (rs.next()) {
                insignias.add(mapear(rs, new InsigniaUsuario()));
            }
            return insignias;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public InsigniaUsuario obtenerPorUsuarioEInsignia(int idUsuario, int idInsignia) throws SQLException {
        String sql = "{call buscar_insignia_usuario_por_usuario_e_insignia(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", idUsuario);
            cmd.setInt("p_id_insignia", idInsignia);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new InsigniaUsuario()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<InsigniaUsuario> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = "{call listar_insignias_por_usuario(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", idUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<InsigniaUsuario> insignias = new ArrayList<>();
                while (rs.next()) {
                    insignias.add(mapear(rs, new InsigniaUsuario()));
                }
                return insignias;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected InsigniaUsuario mapear(ResultSet rs, InsigniaUsuario insigniaUsuario) throws SQLException {
        super.mapear(rs, insigniaUsuario);
        insigniaUsuario.setIdInsigniaUsuario(rs.getInt("id_insignia_usuario"));
        insigniaUsuario.setFechaObtencion(leerFechaHora(rs, "fecha_obtencion"));
        insigniaUsuario.setUsuario(mapearUsuarioReferencia(rs, "usuario"));
        Insignia insignia = new Insignia();
        insignia.setIdInsignia(rs.getInt("id_insignia"));
        insignia.setNombre(rs.getString("insignia_nombre"));
        insignia.setTipo(TipoInsignia.valueOf(rs.getString("insignia_tipo")));
        insigniaUsuario.setInsignia(insignia);
        return insigniaUsuario;
    }
}
