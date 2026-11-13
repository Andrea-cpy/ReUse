package pe.edu.pucp.reuse.bl.impl;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.UsuarioBL;
import pe.edu.pucp.reuse.bl.insignias.MetricasUsuario;
import pe.edu.pucp.reuse.dao.CarreraDAO;
import pe.edu.pucp.reuse.dao.impl.CarreraDAOImpl;
import pe.edu.pucp.reuse.integraciones.IntegracionException;
import pe.edu.pucp.reuse.integraciones.correo.ServicioCorreo;
import pe.edu.pucp.reuse.integraciones.google.DatosTokenGoogle;
import pe.edu.pucp.reuse.integraciones.google.VerificadorTokenGoogle;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.enums.NivelReputacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.CuentaGoogle;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class UsuarioBLImpl extends BaseBLImpl implements UsuarioBL {
    private static final Logger LOG = Logger.getLogger(UsuarioBLImpl.class.getName());

    // Ingreso con Google: solo cuentas del Google Workspace de la PUCP.
    private static final String DOMINIO_PUCP = "pucp.edu.pe";
    // Correo de alumno PUCP: "a" + codigo de 8 digitos (a20221234@pucp.edu.pe).
    private static final Pattern CORREO_ALUMNO = Pattern.compile("^a(\\d{8})@pucp\\.edu\\.pe$");

    // Solo correos institucionales @pucp.edu.pe.
    private static final Pattern PATRON_CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@pucp\\.edu\\.pe$");
    private static final Pattern PATRON_CODIGO = Pattern.compile("^\\d{8}$");
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    // BCrypt ignora lo que pase de 72 bytes: dos claves que solo difieren despues darian el mismo hash.
    private static final int LONGITUD_MAXIMA_CONTRASENA = 72;
    // Mismo mensaje si el correo no existe o la clave no coincide: no revela que correos estan registrados.
    private static final String CREDENCIALES_INVALIDAS = "Correo o contrasena incorrectos";

    // Recuperacion de contrasena: codigo de 6 digitos, 15 minutos de validez y 5 intentos.
    private static final int MINUTOS_VALIDEZ_CODIGO = 15;
    private static final int INTENTOS_MAXIMOS_CODIGO = 5;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    // Solo se guarda el hash del codigo (como con las contrasenas). Vive en memoria del servidor:
    // un reinicio invalida los codigos pendientes, que es aceptable para un codigo de 15 minutos.
    private static final Map<String, SolicitudRecuperacion> RECUPERACIONES = new ConcurrentHashMap<>();

    private final CarreraDAO carreraDAO = new CarreraDAOImpl();

    private static final class SolicitudRecuperacion {
        private final String hashCodigo;
        private final LocalDateTime vence;
        private int intentos;

        private SolicitudRecuperacion(String hashCodigo, LocalDateTime vence) {
            this.hashCodigo = hashCodigo;
            this.vence = vence;
        }
    }

    @Override
    public void solicitarRecuperacion(String correoInstitucional) throws BLException {
        UsuarioPUCP usuario = obtenerPorCorreo(correoInstitucional);
        // Sin cuenta (o suspendida) no se genera codigo, pero tampoco se avisa: si la respuesta
        // fuera distinta, cualquiera podria averiguar que correos estan registrados.
        if (usuario == null || !usuario.isActivo() || usuario.getEstadoCuenta() == EstadoCuenta.SUSPENDIDA) {
            return;
        }
        String correo = usuario.getCorreoInstitucional().toLowerCase();
        String codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
        RECUPERACIONES.put(correo, new SolicitudRecuperacion(
                hashear(codigo), LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ_CODIGO)));
        // El codigo solo llega al dueno del correo. Se envia en segundo plano: la respuesta no tarda
        // mas cuando la cuenta existe, asi que tampoco revela que correos estan registrados.
        ServicioCorreo servicioCorreo = ServicioCorreo.getInstance();
        if (servicioCorreo.estaConfigurado()) {
            servicioCorreo.enviarCodigoRecuperacion(correo, codigo, MINUTOS_VALIDEZ_CODIGO);
        } else {
            // Modo desarrollo (sin correo.properties): el codigo queda en el log del servidor.
            LOG.info("Correo sin configurar. Codigo de recuperacion para " + correo + ": " + codigo);
        }
    }

    @Override
    public void restablecerContrasena(String correoInstitucional, String codigo, String contrasenaNueva)
            throws BLException {
        validarTexto(correoInstitucional, "Debe indicar el correo institucional");
        validarTexto(codigo, "Debe indicar el codigo de recuperacion");
        String clave = correoInstitucional.trim().toLowerCase();
        SolicitudRecuperacion solicitud = RECUPERACIONES.get(clave);
        if (solicitud == null) {
            throw new BLException("No hay una solicitud de recuperacion vigente para este correo; solicita un codigo nuevo");
        }
        if (LocalDateTime.now().isAfter(solicitud.vence)) {
            RECUPERACIONES.remove(clave);
            throw new BLException("El codigo vencio (dura " + MINUTOS_VALIDEZ_CODIGO + " minutos); solicita uno nuevo");
        }
        if (!coincide(codigo.trim(), solicitud.hashCodigo)) {
            solicitud.intentos++;
            if (solicitud.intentos >= INTENTOS_MAXIMOS_CODIGO) {
                RECUPERACIONES.remove(clave);
                throw new BLException("Codigo incorrecto. Se agotaron los intentos; solicita un codigo nuevo");
            }
            throw new BLException("Codigo incorrecto. Te quedan " + (INTENTOS_MAXIMOS_CODIGO - solicitud.intentos)
                    + " intento(s)");
        }
        validarContrasena(contrasenaNueva);
        UsuarioPUCP usuario = obtenerPorCorreo(clave);
        if (usuario == null || !usuario.isActivo()) {
            throw new BLException("La cuenta no existe o ya no esta disponible");
        }
        try {
            usuarioDAO.cambiarContrasena(usuario.getIdUsuario(), hashear(contrasenaNueva));
        } catch (SQLException e) {
            throw new BLException("No se pudo guardar la nueva contrasena", e);
        }
        RECUPERACIONES.remove(clave); // el codigo es de un solo uso
    }

    @Override
    public CuentaGoogle validarCuentaGoogle(String idToken) throws BLException {
        if (idToken == null || idToken.isBlank()) {
            throw new BLException("Falta el token de Google");
        }
        DatosTokenGoogle datos;
        try {
            datos = VerificadorTokenGoogle.getInstance().verificar(idToken);
        } catch (IntegracionException e) {
            // El detalle tecnico viaja como causa (va al log); el usuario solo ve el mensaje.
            throw new BLException(mensajeErrorGoogle(e), e);
        }
        if (datos.getCorreo() == null) {
            throw new BLException("El token de Google tiene un formato inesperado");
        }
        String correo = datos.getCorreo().trim().toLowerCase(Locale.ROOT);
        // hd solo existe en cuentas administradas por la PUCP (Google Workspace). Una cuenta
        // Google personal creada con un correo @pucp.edu.pe no lo trae y se rechaza.
        if (!correo.endsWith("@" + DOMINIO_PUCP) || !DOMINIO_PUCP.equalsIgnoreCase(datos.getHd())) {
            throw new BLException("Solo puedes ingresar con tu cuenta Google @" + DOMINIO_PUCP
                    + ". Elegiste " + correo + ".");
        }
        if (!Boolean.TRUE.equals(datos.getEmailVerificado())) {
            throw new BLException("Google no ha verificado el correo " + correo);
        }
        Matcher alumno = CORREO_ALUMNO.matcher(correo);
        String codigoPUCP = alumno.matches() ? alumno.group(1) : null;
        return new CuentaGoogle(correo, codigoPUCP, datos.getNombres(), datos.getApellidos());
    }

    // Mensajes que ve el usuario segun el motivo del fallo al validar con Google.
    private static String mensajeErrorGoogle(IntegracionException e) {
        return switch (e.getMotivo()) {
            case SIN_CONFIGURAR -> "El ingreso con Google no esta disponible por ahora. Usa tu correo y contrasena.";
            case EMISOR_DESCONOCIDO -> "El token no fue emitido por Google";
            case FORMATO_INESPERADO -> "El token de Google tiene un formato inesperado";
            case TOKEN_RECHAZADO -> "No se pudo validar tu identidad con Google; vuelve a intentarlo";
        };
    }

    @Override
    public UsuarioPUCP iniciarSesionConGoogle(CuentaGoogle cuenta) throws BLException {
        if (cuenta == null) {
            throw new BLException("Debe validar primero la cuenta de Google");
        }
        UsuarioPUCP usuario = obtenerPorCorreo(cuenta.getCorreo());
        if (usuario == null) {
            return null; // aun no tiene cuenta: el frontend le pide completar el registro
        }
        if (!usuario.isActivo()) {
            throw new BLException("La cuenta asociada a " + cuenta.getCorreo()
                    + " fue dada de baja; comunicate con un administrador de ReUse");
        }
        if (usuario.getEstadoCuenta() == EstadoCuenta.SUSPENDIDA) {
            throw new BLException("La cuenta " + usuario.getCodigoPUCP() + " esta SUSPENDIDA");
        }
        // Se registro con contrasena y esperaba al administrador: Google ya confirmo que el correo es suyo.
        if (usuario.getEstadoCuenta() == EstadoCuenta.PENDIENTE_VERIFICACION) {
            try {
                usuarioDAO.verificarCuenta(usuario.getIdUsuario());
            } catch (SQLException e) {
                throw new BLException("No se pudo verificar la cuenta", e);
            }
            usuario = findById(usuario.getIdUsuario());
        }
        return usuario;
    }

    @Override
    public int registrarConGoogle(UsuarioPUCP usuario, CuentaGoogle cuenta) throws BLException {
        if (cuenta == null) {
            throw new BLException("Debe validar primero la cuenta de Google");
        }
        if (usuario == null) {
            throw new BLException("El usuario no puede ser nulo");
        }
        // El correo no lo elige el usuario: es el que Google confirmo.
        usuario.setCorreoInstitucional(cuenta.getCorreo());
        String codigoDelCorreo = cuenta.getCodigoPUCP();
        if (codigoDelCorreo != null && usuario.getCodigoPUCP() != null
                && !codigoDelCorreo.equals(usuario.getCodigoPUCP().trim())) {
            throw new BLException("El codigo PUCP debe ser " + codigoDelCorreo + ", el de tu correo " + cuenta.getCorreo());
        }
        if (usuario.getCodigoPUCP() == null || usuario.getCodigoPUCP().isBlank()) {
            usuario.setCodigoPUCP(codigoDelCorreo);
        }
        // Google ya confirmo que el correo PUCP es suyo: la cuenta no espera al administrador.
        return registrar(usuario, true);
    }

    // Registro con formulario: la cuenta queda pendiente hasta que un administrador la verifique.
    @Override
    public int insert(UsuarioPUCP usuario) throws BLException {
        return registrar(usuario, false);
    }

    /**
     * Crea el mismo usuario por los dos caminos (formulario o Google): mismas validaciones y
     * contrasena elegida por el estudiante (hash BCrypt). Solo cambia el estado inicial:
     * verificado = true deja la cuenta ACTIVA; false, PENDIENTE_VERIFICACION.
     */
    private int registrar(UsuarioPUCP usuario, boolean verificado) throws BLException {
        validarDatosPersonales(usuario);
        validarContrasena(usuario.getContrasena());
        validarCarrera(usuario);
        validarUnicidad(usuario);
        usuario.setVerificado(verificado);
        usuario.setEstadoCuenta(verificado ? EstadoCuenta.ACTIVA : EstadoCuenta.PENDIENTE_VERIFICACION);
        usuario.setContrasena(hashear(usuario.getContrasena()));
        try {
            return usuarioDAO.insert(usuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el usuario", e);
        }
    }

    // Solo el perfil: el procedimiento ignora codigo, correo, contrasena, verificacion, estado y activo.
    @Override
    public int update(UsuarioPUCP usuario) throws BLException {
        validarPerfil(usuario);
        validarCarrera(usuario);
        validarExiste(usuario.getIdUsuario());
        try {
            return usuarioDAO.update(usuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el usuario", e);
        }
    }

    @Override
    public int delete(int idUsuario) throws BLException {
        validarExiste(idUsuario);
        try {
            if (usuarioDAO.contarAnunciosVigentes(idUsuario) > 0) {
                throw new BLException("No se puede eliminar un usuario con anuncios DISPONIBLE o RESERVADO;"
                        + " archivelos primero");
            }
            if (usuarioDAO.contarTransaccionesEnCurso(idUsuario) > 0) {
                throw new BLException("No se puede eliminar un usuario con transacciones en curso");
            }
            return usuarioDAO.delete(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar el usuario", e);
        }
    }

    @Override
    public UsuarioPUCP findById(int idUsuario) throws BLException {
        try {
            return usuarioDAO.findById(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el usuario", e);
        }
    }

    @Override
    public ArrayList<UsuarioPUCP> findAll() throws BLException {
        try {
            return usuarioDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los usuarios", e);
        }
    }

    @Override
    public UsuarioPUCP obtenerPorCorreo(String correoInstitucional) throws BLException {
        validarTexto(correoInstitucional, "Debe indicar el correo institucional");
        try {
            return usuarioDAO.obtenerPorCorreo(correoInstitucional.trim().toLowerCase());
        } catch (SQLException e) {
            throw new BLException("No se pudo buscar el usuario por correo", e);
        }
    }

    @Override
    public UsuarioPUCP iniciarSesion(String correoInstitucional, String contrasena) throws BLException {
        validarTexto(correoInstitucional, "Debe indicar el correo institucional");
        validarTexto(contrasena, "Debe indicar la contrasena");
        UsuarioPUCP usuario = obtenerPorCorreo(correoInstitucional);
        if (usuario == null || !usuario.isActivo() || !coincide(contrasena, usuario.getContrasena())) {
            throw new BLException(CREDENCIALES_INVALIDAS);
        }
        if (usuario.getEstadoCuenta() == EstadoCuenta.SUSPENDIDA) {
            throw new BLException("La cuenta " + usuario.getCodigoPUCP() + " esta SUSPENDIDA");
        }
        // Una cuenta recien registrada no puede ingresar hasta que un administrador la verifique.
        if (usuario.getEstadoCuenta() == EstadoCuenta.PENDIENTE_VERIFICACION) {
            throw new BLException("La cuenta " + usuario.getCodigoPUCP()
                    + " aun no ha sido verificada por un administrador");
        }
        return usuario;
    }

    @Override
    public void cambiarContrasena(int idUsuario, String contrasenaActual, String contrasenaNueva)
            throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, "usuario");
        validarTexto(contrasenaActual, "Debe indicar la contrasena actual");
        if (!coincide(contrasenaActual, usuario.getContrasena())) {
            throw new BLException("La contrasena actual es incorrecta");
        }
        validarContrasena(contrasenaNueva);
        // Se compara con checkpw: la misma clave con otra sal produce otro hash.
        if (coincide(contrasenaNueva, usuario.getContrasena())) {
            throw new BLException("La nueva contrasena debe ser distinta de la actual");
        }
        try {
            usuarioDAO.cambiarContrasena(idUsuario, hashear(contrasenaNueva));
        } catch (SQLException e) {
            throw new BLException("No se pudo cambiar la contrasena", e);
        }
    }

    @Override
    public void verificarCuenta(int idUsuario) throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, "usuario");
        if (usuario.getEstadoCuenta() == EstadoCuenta.SUSPENDIDA) {
            throw new BLException("No se puede verificar una cuenta SUSPENDIDA");
        }
        try {
            usuarioDAO.verificarCuenta(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la cuenta", e);
        }
    }

    @Override
    public void suspenderCuenta(int idUsuario) throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, "usuario");
        if (usuario.getEstadoCuenta() == EstadoCuenta.SUSPENDIDA) {
            throw new BLException("La cuenta ya esta SUSPENDIDA");
        }
        try {
            usuarioDAO.actualizarEstadoCuenta(idUsuario, EstadoCuenta.SUSPENDIDA);
        } catch (SQLException e) {
            throw new BLException("No se pudo suspender la cuenta", e);
        }
    }

    // Vuelve al estado que tenia antes de la suspension: ACTIVA si estaba verificada.
    @Override
    public void reactivarCuenta(int idUsuario) throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, "usuario");
        if (usuario.getEstadoCuenta() != EstadoCuenta.SUSPENDIDA) {
            throw new BLException("Solo se puede reactivar una cuenta SUSPENDIDA");
        }
        EstadoCuenta estado = usuario.isVerificado() ? EstadoCuenta.ACTIVA : EstadoCuenta.PENDIENTE_VERIFICACION;
        try {
            usuarioDAO.actualizarEstadoCuenta(idUsuario, estado);
        } catch (SQLException e) {
            throw new BLException("No se pudo reactivar la cuenta", e);
        }
    }

    @Override
    public void recalcularReputacion(int idUsuario) throws BLException {
        buscarUsuario(idUsuario, "usuario");
        try {
            usuarioDAO.recalcularReputacion(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo recalcular la reputacion", e);
        }
    }

    @Override
    public NivelReputacion obtenerNivelReputacion(int idUsuario) throws BLException {
        Map<TipoMetricaInsignia, Double> metricas = obtenerMetricas(idUsuario,
                List.of(TipoMetricaInsignia.TRANSACCIONES_COMPLETADAS, TipoMetricaInsignia.CALIFICACION_PROMEDIO));
        return nivelSegun(metricas.get(TipoMetricaInsignia.TRANSACCIONES_COMPLETADAS),
                metricas.get(TipoMetricaInsignia.CALIFICACION_PROMEDIO));
    }

    // Valida al usuario una vez y calcula las metricas en paralelo (cada una es un procedimiento).
    @Override
    public Map<TipoMetricaInsignia, Double> obtenerMetricas(int idUsuario, Collection<TipoMetricaInsignia> metricas)
            throws BLException {
        buscarUsuario(idUsuario, "usuario");
        return new MetricasUsuario(idUsuario, usuarioDAO).precargar(metricas).comoMapa();
    }

    /** Escala de NivelReputacion segun transacciones completadas y calificacion promedio. */
    public static NivelReputacion nivelSegun(double completadas, double promedio) {
        if (completadas >= 25 && promedio >= 4.8) {
            return NivelReputacion.EXPERTO;
        }
        if (completadas >= 10 && promedio >= 4.5) {
            return NivelReputacion.DESTACADO;
        }
        if (completadas >= 3 && promedio >= 4.0) {
            return NivelReputacion.CONFIABLE;
        }
        return NivelReputacion.NUEVO;
    }

    @Override
    public double obtenerMetrica(int idUsuario, TipoMetricaInsignia metrica) throws BLException {
        if (metrica == null) {
            throw new BLException("Debe indicar la metrica");
        }
        buscarUsuario(idUsuario, "usuario");
        try {
            return usuarioDAO.obtenerMetrica(idUsuario, metrica);
        } catch (SQLException e) {
            throw new BLException("No se pudo calcular la metrica " + metrica, e);
        }
    }

    // Validaciones y hash comunes a usuarios y administradores (AdministradorBLImpl las reutiliza).

    // Datos que se pueden editar con update.
    void validarPerfil(UsuarioPUCP usuario) throws BLException {
        if (usuario == null) {
            throw new BLException("El usuario no puede ser nulo");
        }
        validarTexto(usuario.getNombres(), "Los nombres son obligatorios");
        validarTexto(usuario.getApellidos(), "Los apellidos son obligatorios");
        validarLongitudMaxima(usuario.getApellidos(), 120, "Los apellidos");
    }

    // Perfil + identificadores institucionales; se usa al registrar.
    void validarDatosPersonales(UsuarioPUCP usuario) throws BLException {
        validarPerfil(usuario);
        if (usuario.getCodigoPUCP() == null || !PATRON_CODIGO.matcher(usuario.getCodigoPUCP()).matches()) {
            throw new BLException("El codigo PUCP debe tener exactamente 8 digitos");
        }
        validarTexto(usuario.getCorreoInstitucional(), "El correo institucional es obligatorio");
        usuario.setCorreoInstitucional(usuario.getCorreoInstitucional().trim().toLowerCase());
        if (!PATRON_CORREO.matcher(usuario.getCorreoInstitucional()).matches()) {
            throw new BLException("El correo '" + usuario.getCorreoInstitucional()
                    + "' no es valido: debe terminar en @pucp.edu.pe");
        }
    }

    // Se valida la contrasena en claro, antes de hashearla (el hash siempre mide 60 caracteres).
    static void validarContrasena(String contrasena) throws BLException {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new BLException("La contrasena debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
        if (contrasena.length() > LONGITUD_MAXIMA_CONTRASENA) {
            throw new BLException("La contrasena no puede superar " + LONGITUD_MAXIMA_CONTRASENA + " caracteres");
        }
    }

    // La sal aleatoria queda dentro del hash, por eso no se guarda aparte.
    static String hashear(String contrasena) {
        return BCrypt.hashpw(contrasena, BCrypt.gensalt());
    }

    // checkpw lanza IllegalArgumentException si lo guardado no es un hash BCrypt valido.
    private static boolean coincide(String contrasena, String hash) {
        if (hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(contrasena, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // El UNIQUE de la BD es la ultima defensa; aqui se da un mensaje claro antes. El mensaje no
    // dice si choco el correo o el codigo, ni los repite: no confirma que datos estan registrados.
    private static final String CUENTA_DUPLICADA = "Ya existe una cuenta con esos datos."
            + " Si es tuya, inicia sesion o recupera tu contrasena.";

    void validarUnicidad(UsuarioPUCP usuario) throws BLException {
        try {
            UsuarioPUCP porCorreo = usuarioDAO.obtenerPorCorreo(usuario.getCorreoInstitucional());
            if (porCorreo != null && porCorreo.getIdUsuario() != usuario.getIdUsuario()) {
                throw new BLException(CUENTA_DUPLICADA);
            }
            UsuarioPUCP porCodigo = usuarioDAO.obtenerPorCodigo(usuario.getCodigoPUCP());
            if (porCodigo != null && porCodigo.getIdUsuario() != usuario.getIdUsuario()) {
                throw new BLException(CUENTA_DUPLICADA);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la unicidad del correo y del codigo", e);
        }
    }

    // Un estudiante debe tener una carrera activa; la facultad se obtiene a traves de ella.
    private void validarCarrera(UsuarioPUCP usuario) throws BLException {
        if (usuario.getCarrera() == null) {
            throw new BLException("El estudiante debe tener una carrera");
        }
        try {
            Carrera carrera = carreraDAO.findById(usuario.getCarrera().getIdCarrera());
            if (carrera == null || !carrera.isActivo()) {
                throw new BLException("La carrera no existe o ya no esta disponible");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la carrera", e);
        }
    }

    private void validarExiste(int idUsuario) throws BLException {
        if (findById(idUsuario) == null) {
            throw new BLException("El usuario no existe o ya no esta disponible");
        }
    }
}
