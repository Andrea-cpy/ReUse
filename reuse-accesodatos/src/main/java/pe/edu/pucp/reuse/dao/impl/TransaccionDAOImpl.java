package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    // La cita se trae con LEFT JOIN: cita_entrega.id_transaccion es UNIQUE (a lo mas una cita).
    private static final String SELECT_BASE = """
            SELECT t.id_transaccion, t.fecha_inicio, t.fecha_fin, t.estado,
                   t.confirmacion_comprador, t.confirmacion_vendedor,
                   a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
                   a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
                   u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
                   u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
                   t.id_oferta, o.monto_propuesto AS oferta_monto, o.estado AS oferta_estado,
                   ce.id_cita, ce.fecha_hora AS cita_fecha_hora, ce.estado AS cita_estado,
                   t.activo, t.fecha_creacion, t.fecha_modificacion, t.usuario_creacion, t.usuario_modificacion
            FROM transaccion t
            JOIN anuncio a ON a.id_anuncio = t.id_anuncio
            JOIN usuario u ON u.id_usuario = t.id_comprador
            LEFT JOIN oferta o ON o.id_oferta = t.id_oferta
            LEFT JOIN cita_entrega ce ON ce.id_transaccion = t.id_transaccion AND ce.activo = 1
            """;

    // fecha_inicio no se envia: la asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).
    @Override
    public int insert(Transaccion transaccion) throws SQLException {
        if (transaccion == null) {
            throw new IllegalArgumentException("La transaccion no puede ser nula");
        }
        String sql = """
                INSERT INTO transaccion (estado, confirmacion_comprador, confirmacion_vendedor, id_anuncio,
                                         id_comprador, id_oferta, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, transaccion.getEstado().name());
            cmd.setBoolean(2, transaccion.isConfirmacionComprador());
            cmd.setBoolean(3, transaccion.isConfirmacionVendedor());
            cmd.setInt(4, transaccion.getAnuncio().getIdAnuncio());
            cmd.setInt(5, transaccion.getComprador().getIdUsuario());
            if (transaccion.getOferta() != null) {
                cmd.setInt(6, transaccion.getOferta().getIdOferta());
            } else {
                cmd.setNull(6, Types.INTEGER);
            }
            cmd.setBoolean(7, transaccion.isActivo());
            cmd.setString(8, usuarioAuditoria(transaccion.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la transaccion");
            }
            transaccion.setIdTransaccion(leerIdGenerado(cmd));
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
        String sql = """
                UPDATE transaccion
                SET estado = ?, confirmacion_comprador = ?, confirmacion_vendedor = ?, id_anuncio = ?,
                    id_comprador = ?, id_oferta = ?, activo = ?, usuario_modificacion = ?
                WHERE id_transaccion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, transaccion.getEstado().name());
            cmd.setBoolean(2, transaccion.isConfirmacionComprador());
            cmd.setBoolean(3, transaccion.isConfirmacionVendedor());
            cmd.setInt(4, transaccion.getAnuncio().getIdAnuncio());
            cmd.setInt(5, transaccion.getComprador().getIdUsuario());
            if (transaccion.getOferta() != null) {
                cmd.setInt(6, transaccion.getOferta().getIdOferta());
            } else {
                cmd.setNull(6, Types.INTEGER);
            }
            cmd.setBoolean(7, transaccion.isActivo());
            cmd.setString(8, usuarioAuditoria(transaccion.getUsuarioModificacion()));
            cmd.setInt(9, transaccion.getIdTransaccion());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idTransaccion) throws SQLException {
        String sql = """
                UPDATE transaccion SET activo = 0 WHERE id_transaccion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idTransaccion);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Transaccion findById(int idTransaccion) throws SQLException {
        String sql = SELECT_BASE + "WHERE t.id_transaccion = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idTransaccion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Transaccion()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Transaccion> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE t.activo = 1 ORDER BY t.fecha_inicio DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
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
        String sql = """
                UPDATE transaccion SET estado = ? WHERE id_transaccion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estado.name());
            cmd.setInt(2, idTransaccion);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int finalizar(int idTransaccion, EstadoTransaccion estadoFinal) throws SQLException {
        if (estadoFinal != EstadoTransaccion.COMPLETADA && estadoFinal != EstadoTransaccion.CANCELADA) {
            throw new IllegalArgumentException("Una transaccion solo finaliza COMPLETADA o CANCELADA");
        }
        String sql = """
                UPDATE transaccion SET estado = ?, fecha_fin = CURRENT_TIMESTAMP WHERE id_transaccion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estadoFinal.name());
            cmd.setInt(2, idTransaccion);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int registrarConfirmacion(int idTransaccion, boolean delComprador) throws SQLException {
        String sql = delComprador
                ? "UPDATE transaccion SET confirmacion_comprador = 1 WHERE id_transaccion = ?"
                : "UPDATE transaccion SET confirmacion_vendedor = 1 WHERE id_transaccion = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idTransaccion);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Transaccion> listarPorAnuncio(int idAnuncio) throws SQLException {
        String sql = SELECT_BASE + "WHERE t.id_anuncio = ? AND t.activo = 1 ORDER BY t.fecha_inicio";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idAnuncio);
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
