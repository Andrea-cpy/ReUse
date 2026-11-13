package pe.edu.pucp.reuse.bl;

import java.util.Collection;
import java.util.Map;

import pe.edu.pucp.reuse.modelo.enums.NivelReputacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.CuentaGoogle;
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
     * y lo envia en segundo plano al correo institucional. Si el correo no esta configurado
     * (modo desarrollo), el codigo solo se registra en el log del servidor. El codigo nunca sale
     * de la capa de negocio. Si el correo no tiene una cuenta habilitada no hace nada (sin lanzar
     * error), para no revelar que correos estan registrados.
     */
    void solicitarRecuperacion(String correoInstitucional) throws BLException;

    /** RF-01: valida el codigo de recuperacion y guarda la nueva contrasena (hash BCrypt). */
    void restablecerContrasena(String correoInstitucional, String codigo, String contrasenaNueva) throws BLException;

    /**
     * Ingreso con Google, paso 1: valida el ID token con Google (firma, Client ID, emisor y
     * vencimiento, en reuse-integraciones), aplica las reglas de ReUse (correo verificado y cuenta
     * del dominio pucp.edu.pe) y devuelve la identidad confirmada.
     */
    CuentaGoogle validarCuentaGoogle(String idToken) throws BLException;

    /**
     * Ingreso con Google, paso 2: devuelve el usuario con ese correo, o null si aun no tiene cuenta.
     * Una cuenta PENDIENTE_VERIFICACION queda verificada: Google ya confirmo el correo PUCP.
     */
    UsuarioPUCP iniciarSesionConGoogle(CuentaGoogle cuenta) throws BLException;

    /**
     * Registro con Google: el correo es el que confirmo Google y la cuenta nace ACTIVA y
     * verificada, sin revision del administrador. Se validan los mismos datos que en el registro
     * con formulario, incluida la contrasena que elige el estudiante.
     */
    int registrarConGoogle(UsuarioPUCP usuario, CuentaGoogle cuenta) throws BLException;
}
