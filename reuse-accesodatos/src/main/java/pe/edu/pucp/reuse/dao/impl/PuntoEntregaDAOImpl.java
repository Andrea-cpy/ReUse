package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.PuntoEntregaDAO;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;

public class PuntoEntregaDAOImpl extends RegistroDAOImpl<PuntoEntrega> implements PuntoEntregaDAO {

    private static final String SELECT_BASE = """
            SELECT id_punto_entrega, nombre, referencia, ubicacion,
                   activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
            FROM punto_entrega
            """;

    @Override
    public int insert(PuntoEntrega punto) throws SQLException {
        if (punto == null) {
            throw new IllegalArgumentException("El punto de entrega no puede ser nulo");
        }
        String sql = """
                INSERT INTO punto_entrega (nombre, referencia, ubicacion, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, punto.getNombre());
            cmd.setString(2, punto.getReferencia());
            cmd.setString(3, punto.getUbicacion());
            cmd.setBoolean(4, punto.isActivo());
            cmd.setString(5, usuarioAuditoria(punto.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el punto de entrega");
            }
            punto.setIdPuntoEntrega(leerIdGenerado(cmd));
            return punto.getIdPuntoEntrega();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(PuntoEntrega punto) throws SQLException {
        if (punto == null) {
            throw new IllegalArgumentException("El punto de entrega no puede ser nulo");
        }
        String sql = """
                UPDATE punto_entrega
                SET nombre = ?, referencia = ?, ubicacion = ?, activo = ?, usuario_modificacion = ?
                WHERE id_punto_entrega = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, punto.getNombre());
            cmd.setString(2, punto.getReferencia());
            cmd.setString(3, punto.getUbicacion());
            cmd.setBoolean(4, punto.isActivo());
            cmd.setString(5, usuarioAuditoria(punto.getUsuarioModificacion()));
            cmd.setInt(6, punto.getIdPuntoEntrega());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idPuntoEntrega) throws SQLException {
        String sql = """
                UPDATE punto_entrega SET activo = 0 WHERE id_punto_entrega = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idPuntoEntrega);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public PuntoEntrega findById(int idPuntoEntrega) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_punto_entrega = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idPuntoEntrega);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new PuntoEntrega()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<PuntoEntrega> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE activo = 1 ORDER BY nombre";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<PuntoEntrega> puntos = new ArrayList<>();
            while (rs.next()) {
                puntos.add(mapear(rs, new PuntoEntrega()));
            }
            return puntos;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public PuntoEntrega obtenerPorNombre(String nombre) throws SQLException {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        String sql = SELECT_BASE + "WHERE nombre = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, nombre);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new PuntoEntrega()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected PuntoEntrega mapear(ResultSet rs, PuntoEntrega punto) throws SQLException {
        super.mapear(rs, punto);
        punto.setIdPuntoEntrega(rs.getInt("id_punto_entrega"));
        punto.setNombre(rs.getString("nombre"));
        punto.setReferencia(rs.getString("referencia"));
        punto.setUbicacion(rs.getString("ubicacion"));
        return punto;
    }
}
