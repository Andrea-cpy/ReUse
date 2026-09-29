package pe.edu.pucp.reuse.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * Llamadas y mapeo de la tabla usuario, compartidos por UsuarioDAOImpl y
 * AdministradorDAOImpl (administrador es un subtipo de usuario).
 * Los procedimientos traen la carrera y su facultad con JOIN: la facultad se
 * obtiene navegando por la carrera, no se guarda en usuario.
 */
public abstract class UsuarioBaseDAOImpl<T extends UsuarioPUCP> extends RegistroDAOImpl<T> {

    // reputacion, contador_reportes y fecha_registro no se envian: los asigna la base
    // de datos o los mantiene la capa de negocio con procedimientos propios.
    protected int insertarUsuario(Connection conn, T usuario) throws SQLException {
        String sql = "{call insertar_usuario(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            asignarDatosUsuario(cmd, usuario);
            cmd.setString("p_usuario_creacion", usuarioAuditoria(usuario.getUsuarioCreacion()));
            cmd.registerOutParameter("p_id", Types.INTEGER);
            cmd.execute();
            usuario.setIdUsuario(cmd.getInt("p_id"));
            return usuario.getIdUsuario();
        }
    }

    protected int modificarUsuario(Connection conn, T usuario) throws SQLException {
        String sql = "{call modificar_usuario(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", usuario.getIdUsuario());
            asignarDatosUsuario(cmd, usuario);
            cmd.setString("p_usuario_modificacion", usuarioAuditoria(usuario.getUsuarioModificacion()));
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        }
    }

    protected int eliminarUsuario(Connection conn, int idUsuario) throws SQLException {
        String sql = "{call eliminar_usuario(?, ?)}";
        try (CallableStatement cmd = conn.prepareCall(sql)) {
            cmd.setInt("p_id", idUsuario);
            cmd.registerOutParameter("p_filas", Types.INTEGER);
            cmd.execute();
            return cmd.getInt("p_filas");
        }
    }

    private void asignarDatosUsuario(CallableStatement cmd, T usuario) throws SQLException {
        cmd.setString("p_codigo_pucp", usuario.getCodigoPUCP());
        cmd.setString("p_nombres", usuario.getNombres());
        cmd.setString("p_apellido_paterno", usuario.getApellidoPaterno());
        cmd.setString("p_apellido_materno", usuario.getApellidoMaterno());
        cmd.setString("p_correo_institucional", usuario.getCorreoInstitucional());
        cmd.setString("p_contrasena", usuario.getContrasena());
        if (usuario.getCarrera() != null) {
            cmd.setInt("p_id_carrera", usuario.getCarrera().getIdCarrera());
        } else {
            cmd.setNull("p_id_carrera", Types.INTEGER);
        }
        cmd.setBoolean("p_verificado", usuario.isVerificado());
        cmd.setString("p_estado_cuenta", usuario.getEstadoCuenta().name());
        cmd.setString("p_foto_perfil", usuario.getFotoPerfil());
        cmd.setBoolean("p_activo", usuario.isActivo());
    }

    @Override
    protected T mapear(ResultSet rs, T usuario) throws SQLException {
        super.mapear(rs, usuario);
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCodigoPUCP(rs.getString("codigo_pucp"));
        usuario.setNombres(rs.getString("nombres"));
        usuario.setApellidoPaterno(rs.getString("apellido_paterno"));
        usuario.setApellidoMaterno(rs.getString("apellido_materno"));
        usuario.setCorreoInstitucional(rs.getString("correo_institucional"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setVerificado(rs.getBoolean("verificado"));
        usuario.setEstadoCuenta(EstadoCuenta.valueOf(rs.getString("estado_cuenta")));
        usuario.setReputacion(rs.getDouble("reputacion"));
        usuario.setContadorReportes(rs.getInt("contador_reportes"));
        usuario.setFotoPerfil(rs.getString("foto_perfil"));
        usuario.setFechaRegistro(leerFechaHora(rs, "fecha_registro"));

        int idCarrera = rs.getInt("id_carrera");
        if (!rs.wasNull()) {
            Facultad facultad = new Facultad();
            facultad.setIdFacultad(rs.getInt("id_facultad"));
            facultad.setNombre(rs.getString("facultad_nombre"));
            Carrera carrera = new Carrera();
            carrera.setIdCarrera(idCarrera);
            carrera.setNombre(rs.getString("carrera_nombre"));
            carrera.setFacultad(facultad);
            usuario.setCarrera(carrera);
        }
        return usuario;
    }
}
