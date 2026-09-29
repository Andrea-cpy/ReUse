package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pe.edu.pucp.reuse.dao.MaterialAcademicoDAO;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;
import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;

public class MaterialAcademicoDAOImpl extends RegistroDAOImpl<MaterialAcademico> implements MaterialAcademicoDAO {

    private static final String SELECT_BASE = """
            SELECT m.id_material, m.titulo, m.id_categoria, cm.nombre AS categoria_nombre,
                   m.activo, m.fecha_creacion, m.fecha_modificacion, m.usuario_creacion, m.usuario_modificacion
            FROM material_academico m
            JOIN categoria_material cm ON cm.id_categoria = m.id_categoria
            """;

    // Carreras activas de la tabla intermedia, con su facultad.
    private static final String SELECT_CARRERAS = """
            SELECT mc.id_material, c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre
            FROM material_carrera mc
            JOIN carrera c ON c.id_carrera = mc.id_carrera
            JOIN facultad f ON f.id_facultad = c.id_facultad
            WHERE mc.activo = 1
            """;

    @Override
    public int insert(MaterialAcademico material) throws SQLException {
        if (material == null) {
            throw new IllegalArgumentException("El material no puede ser nulo");
        }
        // Dos tablas: usa la conexion de la transaccion abierta por la BL y no la cierra.
        Connection conn = TransactionsManager.getConnection();
        String sql = """
                INSERT INTO material_academico (titulo, id_categoria, activo, usuario_creacion)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, material.getTitulo());
            cmd.setInt(2, material.getCategoria().getIdCategoria());
            cmd.setBoolean(3, material.isActivo());
            cmd.setString(4, usuarioAuditoria(material.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el material academico");
            }
            material.setIdMaterial(leerIdGenerado(cmd));
        }
        guardarCarreras(conn, material, usuarioAuditoria(material.getUsuarioCreacion()));
        return material.getIdMaterial();
    }

    @Override
    public int update(MaterialAcademico material) throws SQLException {
        if (material == null) {
            throw new IllegalArgumentException("El material no puede ser nulo");
        }
        Connection conn = TransactionsManager.getConnection();
        String usuario = usuarioAuditoria(material.getUsuarioModificacion());
        String sql = """
                UPDATE material_academico
                SET titulo = ?, id_categoria = ?, activo = ?, usuario_modificacion = ?
                WHERE id_material = ?
                """;
        int filas;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, material.getTitulo());
            cmd.setInt(2, material.getCategoria().getIdCategoria());
            cmd.setBoolean(3, material.isActivo());
            cmd.setString(4, usuario);
            cmd.setInt(5, material.getIdMaterial());
            filas = cmd.executeUpdate();
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
        String sql = """
                UPDATE material_academico SET activo = 0 WHERE id_material = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idMaterial);
            return cmd.executeUpdate();
        }
    }

    @Override
    public MaterialAcademico findById(int idMaterial) throws SQLException {
        String sql = SELECT_BASE + "WHERE m.id_material = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idMaterial);
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
        String sql = SELECT_BASE + "WHERE m.activo = 1 ORDER BY m.titulo";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            ArrayList<MaterialAcademico> materiales = new ArrayList<>();
            Map<Integer, MaterialAcademico> porId = new HashMap<>();
            try (ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    MaterialAcademico material = mapear(rs, new MaterialAcademico());
                    materiales.add(material);
                    porId.put(material.getIdMaterial(), material);
                }
            }
            // Una sola consulta para las carreras de todos los materiales (evita N+1 consultas).
            cargarCarreras(conn, porId);
            return materiales;
        } finally {
            cerrarConexion(conn);
        }
    }

    private void guardarCarreras(Connection conn, MaterialAcademico material, String usuario)
            throws SQLException {
        List<Carrera> carreras = material.getCarreras();
        if (carreras.isEmpty()) {
            return;
        }
        // Si la fila ya existia (desactivada), se reactiva en lugar de duplicar la PK compuesta.
        String sql = """
                INSERT INTO material_carrera (id_material, id_carrera, activo, usuario_creacion)
                VALUES (?, ?, 1, ?)
                ON DUPLICATE KEY UPDATE activo = 1, usuario_modificacion = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            for (Carrera carrera : carreras) {
                cmd.setInt(1, material.getIdMaterial());
                cmd.setInt(2, carrera.getIdCarrera());
                cmd.setString(3, usuario);
                cmd.setString(4, usuario);
                cmd.addBatch();
            }
            cmd.executeBatch();
        }
    }

    private void desactivarCarreras(Connection conn, int idMaterial) throws SQLException {
        String sql = """
                UPDATE material_carrera SET activo = 0 WHERE id_material = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idMaterial);
            cmd.executeUpdate();
        }
    }

    private void cargarCarreras(Connection conn, Map<Integer, MaterialAcademico> porId) throws SQLException {
        if (porId.isEmpty()) {
            return;
        }
        String sql = porId.size() == 1
                ? SELECT_CARRERAS + "AND mc.id_material = ? ORDER BY c.nombre"
                : SELECT_CARRERAS + "ORDER BY c.nombre";
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            if (porId.size() == 1) {
                cmd.setInt(1, porId.keySet().iterator().next());
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
