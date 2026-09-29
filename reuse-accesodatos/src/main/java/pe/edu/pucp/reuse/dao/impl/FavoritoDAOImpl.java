package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.FavoritoDAO;
import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;

public class FavoritoDAOImpl extends RegistroDAOImpl<Favorito> implements FavoritoDAO {

    private static final String SELECT_BASE = """
            SELECT fa.id_favorito, fa.fecha_guardado,
                   u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
                   u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
                   a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
                   a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
                   fa.activo, fa.fecha_creacion, fa.fecha_modificacion, fa.usuario_creacion, fa.usuario_modificacion
            FROM favorito fa
            JOIN usuario u ON u.id_usuario = fa.id_usuario
            JOIN anuncio a ON a.id_anuncio = fa.id_anuncio
            """;

    // fecha_guardado no se envia: la asigna la base de datos.
    @Override
    public int insert(Favorito favorito) throws SQLException {
        if (favorito == null) {
            throw new IllegalArgumentException("El favorito no puede ser nulo");
        }
        String sql = """
                INSERT INTO favorito (id_usuario, id_anuncio, activo, usuario_creacion)
                VALUES (?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setInt(1, favorito.getUsuario().getIdUsuario());
            cmd.setInt(2, favorito.getAnuncio().getIdAnuncio());
            cmd.setBoolean(3, favorito.isActivo());
            cmd.setString(4, usuarioAuditoria(favorito.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el favorito");
            }
            favorito.setIdFavorito(leerIdGenerado(cmd));
            return favorito.getIdFavorito();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Favorito favorito) throws SQLException {
        if (favorito == null) {
            throw new IllegalArgumentException("El favorito no puede ser nulo");
        }
        String sql = """
                UPDATE favorito
                SET id_usuario = ?, id_anuncio = ?, activo = ?, usuario_modificacion = ?
                WHERE id_favorito = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, favorito.getUsuario().getIdUsuario());
            cmd.setInt(2, favorito.getAnuncio().getIdAnuncio());
            cmd.setBoolean(3, favorito.isActivo());
            cmd.setString(4, usuarioAuditoria(favorito.getUsuarioModificacion()));
            cmd.setInt(5, favorito.getIdFavorito());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idFavorito) throws SQLException {
        String sql = """
                UPDATE favorito SET activo = 0 WHERE id_favorito = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idFavorito);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Favorito findById(int idFavorito) throws SQLException {
        String sql = SELECT_BASE + "WHERE fa.id_favorito = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idFavorito);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Favorito()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Favorito> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE fa.activo = 1 ORDER BY fa.fecha_guardado DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Favorito> favoritos = new ArrayList<>();
            while (rs.next()) {
                favoritos.add(mapear(rs, new Favorito()));
            }
            return favoritos;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Favorito obtenerActivo(int idUsuario, int idAnuncio) throws SQLException {
        String sql = SELECT_BASE + "WHERE fa.id_usuario = ? AND fa.id_anuncio = ? AND fa.activo = 1";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            cmd.setInt(2, idAnuncio);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Favorito()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Favorito> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = SELECT_BASE + "WHERE fa.id_usuario = ? AND fa.activo = 1 ORDER BY fa.fecha_guardado DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<Favorito> favoritos = new ArrayList<>();
                while (rs.next()) {
                    favoritos.add(mapear(rs, new Favorito()));
                }
                return favoritos;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Favorito mapear(ResultSet rs, Favorito favorito) throws SQLException {
        super.mapear(rs, favorito);
        favorito.setIdFavorito(rs.getInt("id_favorito"));
        favorito.setFechaGuardado(leerFechaHora(rs, "fecha_guardado"));
        favorito.setUsuario(mapearUsuarioReferencia(rs, "usuario"));
        favorito.setAnuncio(mapearAnuncioReferencia(rs));
        return favorito;
    }
}
