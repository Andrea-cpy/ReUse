package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.AdministradorDAO;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;

public class AdministradorDAOImpl extends UsuarioBaseDAOImpl<AdministradorPUCP> implements AdministradorDAO {

    private static final String SELECT_ADMINISTRADOR = SELECT_USUARIO + """
            JOIN administrador a ON a.id_usuario = u.id_usuario
            """;

    @Override
    public int insert(AdministradorPUCP administrador) throws SQLException {
        if (administrador == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }
        // Dos tablas: usa la conexion de la transaccion abierta por la BL y no la cierra.
        Connection conn = TransactionsManager.getConnection();
        insertarUsuario(conn, administrador);

        String sql = """
                INSERT INTO administrador (id_usuario, activo, usuario_creacion)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, administrador.getIdUsuario());
            cmd.setBoolean(2, administrador.isActivo());
            cmd.setString(3, usuarioAuditoria(administrador.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el administrador");
            }
        }
        return administrador.getIdUsuario();
    }

    @Override
    public int update(AdministradorPUCP administrador) throws SQLException {
        if (administrador == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        int filas = modificarUsuario(conn, administrador);

        String sql = """
                UPDATE administrador
                SET activo = ?, usuario_modificacion = ?
                WHERE id_usuario = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setBoolean(1, administrador.isActivo());
            cmd.setString(2, usuarioAuditoria(administrador.getUsuarioModificacion()));
            cmd.setInt(3, administrador.getIdUsuario());
            cmd.executeUpdate();
        }
        return filas;
    }

    @Override
    public int delete(int idUsuario) throws SQLException {
        Connection conn = TransactionsManager.getConnection();
        String sql = """
                UPDATE administrador SET activo = 0 WHERE id_usuario = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            if (cmd.executeUpdate() == 0) {
                return 0;
            }
        }
        return eliminarUsuario(conn, idUsuario);
    }

    @Override
    public AdministradorPUCP findById(int idUsuario) throws SQLException {
        String sql = SELECT_ADMINISTRADOR + "WHERE u.id_usuario = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new AdministradorPUCP()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<AdministradorPUCP> findAll() throws SQLException {
        String sql = SELECT_ADMINISTRADOR + "WHERE a.activo = 1 ORDER BY u.apellido_paterno, u.nombres";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<AdministradorPUCP> administradores = new ArrayList<>();
            while (rs.next()) {
                administradores.add(mapear(rs, new AdministradorPUCP()));
            }
            return administradores;
        } finally {
            cerrarConexion(conn);
        }
    }
}
