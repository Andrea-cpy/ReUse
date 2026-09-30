package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CitaEntregaDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCita;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class CitaEntregaDAOImpl extends RegistroDAOImpl<CitaEntrega> implements CitaEntregaDAO {

    @Override
    public int insert(CitaEntrega cita) throws SQLException {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }
        String sql = "{call insertar_cita_entrega(?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setTimestamp("p_fecha_hora", Timestamp.valueOf(cita.getFechaHora()));
            cmd.setString("p_estado", cita.getEstado().name());
            cmd.setInt("p_id_punto_entrega", cita.getPuntoEntrega().getIdPuntoEntrega());
            cmd.setInt("p_id_transaccion", cita.getTransaccion().getIdTransaccion());
            cmd.setBoolean("p_activo", cita.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(cita.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            cita.setIdCita(cmd.getInt("p_id"));
            return cita.getIdCita();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(CitaEntrega cita) throws SQLException {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }
        String sql = "{call modificar_cita_entrega(?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", cita.getIdCita());
            cmd.setTimestamp("p_fecha_hora", Timestamp.valueOf(cita.getFechaHora()));
            cmd.setString("p_estado", cita.getEstado().name());
            cmd.setInt("p_id_punto_entrega", cita.getPuntoEntrega().getIdPuntoEntrega());
            cmd.setInt("p_id_transaccion", cita.getTransaccion().getIdTransaccion());
            cmd.setBoolean("p_activo", cita.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(cita.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCita) throws SQLException {
        String sql = "{call eliminar_cita_entrega(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCita);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CitaEntrega findById(int idCita) throws SQLException {
        String sql = "{call buscar_cita_entrega_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCita);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CitaEntrega()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<CitaEntrega> findAll() throws SQLException {
        String sql = "{call listar_citas_entrega()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<CitaEntrega> citas = new ArrayList<>();
            while (rs.next()) {
                citas.add(mapear(rs, new CitaEntrega()));
            }
            return citas;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public CitaEntrega obtenerPorTransaccion(int idTransaccion) throws SQLException {
        String sql = "{call buscar_cita_por_transaccion(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_transaccion", idTransaccion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new CitaEntrega()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int cambiarEstado(int idCita, EstadoCita estado) throws SQLException {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        String sql = "{call cambiar_estado_cita(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCita);
            cmd.setString("p_estado", estado.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected CitaEntrega mapear(ResultSet rs, CitaEntrega cita) throws SQLException {
        super.mapear(rs, cita);
        cita.setIdCita(rs.getInt("id_cita"));
        cita.setFechaHora(leerFechaHora(rs, "fecha_hora"));
        cita.setEstado(EstadoCita.valueOf(rs.getString("estado")));

        PuntoEntrega punto = new PuntoEntrega();
        punto.setIdPuntoEntrega(rs.getInt("id_punto_entrega"));
        punto.setNombre(rs.getString("punto_nombre"));
        punto.setActivo(rs.getBoolean("punto_activo"));
        cita.setPuntoEntrega(punto);

        Transaccion transaccion = new Transaccion();
        transaccion.setIdTransaccion(rs.getInt("id_transaccion"));
        transaccion.setEstado(EstadoTransaccion.valueOf(rs.getString("transaccion_estado")));
        cita.setTransaccion(transaccion);
        return cita;
    }
}
