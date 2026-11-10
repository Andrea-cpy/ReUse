package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.ReglaInsigniaDAO;
import pe.edu.pucp.reuse.modelo.enums.OperadorComparacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public class ReglaInsigniaDAOImpl extends RegistroDAOImpl<ReglaInsignia> implements ReglaInsigniaDAO {

    @Override
    public int insert(ReglaInsignia regla) throws SQLException {
        if (regla == null) {
            throw new IllegalArgumentException("La regla no puede ser nula");
        }
        String sql = "{call insertar_regla_insignia(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_tipo_metrica", regla.getTipoMetrica().name());
            cmd.setString("p_operador", regla.getOperador().name());
            cmd.setDouble("p_valor_objetivo", regla.getValorObjetivo());
            cmd.setInt("p_id_insignia", regla.getInsignia().getIdInsignia());
            cmd.setBoolean("p_activo", regla.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(regla.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            regla.setIdRegla(cmd.getInt("p_id"));
            return regla.getIdRegla();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(ReglaInsignia regla) throws SQLException {
        if (regla == null) {
            throw new IllegalArgumentException("La regla no puede ser nula");
        }
        String sql = "{call modificar_regla_insignia(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", regla.getIdRegla());
            cmd.setString("p_tipo_metrica", regla.getTipoMetrica().name());
            cmd.setString("p_operador", regla.getOperador().name());
            cmd.setDouble("p_valor_objetivo", regla.getValorObjetivo());
            cmd.setInt("p_id_insignia", regla.getInsignia().getIdInsignia());
            cmd.setBoolean("p_activo", regla.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(regla.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idRegla) throws SQLException {
        String sql = "{call eliminar_regla_insignia(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idRegla);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ReglaInsignia findById(int idRegla) throws SQLException {
        String sql = "{call buscar_regla_insignia_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idRegla);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ReglaInsignia()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ReglaInsignia> findAll() throws SQLException {
        String sql = "{call listar_reglas_insignia()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<ReglaInsignia> reglas = new ArrayList<>();
            while (rs.next()) {
                reglas.add(mapear(rs, new ReglaInsignia()));
            }
            return reglas;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ReglaInsignia> listarPorInsignia(int idInsignia) throws SQLException {
        String sql = "{call listar_reglas_por_insignia(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_insignia", idInsignia);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<ReglaInsignia> reglas = new ArrayList<>();
                while (rs.next()) {
                    reglas.add(mapear(rs, new ReglaInsignia()));
                }
                return reglas;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected ReglaInsignia mapear(ResultSet rs, ReglaInsignia regla) throws SQLException {
        super.mapear(rs, regla);
        regla.setIdRegla(rs.getInt("id_regla"));
        regla.setTipoMetrica(TipoMetricaInsignia.valueOf(rs.getString("tipo_metrica")));
        regla.setOperador(OperadorComparacion.valueOf(rs.getString("operador")));
        regla.setValorObjetivo(rs.getDouble("valor_objetivo"));
        Insignia insignia = new Insignia();
        insignia.setIdInsignia(rs.getInt("id_insignia"));
        insignia.setNombre(rs.getString("insignia_nombre"));
        regla.setInsignia(insignia);
        return regla;
    }
}
