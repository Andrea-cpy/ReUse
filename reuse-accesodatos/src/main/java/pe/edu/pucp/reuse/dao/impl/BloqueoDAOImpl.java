package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.BloqueoDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoBloqueo;
import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;

public class BloqueoDAOImpl extends RegistroDAOImpl<Bloqueo> implements BloqueoDAO {

    private static final String SELECT_BASE = """
            SELECT b.id_bloqueo, b.fecha, b.estado,
                   ur.id_usuario AS bloqueador_id, ur.codigo_pucp AS bloqueador_codigo,
                   ur.nombres AS bloqueador_nombres, ur.apellido_paterno AS bloqueador_apellido,
                   ud.id_usuario AS bloqueado_id, ud.codigo_pucp AS bloqueado_codigo,
                   ud.nombres AS bloqueado_nombres, ud.apellido_paterno AS bloqueado_apellido,
                   b.activo, b.fecha_creacion, b.fecha_modificacion, b.usuario_creacion, b.usuario_modificacion
            FROM bloqueo b
            JOIN usuario ur ON ur.id_usuario = b.id_bloqueador
            JOIN usuario ud ON ud.id_usuario = b.id_bloqueado
            """;

    // La fecha del bloqueo no se envia: la asigna la base de datos.
    @Override
    public int insert(Bloqueo bloqueo) throws SQLException {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo no puede ser nulo");
        }
        String sql = """
                INSERT INTO bloqueo (estado, id_bloqueador, id_bloqueado, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, bloqueo.getEstado().name());
            cmd.setInt(2, bloqueo.getBloqueador().getIdUsuario());
            cmd.setInt(3, bloqueo.getBloqueado().getIdUsuario());
            cmd.setBoolean(4, bloqueo.isActivo());
            cmd.setString(5, usuarioAuditoria(bloqueo.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el bloqueo");
            }
            bloqueo.setIdBloqueo(leerIdGenerado(cmd));
            return bloqueo.getIdBloqueo();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Bloqueo bloqueo) throws SQLException {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo no puede ser nulo");
        }
        String sql = """
                UPDATE bloqueo
                SET estado = ?, id_bloqueador = ?, id_bloqueado = ?, activo = ?, usuario_modificacion = ?
                WHERE id_bloqueo = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, bloqueo.getEstado().name());
            cmd.setInt(2, bloqueo.getBloqueador().getIdUsuario());
            cmd.setInt(3, bloqueo.getBloqueado().getIdUsuario());
            cmd.setBoolean(4, bloqueo.isActivo());
            cmd.setString(5, usuarioAuditoria(bloqueo.getUsuarioModificacion()));
            cmd.setInt(6, bloqueo.getIdBloqueo());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idBloqueo) throws SQLException {
        String sql = """
                UPDATE bloqueo SET activo = 0 WHERE id_bloqueo = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idBloqueo);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Bloqueo findById(int idBloqueo) throws SQLException {
        String sql = SELECT_BASE + "WHERE b.id_bloqueo = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idBloqueo);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Bloqueo()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Bloqueo> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE b.activo = 1 ORDER BY b.fecha DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Bloqueo> bloqueos = new ArrayList<>();
            while (rs.next()) {
                bloqueos.add(mapear(rs, new Bloqueo()));
            }
            return bloqueos;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Bloqueo obtenerActivoEntre(int idUsuarioA, int idUsuarioB) throws SQLException {
        String sql = SELECT_BASE + """
                WHERE b.estado = 'ACTIVO' AND b.activo = 1
                  AND ((b.id_bloqueador = ? AND b.id_bloqueado = ?)
                    OR (b.id_bloqueador = ? AND b.id_bloqueado = ?))
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuarioA);
            cmd.setInt(2, idUsuarioB);
            cmd.setInt(3, idUsuarioB);
            cmd.setInt(4, idUsuarioA);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Bloqueo()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Bloqueo mapear(ResultSet rs, Bloqueo bloqueo) throws SQLException {
        super.mapear(rs, bloqueo);
        bloqueo.setIdBloqueo(rs.getInt("id_bloqueo"));
        bloqueo.setFecha(leerFechaHora(rs, "fecha"));
        bloqueo.setEstado(EstadoBloqueo.valueOf(rs.getString("estado")));
        bloqueo.setBloqueador(mapearUsuarioReferencia(rs, "bloqueador"));
        bloqueo.setBloqueado(mapearUsuarioReferencia(rs, "bloqueado"));
        return bloqueo;
    }
}
