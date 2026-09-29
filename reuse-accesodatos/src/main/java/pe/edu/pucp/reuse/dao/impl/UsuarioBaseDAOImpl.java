package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * Columnas y SQL de la tabla usuario, compartidos por UsuarioDAOImpl y
 * AdministradorDAOImpl (administrador es un subtipo de usuario).
 * La carrera y su facultad se traen con JOIN: la facultad se obtiene
 * navegando por la carrera, no se guarda en usuario.
 */
public abstract class UsuarioBaseDAOImpl<T extends UsuarioPUCP> extends RegistroDAOImpl<T> {

    protected static final String SELECT_USUARIO = """
            SELECT u.id_usuario, u.codigo_pucp, u.nombres, u.apellido_paterno, u.apellido_materno,
                   u.correo_institucional, u.contrasena, u.id_carrera, u.verificado, u.estado_cuenta,
                   u.reputacion, u.contador_reportes, u.foto_perfil, u.fecha_registro,
                   c.nombre AS carrera_nombre, c.id_facultad, f.nombre AS facultad_nombre,
                   u.activo, u.fecha_creacion, u.fecha_modificacion, u.usuario_creacion, u.usuario_modificacion
            FROM usuario u
            LEFT JOIN carrera c ON c.id_carrera = u.id_carrera
            LEFT JOIN facultad f ON f.id_facultad = c.id_facultad
            """;

    // reputacion, contador_reportes y fecha_registro no se envian: los asigna la base
    // de datos o los mantiene la capa de negocio con metodos propios.
    protected int insertarUsuario(Connection conn, T usuario) throws SQLException {
        String sql = """
                INSERT INTO usuario (codigo_pucp, nombres, apellido_paterno, apellido_materno,
                                     correo_institucional, contrasena, id_carrera, verificado,
                                     estado_cuenta, foto_perfil, activo, usuario_creacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatosUsuario(cmd, usuario);
            cmd.setString(12, usuarioAuditoria(usuario.getUsuarioCreacion()));
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insert el usuario");
            }
            usuario.setIdUsuario(leerIdGenerado(cmd));
            return usuario.getIdUsuario();
        }
    }

    protected int modificarUsuario(Connection conn, T usuario) throws SQLException {
        String sql = """
                UPDATE usuario
                SET codigo_pucp = ?, nombres = ?, apellido_paterno = ?, apellido_materno = ?,
                    correo_institucional = ?, contrasena = ?, id_carrera = ?, verificado = ?,
                    estado_cuenta = ?, foto_perfil = ?, activo = ?, usuario_modificacion = ?
                WHERE id_usuario = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            asignarDatosUsuario(cmd, usuario);
            cmd.setString(12, usuarioAuditoria(usuario.getUsuarioModificacion()));
            cmd.setInt(13, usuario.getIdUsuario());
            return cmd.executeUpdate();
        }
    }

    protected int eliminarUsuario(Connection conn, int idUsuario) throws SQLException {
        String sql = """
                UPDATE usuario SET activo = 0 WHERE id_usuario = ?
                """;
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, idUsuario);
            return cmd.executeUpdate();
        }
    }

    private void asignarDatosUsuario(PreparedStatement cmd, T usuario) throws SQLException {
        cmd.setString(1, usuario.getCodigoPUCP());
        cmd.setString(2, usuario.getNombres());
        cmd.setString(3, usuario.getApellidoPaterno());
        cmd.setString(4, usuario.getApellidoMaterno());
        cmd.setString(5, usuario.getCorreoInstitucional());
        cmd.setString(6, usuario.getContrasena());
        if (usuario.getCarrera() != null) {
            cmd.setInt(7, usuario.getCarrera().getIdCarrera());
        } else {
            cmd.setNull(7, Types.INTEGER);
        }
        cmd.setBoolean(8, usuario.isVerificado());
        cmd.setString(9, usuario.getEstadoCuenta().name());
        cmd.setString(10, usuario.getFotoPerfil());
        cmd.setBoolean(11, usuario.isActivo());
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
