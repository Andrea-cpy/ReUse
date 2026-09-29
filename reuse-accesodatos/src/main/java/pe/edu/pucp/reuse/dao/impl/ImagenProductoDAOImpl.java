package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.ImagenProductoDAO;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;

public class ImagenProductoDAOImpl extends RegistroDAOImpl<ImagenProducto> implements ImagenProductoDAO {

    @Override
    public int insert(ImagenProducto imagen) throws SQLException {
        if (imagen == null) {
            throw new IllegalArgumentException("La imagen no puede ser nula");
        }
        String sql = "{call insertar_imagen_producto(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_url", imagen.getUrl());
            cmd.setLong("p_peso_bytes", imagen.getPesoBytes());
            cmd.setString("p_formato", imagen.getFormato());
            cmd.setInt("p_id_anuncio", imagen.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", imagen.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(imagen.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            imagen.setIdImagen(cmd.getInt("p_id"));
            return imagen.getIdImagen();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(ImagenProducto imagen) throws SQLException {
        if (imagen == null) {
            throw new IllegalArgumentException("La imagen no puede ser nula");
        }
        String sql = "{call modificar_imagen_producto(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", imagen.getIdImagen());
            cmd.setString("p_url", imagen.getUrl());
            cmd.setLong("p_peso_bytes", imagen.getPesoBytes());
            cmd.setString("p_formato", imagen.getFormato());
            cmd.setInt("p_id_anuncio", imagen.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", imagen.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(imagen.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idImagen) throws SQLException {
        String sql = "{call eliminar_imagen_producto(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idImagen);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ImagenProducto findById(int idImagen) throws SQLException {
        String sql = "{call buscar_imagen_producto_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idImagen);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ImagenProducto()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ImagenProducto> findAll() throws SQLException {
        String sql = "{call listar_imagenes_producto()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<ImagenProducto> imagenes = new ArrayList<>();
            while (rs.next()) {
                imagenes.add(mapear(rs, new ImagenProducto()));
            }
            return imagenes;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ImagenProducto> listarPorAnuncio(int idAnuncio) throws SQLException {
        String sql = "{call listar_imagenes_por_anuncio(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_anuncio", idAnuncio);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<ImagenProducto> imagenes = new ArrayList<>();
                while (rs.next()) {
                    imagenes.add(mapear(rs, new ImagenProducto()));
                }
                return imagenes;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int eliminarPorAnuncio(int idAnuncio) throws SQLException {
        String sql = "{call eliminar_imagenes_por_anuncio(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_anuncio", idAnuncio);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected ImagenProducto mapear(ResultSet rs, ImagenProducto imagen) throws SQLException {
        super.mapear(rs, imagen);
        imagen.setIdImagen(rs.getInt("id_imagen"));
        imagen.setUrl(rs.getString("url"));
        imagen.setPesoBytes(rs.getLong("peso_bytes"));
        imagen.setFormato(rs.getString("formato"));
        imagen.setAnuncio(mapearAnuncioReferencia(rs));
        return imagen;
    }
}
