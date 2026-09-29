package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CarreraDAO;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;

public class CarreraDAOImpl extends RegistroDAOImpl<Carrera> implements CarreraDAO {

    @Override
    public int insert(Carrera carrera) throws SQLException {
        if (carrera == null) {
            throw new IllegalArgumentException("La carrera no puede ser nula");
        }
        String sql = "{call insertar_carrera(?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", carrera.getNombre());
            cmd.setInt("p_id_facultad", carrera.getFacultad().getIdFacultad());
            cmd.setBoolean("p_activo", carrera.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(carrera.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            carrera.setIdCarrera(cmd.getInt("p_id"));
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
        String sql = "{call modificar_carrera(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", carrera.getIdCarrera());
            cmd.setString("p_nombre", carrera.getNombre());
            cmd.setInt("p_id_facultad", carrera.getFacultad().getIdFacultad());
            cmd.setBoolean("p_activo", carrera.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(carrera.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCarrera) throws SQLException {
        String sql = "{call eliminar_carrera(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCarrera);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Carrera findById(int idCarrera) throws SQLException {
        String sql = "{call buscar_carrera_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCarrera);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Carrera()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Carrera> findAll() throws SQLException {
        String sql = "{call listar_carreras()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call buscar_carrera_por_nombre(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", nombre);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Carrera()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Carrera> listarPorFacultad(int idFacultad) throws SQLException {
        String sql = "{call listar_carreras_por_facultad(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_facultad", idFacultad);
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
