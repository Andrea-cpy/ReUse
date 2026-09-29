package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import pe.edu.pucp.reuse.modelo.enums.EstadoRevisionReporte;
import pe.edu.pucp.reuse.modelo.moderacion.Reporte;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;

/**
 * Parte comun de ReporteUsuarioDAOImpl y ReporteAnuncioDAOImpl: la tabla
 * reporte (supertipo). Cada subtipo escribe ademas en su propia tabla, por
 * eso sus insert/update/delete requieren una transaccion de la BL.
 */
public abstract class ReporteDAOImpl<T extends Reporte> extends RegistroDAOImpl<T> {

    // fecha_registro no se envia: la asigna la base de datos. Todo reporte nace PENDIENTE.
    protected int insertarReporte(Connection conn, T reporte) throws SQLException {
        String sql = "{call insertar_reporte(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setString("p_descripcion", reporte.getDescripcion());
            cmd.setString("p_estado_revision", reporte.getEstadoRevision().name());
            cmd.setInt("p_id_denunciante", reporte.getDenunciante().getIdUsuario());
            cmd.setBoolean("p_activo", reporte.isActivo());
            cmd.setString("p_usuario_creacion", usuarioAuditoria(reporte.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            reporte.setIdReporte(cmd.getInt("p_id"));
            return reporte.getIdReporte();
        }
    }

    // El estado de revision no se modifica aqui: solo cambia con resolver().
    protected int modificarReporte(Connection conn, T reporte) throws SQLException {
        String sql = "{call modificar_reporte(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", reporte.getIdReporte());
            cmd.setString("p_descripcion", reporte.getDescripcion());
            cmd.setInt("p_id_denunciante", reporte.getDenunciante().getIdUsuario());
            cmd.setBoolean("p_activo", reporte.isActivo());
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(reporte.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        }
    }

    protected int eliminarReporte(Connection conn, int idReporte) throws SQLException {
        String sql = "{call eliminar_reporte(?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idReporte);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        }
    }

    /**
     * Sanciona o desestima un reporte PENDIENTE y registra al administrador y la
     * fecha de revision (hora de la base de datos). Devuelve 0 si ya estaba resuelto.
     */
    public int resolver(int idReporte, EstadoRevisionReporte estado, int idRevisor) throws SQLException {
        if (estado != EstadoRevisionReporte.SANCIONADO && estado != EstadoRevisionReporte.DESESTIMADO) {
            throw new IllegalArgumentException("Un reporte solo se resuelve SANCIONADO o DESESTIMADO");
        }
        String sql = "{call resolver_reporte(?, ?, ?, ?)}";
        Connection conn = abrirConexion();
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idReporte);
            cmd.setString("p_estado_revision", estado.name());
            cmd.setInt("p_id_revisor", idRevisor);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    protected T mapear(ResultSet rs, T reporte) throws SQLException {
        super.mapear(rs, reporte);
        reporte.setIdReporte(rs.getInt("id_reporte"));
        reporte.setDescripcion(rs.getString("descripcion"));
        reporte.setFechaRegistro(leerFechaHora(rs, "fecha_registro"));
        reporte.setEstadoRevision(EstadoRevisionReporte.valueOf(rs.getString("estado_revision")));
        reporte.setFechaRevision(leerFechaHora(rs, "fecha_revision"));
        reporte.setDenunciante(mapearUsuarioReferencia(rs, "denunciante"));

        int idRevisor = rs.getInt("revisor_id");
        if (!rs.wasNull()) {
            AdministradorPUCP revisor = new AdministradorPUCP();
            revisor.setIdUsuario(idRevisor);
            revisor.setCodigoPUCP(rs.getString("revisor_codigo"));
            revisor.setNombres(rs.getString("revisor_nombres"));
            revisor.setApellidoPaterno(rs.getString("revisor_apellido"));
            reporte.setRevisor(revisor);
        }
        return reporte;
    }
}
