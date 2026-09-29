package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.ReglaInsigniaDAO;
import pe.edu.pucp.reuse.modelo.enums.OperadorComparacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;

public class ReglaInsigniaDAOImpl extends RegistroDAOImpl<ReglaInsignia> implements ReglaInsigniaDAO {

    private static final String SELECT_BASE = """
            SELECT ri.id_regla, ri.tipo_metrica, ri.operador, ri.valor_objetivo,
                   ri.id_insignia, i.nombre AS insignia_nombre,
                   ri.activo, ri.fecha_creacion, ri.fecha_modificacion, ri.usuario_creacion, ri.usuario_modificacion
            FROM regla_insignia ri
            JOIN insignia i ON i.id_insignia = ri.id_insignia
            """;

    @Override
    public int insert(ReglaInsignia regla) throws SQLException {
        if (regla == null) {
            throw new IllegalArgumentException("La regla no puede ser nula");
        }
        String sql = """
                INSERT INTO regla_insignia (tipo_metrica, operador, valor_objetivo, id_insignia, activo,
                                            usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, regla.getTipoMetrica().name());
            cmd.setString(2, regla.getOperador().name());
            cmd.setDouble(3, regla.getValorObjetivo());
            cmd.setInt(4, regla.getInsignia().getIdInsignia());
            cmd.setBoolean(5, regla.isActivo());
            cmd.setString(6, usuarioAuditoria(regla.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la regla de insignia");
            }
            regla.setIdRegla(leerIdGenerado(cmd));
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
        String sql = """
                UPDATE regla_insignia
                SET tipo_metrica = ?, operador = ?, valor_objetivo = ?, id_insignia = ?, activo = ?,
                    usuario_modificacion = ?
                WHERE id_regla = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, regla.getTipoMetrica().name());
            cmd.setString(2, regla.getOperador().name());
            cmd.setDouble(3, regla.getValorObjetivo());
            cmd.setInt(4, regla.getInsignia().getIdInsignia());
            cmd.setBoolean(5, regla.isActivo());
            cmd.setString(6, usuarioAuditoria(regla.getUsuarioModificacion()));
            cmd.setInt(7, regla.getIdRegla());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idRegla) throws SQLException {
        String sql = """
                UPDATE regla_insignia SET activo = 0 WHERE id_regla = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idRegla);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ReglaInsignia findById(int idRegla) throws SQLException {
        String sql = SELECT_BASE + "WHERE ri.id_regla = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idRegla);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new ReglaInsignia()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<ReglaInsignia> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE ri.activo = 1 ORDER BY ri.id_insignia, ri.id_regla";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
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
        String sql = SELECT_BASE + "WHERE ri.id_insignia = ? AND ri.activo = 1 ORDER BY ri.id_regla";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idInsignia);
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
