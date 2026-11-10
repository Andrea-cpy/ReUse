package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.FavoritoDAO;
import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;

public class FavoritoDAOImpl extends RegistroDAOImpl<Favorito> implements FavoritoDAO {

    // fecha_guardado no se envia: la asigna la base de datos.
    @Override
    public int insert(Favorito favorito) throws SQLException {
        if (favorito == null) {
            throw new IllegalArgumentException("El favorito no puede ser nulo");
        }
        String sql = "{call insertar_favorito(?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", favorito.getUsuario().getIdUsuario());
            cmd.setInt("p_id_anuncio", favorito.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", favorito.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(favorito.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            favorito.setIdFavorito(cmd.getInt("p_id"));
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
        String sql = "{call modificar_favorito(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", favorito.getIdFavorito());
            cmd.setInt("p_id_usuario", favorito.getUsuario().getIdUsuario());
            cmd.setInt("p_id_anuncio", favorito.getAnuncio().getIdAnuncio());
            cmd.setBoolean("p_activo", favorito.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(favorito.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idFavorito) throws SQLException {
        String sql = "{call eliminar_favorito(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idFavorito);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Favorito findById(int idFavorito) throws SQLException {
        String sql = "{call buscar_favorito_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idFavorito);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Favorito()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Favorito> findAll() throws SQLException {
        String sql = "{call listar_favoritos()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call buscar_favorito_activo(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", idUsuario);
            cmd.setInt("p_id_anuncio", idAnuncio);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Favorito()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Favorito> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = "{call listar_favoritos_por_usuario(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", idUsuario);
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
