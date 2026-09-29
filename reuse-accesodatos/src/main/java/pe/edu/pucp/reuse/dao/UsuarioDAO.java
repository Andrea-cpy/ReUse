package pe.edu.pucp.reuse.dao;

import java.sql.SQLException;

import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public interface UsuarioDAO extends DAO<UsuarioPUCP> {

    UsuarioPUCP obtenerPorCorreo(String correoInstitucional) throws SQLException;

    UsuarioPUCP obtenerPorCodigo(String codigoPUCP) throws SQLException;

    int actualizarEstadoCuenta(int idUsuario, EstadoCuenta estadoCuenta) throws SQLException;

    // variacion: +1 al registrar un reporte contra el usuario, -1 al anularlo.
    int actualizarContadorReportes(int idUsuario, int variacion) throws SQLException;

    // Recalcula la reputacion como el promedio de las calificaciones recibidas.
    int recalcularReputacion(int idUsuario) throws SQLException;

    // Valor real de una metrica (RF-18), calculado con consultas de agregacion.
    double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws SQLException;
}
