package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String sql = SELECT_USUARIO + "WHERE u.id_usuario = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new UsuarioPUCP()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<UsuarioPUCP> findAll() throws SQLException {
        String sql = SELECT_USUARIO + "WHERE u.activo = 1 ORDER BY u.apellido_paterno, u.nombres";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
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
        String sql = SELECT_USUARIO + "WHERE u.correo_institucional = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, correoInstitucional);
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
        String sql = SELECT_USUARIO + "WHERE u.codigo_pucp = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, codigoPUCP);
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
        String sql = """
                UPDATE usuario SET estado_cuenta = ? WHERE id_usuario = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estadoCuenta.name());
            cmd.setInt(2, idUsuario);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int actualizarContadorReportes(int idUsuario, int variacion) throws SQLException {
        String sql = """
                UPDATE usuario
                SET contador_reportes = GREATEST(contador_reportes + ?, 0)
                WHERE id_usuario = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, variacion);
            cmd.setInt(2, idUsuario);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int recalcularReputacion(int idUsuario) throws SQLException {
        String sql = """
                UPDATE usuario
                SET reputacion = (SELECT COALESCE(ROUND(AVG(c.puntaje), 2), 0)
                                  FROM calificacion c
                                  WHERE c.id_calificado = ? AND c.activo = 1)
                WHERE id_usuario = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            cmd.setInt(2, idUsuario);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws SQLException {
        if (metrica == null) {
            throw new IllegalArgumentException("La metrica no puede ser nula");
        }
        String sql = switch (metrica) {
            case VENTAS_COMPLETADAS -> """
                    SELECT COUNT(*) FROM transaccion t
                    JOIN anuncio a ON a.id_anuncio = t.id_anuncio
                    WHERE a.id_vendedor = ? AND t.estado = 'COMPLETADA' AND t.activo = 1
                    """;
            case COMPRAS_COMPLETADAS -> """
                    SELECT COUNT(*) FROM transaccion t
                    WHERE t.id_comprador = ? AND t.estado = 'COMPLETADA' AND t.activo = 1
                    """;
            case TRANSACCIONES_COMPLETADAS -> """
                    SELECT COUNT(*) FROM transaccion t
                    JOIN anuncio a ON a.id_anuncio = t.id_anuncio
                    WHERE (a.id_vendedor = ? OR t.id_comprador = ?)
                      AND t.estado = 'COMPLETADA' AND t.activo = 1
                    """;
            case CALIFICACION_PROMEDIO -> """
                    SELECT COALESCE(AVG(c.puntaje), 0) FROM calificacion c
                    WHERE c.id_calificado = ? AND c.activo = 1
                    """;
            case REPORTES_SANCIONADOS -> """
                    SELECT COUNT(*) FROM reporte r
                    JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
                    WHERE ru.id_denunciado = ? AND r.estado_revision = 'SANCIONADO' AND r.activo = 1
                    """;
            case INASISTENCIAS_SANCIONADAS -> """
                    SELECT COUNT(*) FROM reporte r
                    JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
                    WHERE ru.id_denunciado = ? AND r.estado_revision = 'SANCIONADO' AND r.activo = 1
                      AND ru.motivo = 'NO_SE_PRESENTO_A_LA_CITA'
                    """;
        };
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            if (metrica == TipoMetricaInsignia.TRANSACCIONES_COMPLETADAS) {
                cmd.setInt(2, idUsuario);
            }
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } finally {
            cerrarConexion(conn);
        }
    }
}
