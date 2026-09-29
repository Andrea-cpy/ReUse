package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.AdministradorDAO;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;

public class AdministradorDAOImpl extends UsuarioBaseDAOImpl<AdministradorPUCP> implements AdministradorDAO {

    @Override
    public int insert(AdministradorPUCP administrador) throws SQLException {
        if (administrador == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }
        // Dos tablas: usa la conexion de la transaccion abierta por la BL y no la cierra.
        Connection conn = TransactionsManager.getConnection();
        insertarUsuario(conn, administrador);

        String sql = "{call insertar_administrador(?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", administrador.getIdUsuario());
            cmd.setBoolean("p_activo", administrador.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(administrador.getUsuarioCreacion()));
            cmd.execute();
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

        String sql = "{call modificar_administrador(?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", administrador.getIdUsuario());
            cmd.setBoolean("p_activo", administrador.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(administrador.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
        }
        return filas;
    }

    @Override
    public int delete(int idUsuario) throws SQLException {
        Connection conn = TransactionsManager.getConnection();
        String sql = "{call eliminar_administrador(?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_usuario", idUsuario);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            if (cmd.getInt("p_filas") == 0) {
                return 0;
            }
        }
        return eliminarUsuario(conn, idUsuario);
    }

    @Override
    public AdministradorPUCP findById(int idUsuario) throws SQLException {
        String sql = "{call buscar_administrador_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new AdministradorPUCP()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<AdministradorPUCP> findAll() throws SQLException {
        String sql = "{call listar_administradores()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
