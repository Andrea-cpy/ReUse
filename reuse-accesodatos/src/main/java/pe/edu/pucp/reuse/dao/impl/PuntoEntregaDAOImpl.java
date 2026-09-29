package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.PuntoEntregaDAO;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;

public class PuntoEntregaDAOImpl extends RegistroDAOImpl<PuntoEntrega> implements PuntoEntregaDAO {

    @Override
    public int insert(PuntoEntrega punto) throws SQLException {
        if (punto == null) {
            throw new IllegalArgumentException("El punto de entrega no puede ser nulo");
        }
        String sql = "{call insertar_punto_entrega(?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", punto.getNombre());
            cmd.setString("p_referencia", punto.getReferencia());
            cmd.setString("p_ubicacion", punto.getUbicacion());
            cmd.setBoolean("p_activo", punto.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(punto.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            punto.setIdPuntoEntrega(cmd.getInt("p_id"));
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
        String sql = "{call modificar_punto_entrega(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", punto.getIdPuntoEntrega());
            cmd.setString("p_nombre", punto.getNombre());
            cmd.setString("p_referencia", punto.getReferencia());
            cmd.setString("p_ubicacion", punto.getUbicacion());
            cmd.setBoolean("p_activo", punto.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(punto.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idPuntoEntrega) throws SQLException {
        String sql = "{call eliminar_punto_entrega(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idPuntoEntrega);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public PuntoEntrega findById(int idPuntoEntrega) throws SQLException {
        String sql = "{call buscar_punto_entrega_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idPuntoEntrega);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new PuntoEntrega()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<PuntoEntrega> findAll() throws SQLException {
        String sql = "{call listar_puntos_entrega()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call buscar_punto_entrega_por_nombre(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_nombre", nombre);
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
