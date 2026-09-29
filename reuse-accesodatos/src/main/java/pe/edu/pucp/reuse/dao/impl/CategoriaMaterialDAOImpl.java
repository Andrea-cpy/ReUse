package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CategoriaMaterialDAO;
import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;

public class CategoriaMaterialDAOImpl extends RegistroDAOImpl<CategoriaMaterial> implements CategoriaMaterialDAO {

    private static final String SELECT_BASE = """
            SELECT id_categoria, nombre, descripcion,
                   activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
            FROM categoria_material
            """;

    @Override
    public int insert(CategoriaMaterial categoria) throws SQLException {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria no puede ser nula");
        }
        String sql = """
                INSERT INTO categoria_material (nombre, descripcion, activo, usuario_creacion)
                VALUES (?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, categoria.getNombre());
            cmd.setString(2, categoria.getDescripcion());
            cmd.setBoolean(3, categoria.isActivo());
            cmd.setString(4, usuarioAuditoria(categoria.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la categoria");
            }
            categoria.setIdCategoria(leerIdGenerado(cmd));
            return categoria.getIdCategoria();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(CategoriaMaterial categoria) throws SQLException {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria no puede ser nula");
        }
        String sql = """
                UPDATE categoria_material
                SET nombre = ?, descripcion = ?, activo = ?, usuario_modificacion = ?
                WHERE id_categoria = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, categoria.getNombre());
            cmd.setString(2, categoria.getDescripcion());
            cmd.setBoolean(3, categoria.isActivo());
            cmd.setString(4, usuarioAuditoria(categoria.getUsuarioModificacion()));
            cmd.setInt(5, categoria.getIdCategoria());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCategoria) throws SQLException {
        String sql = """
                UPDATE categoria_material SET activo = 0 WHERE id_categoria = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCategoria);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CategoriaMaterial findById(int idCategoria) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_categoria = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCategoria);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CategoriaMaterial()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<CategoriaMaterial> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE activo = 1 ORDER BY nombre";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<CategoriaMaterial> categorias = new ArrayList<>();
            while (rs.next()) {
                categorias.add(mapear(rs, new CategoriaMaterial()));
            }
            return categorias;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CategoriaMaterial obtenerPorNombre(String nombre) throws SQLException {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        String sql = SELECT_BASE + "WHERE nombre = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, nombre);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CategoriaMaterial()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected CategoriaMaterial mapear(ResultSet rs, CategoriaMaterial categoria) throws SQLException {
        super.mapear(rs, categoria);
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        return categoria;
    }
}
