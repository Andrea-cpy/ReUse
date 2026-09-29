package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import pe.edu.pucp.reuse.dao.CalificacionDAO;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.enums.TipoCalificacion;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class CalificacionDAOImpl extends RegistroDAOImpl<Calificacion> implements CalificacionDAO {

    // La fecha no se envia: la asigna la base de datos.
    @Override
    public int insert(Calificacion calificacion) throws SQLException {
        if (calificacion == null) {
            throw new IllegalArgumentException("La calificacion no puede ser nula");
        }
        String sql = "{call insertar_calificacion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_puntaje", calificacion.getPuntaje());
            cmd.setString("p_comentario", calificacion.getComentario());
            cmd.setString("p_tipo", calificacion.getTipoCalificacion().name());
            cmd.setInt("p_id_transaccion", calificacion.getTransaccion().getIdTransaccion());
            cmd.setInt("p_id_calificador", calificacion.getCalificador().getIdUsuario());
            cmd.setInt("p_id_calificado", calificacion.getCalificado().getIdUsuario());
            cmd.setBoolean("p_activo", calificacion.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(calificacion.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            calificacion.setIdCalificacion(cmd.getInt("p_id"));
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
        String sql = "{call modificar_calificacion(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", calificacion.getIdCalificacion());
            cmd.setInt("p_puntaje", calificacion.getPuntaje());
            cmd.setString("p_comentario", calificacion.getComentario());
            cmd.setString("p_tipo", calificacion.getTipoCalificacion().name());
            cmd.setInt("p_id_transaccion", calificacion.getTransaccion().getIdTransaccion());
            cmd.setInt("p_id_calificador", calificacion.getCalificador().getIdUsuario());
            cmd.setInt("p_id_calificado", calificacion.getCalificado().getIdUsuario());
            cmd.setBoolean("p_activo", calificacion.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(calificacion.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public int delete(int idCalificacion) throws SQLException {
        String sql = "{call eliminar_calificacion(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCalificacion);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Calificacion findById(int idCalificacion) throws SQLException {
        String sql = "{call buscar_calificacion_por_id(?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idCalificacion);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs, new Calificacion()) : null;
            }
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public ArrayList<Calificacion> findAll() throws SQLException {
        String sql = "{call listar_calificaciones()}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql);
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
        String sql = "{call buscar_calificacion_por_transaccion_y_calificador(?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id_transaccion", idTransaccion);
            cmd.setInt("p_id_calificador", idCalificador);
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
