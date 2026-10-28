package pe.edu.pucp.reuse.bl;

import pe.edu.pucp.reuse.modelo.enums.NivelReputacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * update solo modifica los datos del perfil (nombres, apellidos, carrera y foto).
 * La contrasena, la verificacion y el estado de la cuenta se cambian con sus
 * metodos propios; el codigo PUCP y el correo no son editables.
 */
public interface UsuarioBL extends RegistroBL<UsuarioPUCP> {

    UsuarioPUCP obtenerPorCorreo(String correoInstitucional) throws BLException;

    // Compara la contrasena con el hash guardado (BCrypt.checkpw).
    UsuarioPUCP iniciarSesion(String correoInstitucional, String contrasena) throws BLException;

    void cambiarContrasena(int idUsuario, String contrasenaActual, String contrasenaNueva) throws BLException;

    void verificarCuenta(int idUsuario) throws BLException;

    void suspenderCuenta(int idUsuario) throws BLException;

    void reactivarCuenta(int idUsuario) throws BLException;

    void recalcularReputacion(int idUsuario) throws BLException;

    NivelReputacion obtenerNivelReputacion(int idUsuario) throws BLException;

    double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws BLException;
}
