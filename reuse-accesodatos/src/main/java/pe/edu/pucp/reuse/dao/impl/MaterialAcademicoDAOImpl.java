package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pe.edu.pucp.reuse.dao.MaterialAcademicoDAO;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;
import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;

public class MaterialAcademicoDAOImpl extends RegistroDAOImpl<MaterialAcademico> implements MaterialAcademicoDAO {

    @Override
    public int insert(MaterialAcademico material) throws SQLException {
        if (material == null) {
            throw new IllegalArgumentException("El material no puede ser nulo");
        }
        // Dos tablas: usa la conexion de la transaccion abierta por la BL y no la cierra.
        Connection conn = TransactionsManager.getConnection();
        String usuario = usuarioAuditoria(material.getUsuarioCreacion());
        String sql = "{call insertar_material_academico(?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_titulo", material.getTitulo());
            cmd.setInt("p_id_categoria", material.getCategoria().getIdCategoria());
            cmd.setBoolean("p_activo", material.isActivo());
            cmd.setString("p_usuario_creacion", usuario);
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            material.setIdMaterial(cmd.getInt("p_id"));
        }
        guardarCarreras(conn, material, usuario);
        return material.getIdMaterial();
    }

    @Override
    public int update(MaterialAcademico material) throws SQLException {
        if (material == null) {
            throw new IllegalArgumentException("El material no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        String usuario = usuarioAuditoria(material.getUsuarioModificacion());
        String sql = "{call modificar_material_academico(?, ?, ?, ?, ?, ?)}";
        int filas;
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", material.getIdMaterial());
            cmd.setString("p_titulo", material.getTitulo());
            cmd.setInt("p_id_categoria", material.getCategoria().getIdCategoria());
            cmd.setBoolean("p_activo", material.isActivo());
            cmd.setString("p_usuario_modificacion", usuario);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            filas = cmd.getInt("p_filas");
        }
        // Se desactivan las carreras anteriores y se activan (o insertan) las actuales.
        desactivarCarreras(conn, material.getIdMaterial());
        guardarCarreras(conn, material, usuario);
        return filas;
    }

    @Override
    public int delete(int idMaterial) throws SQLException {
        Connection conn = TransactionsManager.getConnection();
        desactivarCarreras(conn, idMaterial);
        String sql = "{call eliminar_material_academico(?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idMaterial);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        }
    }

    @Override
    public MaterialAcademico findById(int idMaterial) throws SQLException {
        String sql = "{call buscar_material_academico_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idMaterial);
            MaterialAcademico material;
            try (ResultSet rs = cmd.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                material = mapear(rs, new MaterialAcademico());
            }
            Map<Integer, MaterialAcademico> porId = new HashMap<>();
            porId.put(material.getIdMaterial(), material);
            cargarCarreras(conn, porId);
            return material;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<MaterialAcademico> findAll() throws SQLException {
        String sql = "{call listar_materiales_academicos()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            ArrayList<MaterialAcademico> materiales = new ArrayList<>();
            Map<Integer, MaterialAcademico> porId = new HashMap<>();
            try (ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    MaterialAcademico material = mapear(rs, new MaterialAcademico());
                    materiales.add(material);
                    porId.put(material.getIdMaterial(), material);
                }
            }
            // Una sola llamada para las carreras de todos los materiales (evita N+1 llamadas).
            cargarCarreras(conn, porId);
            return materiales;
        } finally {
            cerrarConexion(conn);
        }
    }

    // Si la fila ya existia (desactivada), el procedimiento la reactiva en lugar de duplicar la PK.
    private void guardarCarreras(Connection conn, MaterialAcademico material, String usuario)
            throws SQLException {
        String sql = "{call guardar_material_carrera(?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            for (Carrera carrera : material.getCarreras()) {
                cmd.setInt("p_id_material", material.getIdMaterial());
                cmd.setInt("p_id_carrera", carrera.getIdCarrera());
                cmd.setString("p_usuario", usuario);
                cmd.execute();
            }
        }
    }

    private void desactivarCarreras(Connection conn, int idMaterial) throws SQLException {
        String sql = "{call desactivar_carreras_material(?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_material", idMaterial);
            cmd.execute();
        }
    }

    private void cargarCarreras(Connection conn, Map<Integer, MaterialAcademico> porId) throws SQLException {
        if (porId.isEmpty()) {
            return;
        }
        boolean unSoloMaterial = porId.size() == 1;
        String sql = unSoloMaterial
                ? "{call listar_carreras_por_material(?)}"
                : "{call listar_carreras_de_materiales()}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            if (unSoloMaterial) {
                cmd.setInt("p_id_material", porId.keySet().iterator().next());
            }
            try (ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    MaterialAcademico material = porId.get(rs.getInt("id_material"));
                    if (material == null) {
                        continue;
                    }
                    Facultad facultad = new Facultad();
                    facultad.setIdFacultad(rs.getInt("id_facultad"));
                    facultad.setNombre(rs.getString("facultad_nombre"));
                    Carrera carrera = new Carrera();
                    carrera.setIdCarrera(rs.getInt("id_carrera"));
                    carrera.setNombre(rs.getString("nombre"));
                    carrera.setFacultad(facultad);
                    material.agregarCarrera(carrera);
                }
            }
        }
    }

    @Override
    protected MaterialAcademico mapear(ResultSet rs, MaterialAcademico material) throws SQLException {
        super.mapear(rs, material);
        material.setIdMaterial(rs.getInt("id_material"));
        material.setTitulo(rs.getString("titulo"));
        CategoriaMaterial categoria = new CategoriaMaterial();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("categoria_nombre"));
        material.setCategoria(categoria);
        return material;
    }
}
