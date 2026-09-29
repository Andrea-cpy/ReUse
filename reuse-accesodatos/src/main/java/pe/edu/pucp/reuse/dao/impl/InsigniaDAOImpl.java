package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.InsigniaDAO;
import pe.edu.pucp.reuse.dao.ReglaInsigniaDAO;
import pe.edu.pucp.reuse.modelo.enums.TipoInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public class InsigniaDAOImpl extends RegistroDAOImpl<Insignia> implements InsigniaDAO {

    @Override
    public int insert(Insignia insignia) throws SQLException {
        if (insignia == null) {
            throw new IllegalArgumentException("La insignia no puede ser nula");
        }
        String sql = "{call insertar_insignia(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", insignia.getNombre());
            cmd.setString("p_descripcion", insignia.getDescripcion());
            cmd.setString("p_icono", insignia.getIcono());
            cmd.setString("p_tipo", insignia.getTipo().name());
            cmd.setBoolean("p_activo", insignia.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(insignia.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            insignia.setIdInsignia(cmd.getInt("p_id"));
            return insignia.getIdInsignia();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Insignia insignia) throws SQLException {
        if (insignia == null) {
            throw new IllegalArgumentException("La insignia no puede ser nula");
        }
        String sql = "{call modificar_insignia(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", insignia.getIdInsignia());
            cmd.setString("p_nombre", insignia.getNombre());
            cmd.setString("p_descripcion", insignia.getDescripcion());
            cmd.setString("p_icono", insignia.getIcono());
            cmd.setString("p_tipo", insignia.getTipo().name());
            cmd.setBoolean("p_activo", insignia.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(insignia.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idInsignia) throws SQLException {
        String sql = "{call eliminar_insignia(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idInsignia);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    // Incluye el detalle: las reglas activas de la insignia.
    @Override
    public Insignia findById(int idInsignia) throws SQLException {
        String sql = "{call buscar_insignia_por_id(?)}";
        Insignia insignia;
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idInsignia);
            try (ResultSet rs = cmd.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                insignia = mapear(rs, new Insignia());
            }
        } finally {
            cerrarConexion(conn);
        }
        ReglaInsigniaDAO reglaInsigniaDAO = new ReglaInsigniaDAOImpl();
        for (ReglaInsignia regla : reglaInsigniaDAO.listarPorInsignia(idInsignia)) {
            insignia.agregarRegla(regla);
        }
        return insignia;
    }

    @Override
    public ArrayList<Insignia> findAll() throws SQLException {
        String sql = "{call listar_insignias()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Insignia> insignias = new ArrayList<>();
            while (rs.next()) {
                insignias.add(mapear(rs, new Insignia()));
            }
            return insignias;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Insignia obtenerPorNombre(String nombre) throws SQLException {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        String sql = "{call buscar_insignia_por_nombre(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", nombre);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Insignia()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Insignia mapear(ResultSet rs, Insignia insignia) throws SQLException {
        super.mapear(rs, insignia);
        insignia.setIdInsignia(rs.getInt("id_insignia"));
        insignia.setNombre(rs.getString("nombre"));
        insignia.setDescripcion(rs.getString("descripcion"));
        insignia.setIcono(rs.getString("icono"));
        insignia.setTipo(TipoInsignia.valueOf(rs.getString("tipo")));
        return insignia;
    }
}
