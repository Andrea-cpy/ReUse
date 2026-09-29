package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.ImagenProductoDAO;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;

public class ImagenProductoDAOImpl extends RegistroDAOImpl<ImagenProducto> implements ImagenProductoDAO {

    private static final String SELECT_BASE = """
            SELECT i.id_imagen, i.url, i.peso_bytes, i.formato,
                   a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
                   a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
                   i.activo, i.fecha_creacion, i.fecha_modificacion, i.usuario_creacion, i.usuario_modificacion
            FROM imagen_producto i
            JOIN anuncio a ON a.id_anuncio = i.id_anuncio
            """;

    @Override
    public int insert(ImagenProducto imagen) throws SQLException {
        if (imagen == null) {
            throw new IllegalArgumentException("La imagen no puede ser nula");
        }
        String sql = """
                INSERT INTO imagen_producto (url, peso_bytes, formato, id_anuncio, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, imagen.getUrl());
            cmd.setLong(2, imagen.getPesoBytes());
            cmd.setString(3, imagen.getFormato());
            cmd.setInt(4, imagen.getAnuncio().getIdAnuncio());
            cmd.setBoolean(5, imagen.isActivo());
            cmd.setString(6, usuarioAuditoria(imagen.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la imagen");
            }
            imagen.setIdImagen(leerIdGenerado(cmd));
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
        String sql = """
                UPDATE imagen_producto
                SET url = ?, peso_bytes = ?, formato = ?, id_anuncio = ?, activo = ?, usuario_modificacion = ?
                WHERE id_imagen = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, imagen.getUrl());
            cmd.setLong(2, imagen.getPesoBytes());
            cmd.setString(3, imagen.getFormato());
            cmd.setInt(4, imagen.getAnuncio().getIdAnuncio());
            cmd.setBoolean(5, imagen.isActivo());
            cmd.setString(6, usuarioAuditoria(imagen.getUsuarioModificacion()));
            cmd.setInt(7, imagen.getIdImagen());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idImagen) throws SQLException {
        String sql = """
                UPDATE imagen_producto SET activo = 0 WHERE id_imagen = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idImagen);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ImagenProducto findById(int idImagen) throws SQLException {
        String sql = SELECT_BASE + "WHERE i.id_imagen = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idImagen);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ImagenProducto()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ImagenProducto> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE i.activo = 1 ORDER BY i.id_anuncio, i.id_imagen";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
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
        String sql = SELECT_BASE + "WHERE i.id_anuncio = ? AND i.activo = 1 ORDER BY i.id_imagen";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
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
        String sql = """
                UPDATE imagen_producto SET activo = 0 WHERE id_anuncio = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
            return cmd.executeUpdate();
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
