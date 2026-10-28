package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public interface UsuarioDAO extends DAO<UsuarioPUCP> {

    UsuarioPUCP obtenerPorCorreo(String correoInstitucional) throws SQLException;

    UsuarioPUCP obtenerPorCodigo(String codigoPUCP) throws SQLException;

    int actualizarEstadoCuenta(int idUsuario, EstadoCuenta estadoCuenta) throws SQLException;

    // Marca la cuenta como verificada y ACTIVA.
    int verificarCuenta(int idUsuario) throws SQLException;

    // hash: contrasena ya hasheada con BCrypt en la capa de negocio.
    int cambiarContrasena(int idUsuario, String hash) throws SQLException;

    // Anuncios DISPONIBLE o RESERVADO publicados por el usuario.
    int contarAnunciosVigentes(int idUsuario) throws SQLException;

    // Transacciones EN_NEGOCIACION o CITA_CONFIRMADA como comprador o vendedor.
    int contarTransaccionesEnCurso(int idUsuario) throws SQLException;

    // variacion: +1 al registrar un reporte contra el usuario, -1 al anularlo.
    int actualizarContadorReportes(int idUsuario, int variacion) throws SQLException;

    // Recalcula la reputacion como el promedio de las calificaciones recibidas.
    int recalcularReputacion(int idUsuario) throws SQLException;

    // Valor real de una metrica, calculado con consultas de agregacion.
    double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws SQLException;
}
