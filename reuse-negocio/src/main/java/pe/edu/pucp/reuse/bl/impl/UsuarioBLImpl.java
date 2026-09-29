package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.UsuarioBL;
import pe.edu.pucp.reuse.dao.CarreraDAO;
import pe.edu.pucp.reuse.dao.impl.CarreraDAOImpl;
import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.enums.NivelReputacion;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class UsuarioBLImpl extends BaseBLImpl implements UsuarioBL {

    // RNF-02: solo correos institucionales @pucp.edu.pe.
    private static final Pattern PATRON_CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@pucp\\.edu\\.pe$");
    private static final Pattern PATRON_CODIGO = Pattern.compile("^\\d{8}$");
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private final CarreraDAO carreraDAO = new CarreraDAOImpl();

    // RF-01: toda cuenta nueva queda pendiente de verificacion.
    @Override
    public int insert(UsuarioPUCP usuario) throws BLException {
        validarDatosPersonales(usuario);
        validarCarrera(usuario);
        validarUnicidad(usuario);
        usuario.setVerificado(false);
        usuario.setEstadoCuenta(EstadoCuenta.PENDIENTE_VERIFICACION);
        try {
            return usuarioDAO.insert(usuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el usuario", e);
        }
    }

    @Override
    public int update(UsuarioPUCP usuario) throws BLException {
        validarDatosPersonales(usuario);
        validarCarrera(usuario);
        validarExiste(usuario.getIdUsuario());
        validarUnicidad(usuario);
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
            return usuarioDAO.delete(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete el usuario", e);
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
    public void verificarCuenta(int idUsuario) throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, "usuario");
        if (usuario.getEstadoCuenta() == EstadoCuenta.SUSPENDIDA) {
            throw new BLException("No se puede verificar una cuenta SUSPENDIDA");
        }
        usuario.setVerificado(true);
        usuario.setEstadoCuenta(EstadoCuenta.ACTIVA);
        try {
            usuarioDAO.update(usuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la cuenta", e);
        }
    }

    @Override
    public void suspenderCuenta(int idUsuario) throws BLException {
        buscarUsuario(idUsuario, "usuario");
        try {
            usuarioDAO.actualizarEstadoCuenta(idUsuario, EstadoCuenta.SUSPENDIDA);
        } catch (SQLException e) {
            throw new BLException("No se pudo suspender la cuenta", e);
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
        double completadas = obtenerMetrica(idUsuario, TipoMetricaInsignia.TRANSACCIONES_COMPLETADAS);
        double promedio = obtenerMetrica(idUsuario, TipoMetricaInsignia.CALIFICACION_PROMEDIO);
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

    // Validaciones comunes a usuarios y administradores (AdministradorBLImpl las reutiliza).
    void validarDatosPersonales(UsuarioPUCP usuario) throws BLException {
        if (usuario == null) {
            throw new BLException("El usuario no puede ser nulo");
        }
        if (usuario.getCodigoPUCP() == null || !PATRON_CODIGO.matcher(usuario.getCodigoPUCP()).matches()) {
            throw new BLException("El codigo PUCP debe tener exactamente 8 digitos");
        }
        validarTexto(usuario.getNombres(), "Los nombres son obligatorios");
        validarTexto(usuario.getApellidoPaterno(), "El apellido paterno es obligatorio");
        validarTexto(usuario.getCorreoInstitucional(), "El correo institucional es obligatorio");
        usuario.setCorreoInstitucional(usuario.getCorreoInstitucional().trim().toLowerCase());
        if (!PATRON_CORREO.matcher(usuario.getCorreoInstitucional()).matches()) {
            throw new BLException("El correo '" + usuario.getCorreoInstitucional()
                    + "' no es valido: debe terminar en @pucp.edu.pe");
        }
        if (usuario.getContrasena() == null || usuario.getContrasena().length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new BLException("La contrasena debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
        if (usuario.getEstadoCuenta() == null) {
            throw new BLException("El estado de la cuenta es obligatorio");
        }
    }

    // Obs. 2 de la JP: el UNIQUE de la BD es la ultima defensa; aqui se da un mensaje claro antes.
    void validarUnicidad(UsuarioPUCP usuario) throws BLException {
        try {
            UsuarioPUCP porCorreo = usuarioDAO.obtenerPorCorreo(usuario.getCorreoInstitucional());
            if (porCorreo != null && porCorreo.getIdUsuario() != usuario.getIdUsuario()) {
                throw new BLException("Ya existe un usuario con el correo " + usuario.getCorreoInstitucional());
            }
            UsuarioPUCP porCodigo = usuarioDAO.obtenerPorCodigo(usuario.getCodigoPUCP());
            if (porCodigo != null && porCodigo.getIdUsuario() != usuario.getIdUsuario()) {
                throw new BLException("Ya existe un usuario con el codigo PUCP " + usuario.getCodigoPUCP());
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
                throw new BLException("No existe la carrera " + usuario.getCarrera().getIdCarrera());
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la carrera", e);
        }
    }

    private void validarExiste(int idUsuario) throws BLException {
        if (findById(idUsuario) == null) {
            throw new BLException("No existe un usuario con id " + idUsuario);
        }
    }
}
