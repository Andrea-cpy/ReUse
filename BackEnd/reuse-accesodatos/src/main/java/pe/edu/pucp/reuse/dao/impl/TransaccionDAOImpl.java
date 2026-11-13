package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.TransaccionDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoCita;
import pe.edu.pucp.reuse.modelo.enums.EstadoOferta;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class TransaccionDAOImpl extends RegistroDAOImpl<Transaccion> implements TransaccionDAO {

    // fecha_inicio no se envia: la asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).
    @Override
    public int insert(Transaccion transaccion) throws SQLException {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transaccion no puede ser nula");
        }
        String sql = "{call insertar_transaccion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_estado", transaccion.getEstado().name());
            cmd.setBoolean("p_confirmacion_comprador", transaccion.isConfirmacionComprador());
            cmd.setBoolean("p_confirmacion_vendedor", transaccion.isConfirmacionVendedor());
            cmd.setInt("p_id_anuncio", transaccion.getAnuncio().getIdAnuncio());
            cmd.setInt("p_id_comprador", transaccion.getComprador().getIdUsuario());
            if (transaccion.getOferta() != null) {
                cmd.setInt("p_id_oferta", transaccion.getOferta().getIdOferta());
            } else {
                cmd.setNull("p_id_oferta", Types.INTEGER);
            }
            cmd.setBoolean("p_activo", transaccion.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(transaccion.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            transaccion.setIdTransaccion(cmd.getInt("p_id"));
            return transaccion.getIdTransaccion();
        } finally {
            cerrarConexion(conn);
        }
    }

    // fecha_fin no se modifica aqui: solo la registra finalizar() con la hora de la base de datos.
    @Override
    public int update(Transaccion transaccion) throws SQLException {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transaccion no puede ser nula");
        }
        String sql = "{call modificar_transaccion(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", transaccion.getIdTransaccion());
            cmd.setString("p_estado", transaccion.getEstado().name());
            cmd.setBoolean("p_confirmacion_comprador", transaccion.isConfirmacionComprador());
            cmd.setBoolean("p_confirmacion_vendedor", transaccion.isConfirmacionVendedor());
            cmd.setInt("p_id_anuncio", transaccion.getAnuncio().getIdAnuncio());
            cmd.setInt("p_id_comprador", transaccion.getComprador().getIdUsuario());
            if (transaccion.getOferta() != null) {
                cmd.setInt("p_id_oferta", transaccion.getOferta().getIdOferta());
            } else {
                cmd.setNull("p_id_oferta", Types.INTEGER);
            }
            cmd.setBoolean("p_activo", transaccion.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(transaccion.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idTransaccion) throws SQLException {
        String sql = "{call eliminar_transaccion(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idTransaccion);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Transaccion findById(int idTransaccion) throws SQLException {
        String sql = "{call buscar_transaccion_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idTransaccion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Transaccion()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Transaccion> findAll() throws SQLException {
        String sql = "{call listar_transacciones()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Transaccion> transacciones = new ArrayList<>();
            while (rs.next()) {
                transacciones.add(mapear(rs, new Transaccion()));
            }
            return transacciones;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int cambiarEstado(int idTransaccion, EstadoTransaccion estado) throws SQLException {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        String sql = "{call cambiar_estado_transaccion(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idTransaccion);
            cmd.setString("p_estado", estado.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int finalizar(int idTransaccion, EstadoTransaccion estadoFinal) throws SQLException {
        if (estadoFinal != EstadoTransaccion.COMPLETADA && estadoFinal != EstadoTransaccion.CANCELADA) {
            throw new IllegalArgumentException("Una transaccion solo finaliza COMPLETADA o CANCELADA");
        }
        String sql = "{call finalizar_transaccion(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idTransaccion);
            cmd.setString("p_estado", estadoFinal.name());
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int registrarConfirmacion(int idTransaccion, boolean delComprador) throws SQLException {
        String sql = "{call registrar_confirmacion_transaccion(?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idTransaccion);
            cmd.setBoolean("p_del_comprador", delComprador);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Transaccion> listarPorAnuncio(int idAnuncio) throws SQLException {
        String sql = "{call listar_transacciones_por_anuncio(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_anuncio", idAnuncio);
            try (ResultSet rs = cmd.executeQuery()) {
                ArrayList<Transaccion> transacciones = new ArrayList<>();
                while (rs.next()) {
                    transacciones.add(mapear(rs, new Transaccion()));
                }
                return transacciones;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Transaccion mapear(ResultSet rs, Transaccion transaccion) throws SQLException {
        super.mapear(rs, transaccion);
        transaccion.setIdTransaccion(rs.getInt("id_transaccion"));
        transaccion.setFechaInicio(leerFechaHora(rs, "fecha_inicio"));
        transaccion.setFechaFin(leerFechaHora(rs, "fecha_fin"));
        transaccion.setEstado(EstadoTransaccion.valueOf(rs.getString("estado")));
        transaccion.setConfirmacionComprador(rs.getBoolean("confirmacion_comprador"));
        transaccion.setConfirmacionVendedor(rs.getBoolean("confirmacion_vendedor"));
        transaccion.setAnuncio(mapearAnuncioReferencia(rs));
        transaccion.setComprador(mapearUsuarioReferencia(rs, "comprador"));

        int idOferta = rs.getInt("id_oferta");
        if (!rs.wasNull()) {
            Oferta oferta = new Oferta();
            oferta.setIdOferta(idOferta);
            oferta.setMontoPropuesto(rs.getDouble("oferta_monto"));
            oferta.setEstado(EstadoOferta.valueOf(rs.getString("oferta_estado")));
            transaccion.setOferta(oferta);
        }

        int idCita = rs.getInt("id_cita");
        if (!rs.wasNull()) {
            CitaEntrega cita = new CitaEntrega();
            cita.setIdCita(idCita);
            cita.setFechaHora(leerFechaHora(rs, "cita_fecha_hora"));
            cita.setEstado(EstadoCita.valueOf(rs.getString("cita_estado")));
            transaccion.setCitaEntrega(cita);
        }
        return transaccion;
    }
}
