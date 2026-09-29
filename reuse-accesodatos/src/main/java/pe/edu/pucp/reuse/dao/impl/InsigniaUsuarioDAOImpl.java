package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.InsigniaUsuarioDAO;
import pe.edu.pucp.reuse.modelo.enums.TipoInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;

public class InsigniaUsuarioDAOImpl extends RegistroDAOImpl<InsigniaUsuario> implements InsigniaUsuarioDAO {

    private static final String SELECT_BASE = """
            SELECT iu.id_insignia_usuario, iu.fecha_obtencion,
                   u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
                   u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
                   iu.id_insignia, i.nombre AS insignia_nombre, i.tipo AS insignia_tipo,
                   iu.activo, iu.fecha_creacion, iu.fecha_modificacion, iu.usuario_creacion, iu.usuario_modificacion
            FROM insignia_usuario iu
            JOIN usuario u ON u.id_usuario = iu.id_usuario
            JOIN insignia i ON i.id_insignia = iu.id_insignia
            """;

    // fecha_obtencion no se envia: la asigna la base de datos.
    @Override
    public int insert(InsigniaUsuario insigniaUsuario) throws SQLException {
        if (insigniaUsuario == null) {
            throw new IllegalArgumentException("La insignia del usuario no puede ser nula");
        }
        String sql = """
                INSERT INTO insignia_usuario (id_usuario, id_insignia, activo, usuario_creacion)
                VALUES (?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setInt(1, insigniaUsuario.getUsuario().getIdUsuario());
            cmd.setInt(2, insigniaUsuario.getInsignia().getIdInsignia());
            cmd.setBoolean(3, insigniaUsuario.isActivo());
            cmd.setString(4, usuarioAuditoria(insigniaUsuario.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo otorgar la insignia");
            }
            insigniaUsuario.setIdInsigniaUsuario(leerIdGenerado(cmd));
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
        String sql = """
                UPDATE insignia_usuario
                SET id_usuario = ?, id_insignia = ?, activo = ?, usuario_modificacion = ?
                WHERE id_insignia_usuario = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, insigniaUsuario.getUsuario().getIdUsuario());
            cmd.setInt(2, insigniaUsuario.getInsignia().getIdInsignia());
            cmd.setBoolean(3, insigniaUsuario.isActivo());
            cmd.setString(4, usuarioAuditoria(insigniaUsuario.getUsuarioModificacion()));
            cmd.setInt(5, insigniaUsuario.getIdInsigniaUsuario());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idInsigniaUsuario) throws SQLException {
        String sql = """
                UPDATE insignia_usuario SET activo = 0 WHERE id_insignia_usuario = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idInsigniaUsuario);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public InsigniaUsuario findById(int idInsigniaUsuario) throws SQLException {
        String sql = SELECT_BASE + "WHERE iu.id_insignia_usuario = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idInsigniaUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new InsigniaUsuario()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<InsigniaUsuario> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE iu.activo = 1 ORDER BY iu.fecha_obtencion DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
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
        String sql = SELECT_BASE + "WHERE iu.id_usuario = ? AND iu.id_insignia = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            cmd.setInt(2, idInsignia);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new InsigniaUsuario()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<InsigniaUsuario> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = SELECT_BASE + "WHERE iu.id_usuario = ? AND iu.activo = 1 ORDER BY iu.fecha_obtencion";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
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
