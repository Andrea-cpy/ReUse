package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CalificacionDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.enums.TipoCalificacion;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class CalificacionDAOImpl extends RegistroDAOImpl<Calificacion> implements CalificacionDAO {

    private static final String SELECT_BASE = """
            SELECT ca.id_calificacion, ca.puntaje, ca.comentario, ca.fecha, ca.tipo,
                   ca.id_transaccion, t.estado AS transaccion_estado,
                   cr.id_usuario AS calificador_id, cr.codigo_pucp AS calificador_codigo,
                   cr.nombres AS calificador_nombres, cr.apellido_paterno AS calificador_apellido,
                   cd.id_usuario AS calificado_id, cd.codigo_pucp AS calificado_codigo,
                   cd.nombres AS calificado_nombres, cd.apellido_paterno AS calificado_apellido,
                   ca.activo, ca.fecha_creacion, ca.fecha_modificacion, ca.usuario_creacion, ca.usuario_modificacion
            FROM calificacion ca
            JOIN transaccion t ON t.id_transaccion = ca.id_transaccion
            JOIN usuario cr ON cr.id_usuario = ca.id_calificador
            JOIN usuario cd ON cd.id_usuario = ca.id_calificado
            """;

    // La fecha no se envia: la asigna la base de datos.
    @Override
    public int insert(Calificacion calificacion) throws SQLException {
        if (calificacion == null) {
            throw new IllegalArgumentException("La calificacion no puede ser nula");
        }
        String sql = """
                INSERT INTO calificacion (puntaje, comentario, tipo, id_transaccion, id_calificador,
                                          id_calificado, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setInt(1, calificacion.getPuntaje());
            cmd.setString(2, calificacion.getComentario());
            cmd.setString(3, calificacion.getTipoCalificacion().name());
            cmd.setInt(4, calificacion.getTransaccion().getIdTransaccion());
            cmd.setInt(5, calificacion.getCalificador().getIdUsuario());
            cmd.setInt(6, calificacion.getCalificado().getIdUsuario());
            cmd.setBoolean(7, calificacion.isActivo());
            cmd.setString(8, usuarioAuditoria(calificacion.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert la calificacion");
            }
            calificacion.setIdCalificacion(leerIdGenerado(cmd));
            return calificacion.getIdCalificacion();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int update(Calificacion calificacion) throws SQLException {
        if (calificacion == null) {
            throw new IllegalArgumentException("La calificacion no puede ser nula");
        }
        String sql = """
                UPDATE calificacion
                SET puntaje = ?, comentario = ?, tipo = ?, id_transaccion = ?, id_calificador = ?,
                    id_calificado = ?, activo = ?, usuario_modificacion = ?
                WHERE id_calificacion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, calificacion.getPuntaje());
            cmd.setString(2, calificacion.getComentario());
            cmd.setString(3, calificacion.getTipoCalificacion().name());
            cmd.setInt(4, calificacion.getTransaccion().getIdTransaccion());
            cmd.setInt(5, calificacion.getCalificador().getIdUsuario());
            cmd.setInt(6, calificacion.getCalificado().getIdUsuario());
            cmd.setBoolean(7, calificacion.isActivo());
            cmd.setString(8, usuarioAuditoria(calificacion.getUsuarioModificacion()));
            cmd.setInt(9, calificacion.getIdCalificacion());
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCalificacion) throws SQLException {
        String sql = """
                UPDATE calificacion SET activo = 0 WHERE id_calificacion = ?
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCalificacion);
            return cmd.executeUpdate();
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Calificacion findById(int idCalificacion) throws SQLException {
        String sql = SELECT_BASE + "WHERE ca.id_calificacion = ?";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idCalificacion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Calificacion()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Calificacion> findAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE ca.activo = 1 ORDER BY ca.fecha DESC";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            ArrayList<Calificacion> calificaciones = new ArrayList<>();
            while (rs.next()) {
                calificaciones.add(mapear(rs, new Calificacion()));
            }
            return calificaciones;
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Calificacion obtenerPorTransaccionYCalificador(int idTransaccion, int idCalificador)
            throws SQLException {
        String sql = SELECT_BASE + "WHERE ca.id_transaccion = ? AND ca.id_calificador = ? AND ca.activo = 1";
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idTransaccion);
            cmd.setInt(2, idCalificador);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Calificacion()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected Calificacion mapear(ResultSet rs, Calificacion calificacion) throws SQLException {
        super.mapear(rs, calificacion);
        calificacion.setIdCalificacion(rs.getInt("id_calificacion"));
        calificacion.setPuntaje(rs.getInt("puntaje"));
        calificacion.setComentario(rs.getString("comentario"));
        calificacion.setFecha(leerFechaHora(rs, "fecha"));
        calificacion.setTipoCalificacion(TipoCalificacion.valueOf(rs.getString("tipo")));
        Transaccion transaccion = new Transaccion();
        transaccion.setIdTransaccion(rs.getInt("id_transaccion"));
        transaccion.setEstado(EstadoTransaccion.valueOf(rs.getString("transaccion_estado")));
        calificacion.setTransaccion(transaccion);
        calificacion.setCalificador(mapearUsuarioReferencia(rs, "calificador"));
        calificacion.setCalificado(mapearUsuarioReferencia(rs, "calificado"));
        return calificacion;
    }
}
