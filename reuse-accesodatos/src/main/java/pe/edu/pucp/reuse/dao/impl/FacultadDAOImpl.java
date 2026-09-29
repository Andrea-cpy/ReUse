package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.FacultadDAO;
import pe.edu.pucp.reuse.modelo.academico.Facultad;

public class FacultadDAOImpl extends RegistroDAOImpl<Facultad> implements FacultadDAO {

    @Override
    public int insert(Facultad facultad) throws SQLException {
        if (facultad == null) {
            throw new IllegalArgumentException("La facultad no puede ser nula");
        }
        String sql = "{call insertar_facultad(?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", facultad.getNombre());
            cmd.setBoolean("p_activo", facultad.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(facultad.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            facultad.setIdFacultad(cmd.getInt("p_id"));
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
        String sql = "{call modificar_facultad(?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", facultad.getIdFacultad());
            cmd.setString("p_nombre", facultad.getNombre());
            cmd.setBoolean("p_activo", facultad.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(facultad.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idFacultad) throws SQLException {
        String sql = "{call eliminar_facultad(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idFacultad);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Facultad findById(int idFacultad) throws SQLException {
        String sql = "{call buscar_facultad_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idFacultad);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Facultad()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Facultad> findAll() throws SQLException {
        String sql = "{call listar_facultades()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call buscar_facultad_por_nombre(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", nombre);
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
