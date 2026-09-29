package pe.edu.pucp.reuse.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.db.DBManager;
import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * Base de todos los DAO: mapea las columnas de borrado logico y auditoria y
 * decide que conexion usar. Todos los DAO invocan procedimientos almacenados
 * ({call ...}) con CallableStatement; el SQL vive en la base de datos.
 * <p>
 * Si la capa de negocio abrio una transaccion (TransactionsManager.iniciar()),
 * el DAO usa esa misma conexion y NO la cierra: la cierra TransactionsManager
 * al hacer commit o rollback. Si no hay transaccion, el DAO abre y cierra su
 * propia conexion (autocommit).
 */
public abstract class RegistroDAOImpl<T extends Registro> {

    protected static final String USUARIO_SISTEMA = "SISTEMA";

    protected Connection abrirConexion() throws SQLException {
        if (TransactionsManager.activa()) {
            return TransactionsManager.getConnection();
        }
        return DBManager.getInstance().getConnection();
    }

    protected void cerrarConexion(Connection conn) throws SQLException {
        if (conn != null && !TransactionsManager.activa()) {
            conn.close();
        }
    }

    protected T mapear(ResultSet rs, T registro) throws SQLException {
        registro.setActivo(rs.getBoolean("activo"));
        registro.setFechaCreacion(leerFechaHora(rs, "fecha_creacion"));
        registro.setFechaModificacion(leerFechaHora(rs, "fecha_modificacion"));
        registro.setUsuarioCreacion(rs.getString("usuario_creacion"));
        registro.setUsuarioModificacion(rs.getString("usuario_modificacion"));
        return registro;
    }

    protected static LocalDateTime leerFechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp valor = rs.getTimestamp(columna);
        return valor != null ? valor.toLocalDateTime() : null;
    }

    protected static String usuarioAuditoria(String usuario) {
        return (usuario == null || usuario.isBlank()) ? USUARIO_SISTEMA : usuario;
    }

    /**
     * Usuario referenciado por una FK. El procedimiento debe devolver, con JOIN a usuario:
     * prefijo_id, prefijo_codigo, prefijo_nombres y prefijo_apellido.
     */
    protected static UsuarioPUCP mapearUsuarioReferencia(ResultSet rs, String prefijo) throws SQLException {
        int idUsuario = rs.getInt(prefijo + "_id");
        if (rs.wasNull()) {
            return null;
        }
        UsuarioPUCP usuario = new UsuarioPUCP();
        usuario.setIdUsuario(idUsuario);
        usuario.setCodigoPUCP(rs.getString(prefijo + "_codigo"));
        usuario.setNombres(rs.getString(prefijo + "_nombres"));
        usuario.setApellidoPaterno(rs.getString(prefijo + "_apellido"));
        return usuario;
    }

    /**
     * Anuncio referenciado por una FK. El procedimiento debe devolver, con JOIN a anuncio:
     * anuncio_id, anuncio_titulo, anuncio_precio, anuncio_estado y anuncio_id_vendedor.
     */
    protected static Anuncio mapearAnuncioReferencia(ResultSet rs) throws SQLException {
        Anuncio anuncio = new Anuncio();
        anuncio.setIdAnuncio(rs.getInt("anuncio_id"));
        anuncio.setTitulo(rs.getString("anuncio_titulo"));
        anuncio.setPrecio(rs.getDouble("anuncio_precio"));
        anuncio.setEstado(EstadoAnuncio.valueOf(rs.getString("anuncio_estado")));
        UsuarioPUCP vendedor = new UsuarioPUCP();
        vendedor.setIdUsuario(rs.getInt("anuncio_id_vendedor"));
        anuncio.setVendedor(vendedor);
        return anuncio;
    }
}
