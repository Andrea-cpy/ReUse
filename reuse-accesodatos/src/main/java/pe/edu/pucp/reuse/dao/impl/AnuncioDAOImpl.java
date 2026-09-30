package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.AnuncioDAO;
import pe.edu.pucp.reuse.dao.ImagenProductoDAO;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;

public class AnuncioDAOImpl extends RegistroDAOImpl<Anuncio> implements AnuncioDAO {

    @Override
    public int insert(Anuncio anuncio) throws SQLException {
        if (anuncio == null) {
            throw new IllegalArgumentException("El anuncio no puede ser nulo");
        }
        String sql = "{call insertar_anuncio(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_titulo", anuncio.getTitulo());
            cmd.setDouble("p_precio", anuncio.getPrecio());
            cmd.setString("p_descripcion", anuncio.getDescripcion());
            cmd.setString("p_condicion", anuncio.getCondicion().name());
            cmd.setString("p_estado", anuncio.getEstado().name());
            cmd.setInt("p_id_vendedor", anuncio.getVendedor().getIdUsuario());
            cmd.setInt("p_id_material", anuncio.getMaterialAcademico().getIdMaterial());
            cmd.setBoolean("p_activo", anuncio.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(anuncio.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            anuncio.setIdAnuncio(cmd.getInt("p_id"));
            return anuncio.getIdAnuncio();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Anuncio anuncio) throws SQLException {
        if (anuncio == null) {
            throw new IllegalArgumentException("El anuncio no puede ser nulo");
        }
        String sql = "{call modificar_anuncio(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", anuncio.getIdAnuncio());
            cmd.setString("p_titulo", anuncio.getTitulo());
            cmd.setDouble("p_precio", anuncio.getPrecio());
            cmd.setString("p_descripcion", anuncio.getDescripcion());
            cmd.setString("p_condicion", anuncio.getCondicion().name());
            cmd.setString("p_estado", anuncio.getEstado().name());
            cmd.setInt("p_id_material", anuncio.getMaterialAcademico().getIdMaterial());
            cmd.setBoolean("p_activo", anuncio.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(anuncio.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idAnuncio) throws SQLException {
        String sql = "{call eliminar_anuncio(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idAnuncio);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Anuncio findById(int idAnuncio) throws SQLException {
        String sql = "{call buscar_anuncio_por_id(?)}";
        Anuncio anuncio;
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idAnuncio);
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

    @Override
    public ArrayList<Anuncio> findAll() throws SQLException {
        String sql = "{call listar_anuncios()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call cambiar_estado_anuncio(?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idAnuncio);
            cmd.setString("p_estado_actual", estadoActual.name());
            cmd.setString("p_estado_nuevo", estadoNuevo.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int contarTransacciones(int idAnuncio) throws SQLException {
        String sql = "{call contar_transacciones_anuncio(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idAnuncio);
            cmd.registerOutParameter("p_total", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_total");
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
