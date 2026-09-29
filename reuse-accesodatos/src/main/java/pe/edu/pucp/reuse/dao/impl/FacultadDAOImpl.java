package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.FacultadDAO;
import pe.edu.pucp.reuse.modelo.academico.Facultad;

public class FacultadDAOImpl extends RegistroDAOImpl<Facultad> implements FacultadDAO {

    private static final String SELECT_BASE = """
            SELECT id_facultad, nombre,
                   activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
            FROM facultad
            """;

    @Override
    public int insert(Facultad facultad) throws SQLException {
        if (facultad == null) {
            throw new IllegalArgumentException("La facultad no puede ser nula");
        }
        String sql = """
                INSERT INTO facultad (nombre, activo, usuario_creacion)
                VALUES (?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, facultad.getNombre());
            cmd.setBoolean(2, facultad.isActivo());
            cmd.setString(3, usuarioAuditoria(facultad.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la facultad");
            }
            facultad.setIdFacultad(leerIdGenerado(cmd));
            return facultad.getIdFacultad();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Facultad facultad) throws SQLException {
        if (facultad == null) {
            throw new IllegalArgumentException("La facultad no puede ser nula");
        }
        String sql = """
                UPDATE facultad
                SET nombre = ?, activo = ?, usuario_modificacion = ?
                WHERE id_facultad = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, facultad.getNombre());
            cmd.setBoolean(2, facultad.isActivo());
            cmd.setString(3, usuarioAuditoria(facultad.getUsuarioModificacion()));
            cmd.setInt(4, facultad.getIdFacultad());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idFacultad) throws SQLException {
        String sql = """
                UPDATE facultad SET activo = 0 WHERE id_facultad = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idFacultad);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Facultad findById(int idFacultad) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_facultad = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idFacultad);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Facultad()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Facultad> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE activo = 1 ORDER BY nombre";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Facultad> facultades = new ArrayList<>();
            while (rs.next()) {
                facultades.add(mapear(rs, new Facultad()));
            }
            return facultades;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Facultad obtenerPorNombre(String nombre) throws SQLException {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        String sql = SELECT_BASE + "WHERE nombre = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, nombre);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Facultad()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Facultad mapear(ResultSet rs, Facultad facultad) throws SQLException {
        super.mapear(rs, facultad);
        facultad.setIdFacultad(rs.getInt("id_facultad"));
        facultad.setNombre(rs.getString("nombre"));
        return facultad;
    }
}
