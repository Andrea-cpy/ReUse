package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CarreraDAO;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;

public class CarreraDAOImpl extends RegistroDAOImpl<Carrera> implements CarreraDAO {

    private static final String SELECT_BASE = """
            SELECT c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre,
                   c.activo, c.fecha_creacion, c.fecha_modificacion, c.usuario_creacion, c.usuario_modificacion
            FROM carrera c
            JOIN facultad f ON f.id_facultad = c.id_facultad
            """;

    @Override
    public int insert(Carrera carrera) throws SQLException {
        if (carrera == null) {
            throw new IllegalArgumentException("La carrera no puede ser nula");
        }
        String sql = """
                INSERT INTO carrera (nombre, id_facultad, activo, usuario_creacion)
                VALUES (?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, carrera.getNombre());
            cmd.setInt(2, carrera.getFacultad().getIdFacultad());
            cmd.setBoolean(3, carrera.isActivo());
            cmd.setString(4, usuarioAuditoria(carrera.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la carrera");
            }
            carrera.setIdCarrera(leerIdGenerado(cmd));
            return carrera.getIdCarrera();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Carrera carrera) throws SQLException {
        if (carrera == null) {
            throw new IllegalArgumentException("La carrera no puede ser nula");
        }
        String sql = """
                UPDATE carrera
                SET nombre = ?, id_facultad = ?, activo = ?, usuario_modificacion = ?
                WHERE id_carrera = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, carrera.getNombre());
            cmd.setInt(2, carrera.getFacultad().getIdFacultad());
            cmd.setBoolean(3, carrera.isActivo());
            cmd.setString(4, usuarioAuditoria(carrera.getUsuarioModificacion()));
            cmd.setInt(5, carrera.getIdCarrera());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCarrera) throws SQLException {
        String sql = """
                UPDATE carrera SET activo = 0 WHERE id_carrera = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCarrera);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Carrera findById(int idCarrera) throws SQLException {
        String sql = SELECT_BASE + "WHERE c.id_carrera = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCarrera);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Carrera()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Carrera> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE c.activo = 1 ORDER BY c.nombre";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Carrera> carreras = new ArrayList<>();
            while (rs.next()) {
                carreras.add(mapear(rs, new Carrera()));
            }
            return carreras;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Carrera obtenerPorNombre(String nombre) throws SQLException {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        String sql = SELECT_BASE + "WHERE c.nombre = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, nombre);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Carrera()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Carrera> listarPorFacultad(int idFacultad) throws SQLException {
        String sql = SELECT_BASE + "WHERE c.id_facultad = ? AND c.activo = 1 ORDER BY c.nombre";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idFacultad);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<Carrera> carreras = new ArrayList<>();
                while (rs.next()) {
                    carreras.add(mapear(rs, new Carrera()));
                }
                return carreras;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Carrera mapear(ResultSet rs, Carrera carrera) throws SQLException {
        super.mapear(rs, carrera);
        carrera.setIdCarrera(rs.getInt("id_carrera"));
        carrera.setNombre(rs.getString("nombre"));
        Facultad facultad = new Facultad();
        facultad.setIdFacultad(rs.getInt("id_facultad"));
        facultad.setNombre(rs.getString("facultad_nombre"));
        carrera.setFacultad(facultad);
        return carrera;
    }
}
