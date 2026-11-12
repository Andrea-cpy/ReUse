package pe.edu.pucp.reuse.bl;

import java.util.Collection;
import java.util.Map;

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

    /** Varias metricas de una vez (se calculan en paralelo). */
    Map<TipoMetricaInsignia, Double> obtenerMetricas(int idUsuario,
            Collection<TipoMetricaInsignia> metricas) throws BLException;

    /**
     * RF-01 (recuperacion de contrasena): genera un codigo de 6 digitos valido por 15 minutos
     * para el correo indicado y lo devuelve para que la capa de servicios lo entregue.
     */
    String solicitarRecuperacion(String correoInstitucional) throws BLException;

    /** RF-01: valida el codigo de recuperacion y guarda la nueva contrasena (hash BCrypt). */
    void restablecerContrasena(String correoInstitucional, String codigo, String contrasenaNueva) throws BLException;
}
