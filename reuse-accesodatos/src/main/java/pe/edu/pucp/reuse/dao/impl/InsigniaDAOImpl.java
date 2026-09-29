package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.InsigniaDAO;
import pe.edu.pucp.reuse.dao.ReglaInsigniaDAO;
import pe.edu.pucp.reuse.modelo.enums.TipoInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public class InsigniaDAOImpl extends RegistroDAOImpl<Insignia> implements InsigniaDAO {

    private static final String SELECT_BASE = """
            SELECT id_insignia, nombre, descripcion, icono, tipo,
                   activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
            FROM insignia
            """;

    @Override
    public int insert(Insignia insignia) throws SQLException {
        if (insignia == null) {
            throw new IllegalArgumentException("La insignia no puede ser nula");
        }
        String sql = """
                INSERT INTO insignia (nombre, descripcion, icono, tipo, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, insignia.getNombre());
            cmd.setString(2, insignia.getDescripcion());
            cmd.setString(3, insignia.getIcono());
            cmd.setString(4, insignia.getTipo().name());
            cmd.setBoolean(5, insignia.isActivo());
            cmd.setString(6, usuarioAuditoria(insignia.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la insignia");
            }
            insignia.setIdInsignia(leerIdGenerado(cmd));
            return insignia.getIdInsignia();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Insignia insignia) throws SQLException {
        if (insignia == null) {
            throw new IllegalArgumentException("La insignia no puede ser nula");
        }
        String sql = """
                UPDATE insignia
                SET nombre = ?, descripcion = ?, icono = ?, tipo = ?, activo = ?, usuario_modificacion = ?
                WHERE id_insignia = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, insignia.getNombre());
            cmd.setString(2, insignia.getDescripcion());
            cmd.setString(3, insignia.getIcono());
            cmd.setString(4, insignia.getTipo().name());
            cmd.setBoolean(5, insignia.isActivo());
            cmd.setString(6, usuarioAuditoria(insignia.getUsuarioModificacion()));
            cmd.setInt(7, insignia.getIdInsignia());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idInsignia) throws SQLException {
        String sql = """
                UPDATE insignia SET activo = 0 WHERE id_insignia = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idInsignia);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    // Incluye el detalle: las reglas activas de la insignia.
    @Override
    public Insignia findById(int idInsignia) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_insignia = ?";
        Insignia insignia;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idInsignia);
            try (ResultSet rs = cmd.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                insignia = mapear(rs, new Insignia());
            }
        } finally {
            cerrarConexion(conn);
        }
        ReglaInsigniaDAO reglaInsigniaDAO = new ReglaInsigniaDAOImpl();
        for (ReglaInsignia regla : reglaInsigniaDAO.listarPorInsignia(idInsignia)) {
            insignia.agregarRegla(regla);
        }
        return insignia;
    }

    @Override
    public ArrayList<Insignia> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE activo = 1 ORDER BY nombre";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Insignia> insignias = new ArrayList<>();
            while (rs.next()) {
                insignias.add(mapear(rs, new Insignia()));
            }
            return insignias;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Insignia obtenerPorNombre(String nombre) throws SQLException {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        String sql = SELECT_BASE + "WHERE nombre = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, nombre);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Insignia()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Insignia mapear(ResultSet rs, Insignia insignia) throws SQLException {
        super.mapear(rs, insignia);
        insignia.setIdInsignia(rs.getInt("id_insignia"));
        insignia.setNombre(rs.getString("nombre"));
        insignia.setDescripcion(rs.getString("descripcion"));
        insignia.setIcono(rs.getString("icono"));
        insignia.setTipo(TipoInsignia.valueOf(rs.getString("tipo")));
        return insignia;
    }
}
