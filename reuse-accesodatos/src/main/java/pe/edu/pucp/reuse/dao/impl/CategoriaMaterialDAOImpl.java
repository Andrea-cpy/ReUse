package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CategoriaMaterialDAO;
import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;

public class CategoriaMaterialDAOImpl extends RegistroDAOImpl<CategoriaMaterial> implements CategoriaMaterialDAO {

    @Override
    public int insert(CategoriaMaterial categoria) throws SQLException {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria no puede ser nula");
        }
        String sql = "{call insertar_categoria_material(?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", categoria.getNombre());
            cmd.setString("p_descripcion", categoria.getDescripcion());
            cmd.setBoolean("p_activo", categoria.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(categoria.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            categoria.setIdCategoria(cmd.getInt("p_id"));
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
        String sql = "{call modificar_categoria_material(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", categoria.getIdCategoria());
            cmd.setString("p_nombre", categoria.getNombre());
            cmd.setString("p_descripcion", categoria.getDescripcion());
            cmd.setBoolean("p_activo", categoria.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(categoria.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCategoria) throws SQLException {
        String sql = "{call eliminar_categoria_material(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCategoria);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CategoriaMaterial findById(int idCategoria) throws SQLException {
        String sql = "{call buscar_categoria_material_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCategoria);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CategoriaMaterial()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<CategoriaMaterial> findAll() throws SQLException {
        String sql = "{call listar_categorias_material()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call buscar_categoria_material_por_nombre(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", nombre);
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
