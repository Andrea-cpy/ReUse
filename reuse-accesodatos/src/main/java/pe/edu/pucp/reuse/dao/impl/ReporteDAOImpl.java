package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import pe.edu.pucp.reuse.modelo.enums.EstadoRevisionReporte;
import pe.edu.pucp.reuse.modelo.moderacion.Reporte;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;

/**
 * Parte comun de ReporteUsuarioDAOImpl y ReporteAnuncioDAOImpl: la tabla
 * reporte (supertipo). Cada subtipo escribe ademas en su propia tabla, por
 * eso sus insert/update/delete requieren una transaccion de la BL.
 */
public abstract class ReporteDAOImpl<T extends Reporte> extends RegistroDAOImpl<T> {

    // Columnas del supertipo que deben ir en el SELECT de cada subtipo.
    protected static final String COLUMNAS_REPORTE = """
            r.id_reporte, r.descripcion, r.fecha_registro, r.estado_revision, r.fecha_revision,
            d.id_usuario AS denunciante_id, d.codigo_pucp AS denunciante_codigo,
            d.nombres AS denunciante_nombres, d.apellido_paterno AS denunciante_apellido,
            rv.id_usuario AS revisor_id, rv.codigo_pucp AS revisor_codigo,
            rv.nombres AS revisor_nombres, rv.apellido_paterno AS revisor_apellido,
            r.activo, r.fecha_creacion, r.fecha_modificacion, r.usuario_creacion, r.usuario_modificacion
            """;

    // fecha_registro no se envia: la asigna la base de datos. Todo reporte nace PENDIENTE.
    protected int insertarReporte(Connection conn, T reporte) throws SQLException {
        String sql = """
                INSERT INTO reporte (descripcion, estado_revision, id_denunciante, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cmd.setString(1, reporte.getDescripcion());
            cmd.setString(2, reporte.getEstadoRevision().name());
            cmd.setInt(3, reporte.getDenunciante().getIdUsuario());
            cmd.setBoolean(4, reporte.isActivo());
            cmd.setString(5, usuarioAuditoria(reporte.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el reporte");
            }
            reporte.setIdReporte(leerIdGenerado(cmd));
            return reporte.getIdReporte();
        }
    }

    // El estado de revision no se modifica aqui: solo cambia con resolver().
    protected int modificarReporte(Connection conn, T reporte) throws SQLException {
        String sql = """
                UPDATE reporte
                SET descripcion = ?, id_denunciante = ?, activo = ?, usuario_modificacion = ?
                WHERE id_reporte = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, reporte.getDescripcion());
            cmd.setInt(2, reporte.getDenunciante().getIdUsuario());
            cmd.setBoolean(3, reporte.isActivo());
            cmd.setString(4, usuarioAuditoria(reporte.getUsuarioModificacion()));
            cmd.setInt(5, reporte.getIdReporte());
            return cmd.executeUpdate();
        }
    }

    protected int eliminarReporte(Connection conn, int idReporte) throws SQLException {
        String sql = """
                UPDATE reporte SET activo = 0 WHERE id_reporte = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idReporte);
            return cmd.executeUpdate();
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
        String sql = """
                UPDATE reporte
                SET estado_revision = ?, fecha_revision = CURRENT_TIMESTAMP, id_revisor = ?
                WHERE id_reporte = ? AND estado_revision = 'PENDIENTE'
                """;
        Connection conn = abrirConexion();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, estado.name());
            cmd.setInt(2, idRevisor);
            cmd.setInt(3, idReporte);
            return cmd.executeUpdate();
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
