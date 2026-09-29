package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.AnuncioDAO;
import pe.edu.pucp.reuse.dao.ImagenProductoDAO;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;

public class AnuncioDAOImpl extends RegistroDAOImpl<Anuncio> implements AnuncioDAO {

    private static final String SELECT_BASE = """
            SELECT a.id_anuncio, a.titulo, a.precio, a.descripcion, a.condicion, a.estado, a.fecha_publicacion,
                   v.id_usuario AS vendedor_id, v.codigo_pucp AS vendedor_codigo,
                   v.nombres AS vendedor_nombres, v.apellido_paterno AS vendedor_apellido,
                   a.id_material, m.titulo AS material_titulo,
                   a.activo, a.fecha_creacion, a.fecha_modificacion, a.usuario_creacion, a.usuario_modificacion
            FROM anuncio a
            JOIN usuario v ON v.id_usuario = a.id_vendedor
            JOIN material_academico m ON m.id_material = a.id_material
            """;

    // fecha_publicacion no se envia: la asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).
    @Override
    public int insert(Anuncio anuncio) throws SQLException {
        if (anuncio == null) {
            throw new IllegalArgumentException("El anuncio no puede ser nulo");
        }
        String sql = """
                INSERT INTO anuncio (titulo, precio, descripcion, condicion, estado, id_vendedor,
                                     id_material, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, anuncio.getTitulo());
            cmd.setDouble(2, anuncio.getPrecio());
            cmd.setString(3, anuncio.getDescripcion());
            cmd.setString(4, anuncio.getCondicion().name());
            cmd.setString(5, anuncio.getEstado().name());
            cmd.setInt(6, anuncio.getVendedor().getIdUsuario());
            cmd.setInt(7, anuncio.getMaterialAcademico().getIdMaterial());
            cmd.setBoolean(8, anuncio.isActivo());
            cmd.setString(9, usuarioAuditoria(anuncio.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el anuncio");
            }
            anuncio.setIdAnuncio(leerIdGenerado(cmd));
            return anuncio.getIdAnuncio();
        } finally {
            cerrarConexion(conn);
        }
    }

    // El vendedor de un anuncio no cambia, por eso id_vendedor no se actualiza.
    @Override
    public int update(Anuncio anuncio) throws SQLException {
        if (anuncio == null) {
            throw new IllegalArgumentException("El anuncio no puede ser nulo");
        }
        String sql = """
                UPDATE anuncio
                SET titulo = ?, precio = ?, descripcion = ?, condicion = ?, estado = ?, id_material = ?,
                    activo = ?, usuario_modificacion = ?
                WHERE id_anuncio = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, anuncio.getTitulo());
            cmd.setDouble(2, anuncio.getPrecio());
            cmd.setString(3, anuncio.getDescripcion());
            cmd.setString(4, anuncio.getCondicion().name());
            cmd.setString(5, anuncio.getEstado().name());
            cmd.setInt(6, anuncio.getMaterialAcademico().getIdMaterial());
            cmd.setBoolean(7, anuncio.isActivo());
            cmd.setString(8, usuarioAuditoria(anuncio.getUsuarioModificacion()));
            cmd.setInt(9, anuncio.getIdAnuncio());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idAnuncio) throws SQLException {
        String sql = """
                UPDATE anuncio SET activo = 0 WHERE id_anuncio = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    // Incluye el detalle: las imagenes activas del anuncio.
    @Override
    public Anuncio findById(int idAnuncio) throws SQLException {
        String sql = SELECT_BASE + "WHERE a.id_anuncio = ?";
        Anuncio anuncio;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
            try (ResultSet rs = cmd.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                anuncio = mapear(rs, new Anuncio());
            }
        } finally {
            cerrarConexion(conn);
        }
        ImagenProductoDAO imagenProductoDAO = new ImagenProductoDAOImpl();
        for (ImagenProducto imagen : imagenProductoDAO.listarPorAnuncio(idAnuncio)) {
            anuncio.agregarImagen(imagen);
        }
        return anuncio;
    }

    // Solo cabeceras (sin imagenes) para que el listado sea una sola consulta.
    @Override
    public ArrayList<Anuncio> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE a.activo = 1 ORDER BY a.fecha_publicacion DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Anuncio> anuncios = new ArrayList<>();
            while (rs.next()) {
                anuncios.add(mapear(rs, new Anuncio()));
            }
            return anuncios;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int cambiarEstado(int idAnuncio, EstadoAnuncio estadoActual, EstadoAnuncio estadoNuevo)
            throws SQLException {
        if (estadoActual == null || estadoNuevo == null) {
            throw new IllegalArgumentException("Los estados no pueden ser nulos");
        }
        String sql = """
                UPDATE anuncio SET estado = ? WHERE id_anuncio = ? AND estado = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estadoNuevo.name());
            cmd.setInt(2, idAnuncio);
            cmd.setString(3, estadoActual.name());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int contarTransacciones(int idAnuncio) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM transaccion WHERE id_anuncio = ? AND activo = 1
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Anuncio mapear(ResultSet rs, Anuncio anuncio) throws SQLException {
        super.mapear(rs, anuncio);
        anuncio.setIdAnuncio(rs.getInt("id_anuncio"));
        anuncio.setTitulo(rs.getString("titulo"));
        anuncio.setPrecio(rs.getDouble("precio"));
        anuncio.setDescripcion(rs.getString("descripcion"));
        anuncio.setCondicion(CondicionMaterial.valueOf(rs.getString("condicion")));
        anuncio.setEstado(EstadoAnuncio.valueOf(rs.getString("estado")));
        anuncio.setFechaPublicacion(leerFechaHora(rs, "fecha_publicacion"));
        anuncio.setVendedor(mapearUsuarioReferencia(rs, "vendedor"));
        MaterialAcademico material = new MaterialAcademico();
        material.setIdMaterial(rs.getInt("id_material"));
        material.setTitulo(rs.getString("material_titulo"));
        anuncio.setMaterialAcademico(material);
        return anuncio;
    }
}
