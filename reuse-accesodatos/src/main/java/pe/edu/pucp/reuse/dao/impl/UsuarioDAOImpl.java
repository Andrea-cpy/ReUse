package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.UsuarioDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class UsuarioDAOImpl extends UsuarioBaseDAOImpl<UsuarioPUCP> implements UsuarioDAO {

    @Override
    public int insert(UsuarioPUCP usuario) throws SQLException {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        Connection conn = abrirConexion();
        try {
            return insertarUsuario(conn, usuario);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(UsuarioPUCP usuario) throws SQLException {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        Connection conn = abrirConexion();
        try {
            return modificarUsuario(conn, usuario);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idUsuario) throws SQLException {
        Connection conn = abrirConexion();
        try {
            return eliminarUsuario(conn, idUsuario);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public UsuarioPUCP findById(int idUsuario) throws SQLException {
        String sql = "{call buscar_usuario_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new UsuarioPUCP()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<UsuarioPUCP> findAll() throws SQLException {
        String sql = "{call listar_usuarios()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<UsuarioPUCP> usuarios = new ArrayList<>();
            while (rs.next()) {
                usuarios.add(mapear(rs, new UsuarioPUCP()));
            }
            return usuarios;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public UsuarioPUCP obtenerPorCorreo(String correoInstitucional) throws SQLException {
        if (correoInstitucional == null) {
            throw new IllegalArgumentException("El correo no puede ser nulo");
        }
        String sql = "{call buscar_usuario_por_correo(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_correo_institucional", correoInstitucional);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new UsuarioPUCP()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public UsuarioPUCP obtenerPorCodigo(String codigoPUCP) throws SQLException {
        if (codigoPUCP == null) {
            throw new IllegalArgumentException("El codigo PUCP no puede ser nulo");
        }
        String sql = "{call buscar_usuario_por_codigo(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_codigo_pucp", codigoPUCP);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new UsuarioPUCP()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int actualizarEstadoCuenta(int idUsuario, EstadoCuenta estadoCuenta) throws SQLException {
        if (estadoCuenta == null) {
            throw new IllegalArgumentException("El estado de cuenta no puede ser nulo");
        }
        String sql = "{call actualizar_estado_cuenta(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idUsuario);
            cmd.setString("p_estado_cuenta", estadoCuenta.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int actualizarContadorReportes(int idUsuario, int variacion) throws SQLException {
        String sql = "{call actualizar_contador_reportes(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idUsuario);
            cmd.setInt("p_variacion", variacion);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int recalcularReputacion(int idUsuario) throws SQLException {
        String sql = "{call recalcular_reputacion(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idUsuario);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws SQLException {
        if (metrica == null) {
            throw new IllegalArgumentException("La metrica no puede ser nula");
        }
        String sql = "{call obtener_metrica_usuario(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idUsuario);
            cmd.setString("p_metrica", metrica.name());
            cmd.registerOutParameter("p_valor", Types.DOUBLE);
            cmd.execute();
            return cmd.getDouble("p_valor");
        } finally {
            cerrarConexion(conn);
        }
    }
}
