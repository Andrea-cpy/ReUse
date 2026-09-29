package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.enums.NivelReputacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public interface UsuarioBL extends RegistroBL<UsuarioPUCP> {

    UsuarioPUCP obtenerPorCorreo(String correoInstitucional) throws BLException;

    // RF-01: la cuenta pasa de PENDIENTE_VERIFICACION a ACTIVA.
    void verificarCuenta(int idUsuario) throws BLException;

    // RF-01: suspension manual de una cuenta.
    void suspenderCuenta(int idUsuario) throws BLException;

    // RF-18: reputacion = promedio de las calificaciones recibidas.
    void recalcularReputacion(int idUsuario) throws BLException;

    // RF-18: nivel segun transacciones completadas y calificacion promedio.
    NivelReputacion obtenerNivelReputacion(int idUsuario) throws BLException;

    double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws BLException;
}
