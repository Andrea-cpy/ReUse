package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.dao.AnuncioDAO;
import pe.edu.pucp.reuse.dao.TransaccionDAO;
import pe.edu.pucp.reuse.dao.UsuarioDAO;
import pe.edu.pucp.reuse.dao.impl.AnuncioDAOImpl;
import pe.edu.pucp.reuse.dao.impl.TransaccionDAOImpl;
import pe.edu.pucp.reuse.dao.impl.UsuarioDAOImpl;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

/**
 * Consultas de validacion que comparten varias clases de negocio: verificar
 * que un usuario, anuncio o transaccion referenciado exista y este activo.
 */
abstract class BaseBLImpl {

    protected final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    protected final AnuncioDAO anuncioDAO = new AnuncioDAOImpl();
    protected final TransaccionDAO transaccionDAO = new TransaccionDAOImpl();

    protected static void validarTexto(String valor, String mensaje) throws BLException {
        if (valor == null || valor.isBlank()) {
            throw new BLException(mensaje);
        }
    }

    protected static void validarLongitudMaxima(String valor, int maximo, String campo) throws BLException {
        if (valor != null && valor.length() > maximo) {
            throw new BLException(campo + " no puede superar " + maximo + " caracteres");
        }
    }

    // Usuario existente y no eliminado (borrado logico).
    protected UsuarioPUCP buscarUsuario(int idUsuario, String rol) throws BLException {
        UsuarioPUCP usuario;
        try {
            usuario = usuarioDAO.findById(idUsuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el " + rol, e);
        }
        if (usuario == null || !usuario.isActivo()) {
            throw new BLException("No existe el " + rol + " con id " + idUsuario);
        }
        return usuario;
    }

    protected UsuarioPUCP buscarUsuario(UsuarioPUCP referencia, String rol) throws BLException {
        if (referencia == null) {
            throw new BLException("Debe indicar el " + rol);
        }
        return buscarUsuario(referencia.getIdUsuario(), rol);
    }

    // Ademas de existir, la cuenta debe estar ACTIVA (verificada y no suspendida).
    protected UsuarioPUCP buscarUsuarioHabilitado(int idUsuario, String rol) throws BLException {
        UsuarioPUCP usuario = buscarUsuario(idUsuario, rol);
        if (usuario.getEstadoCuenta() != EstadoCuenta.ACTIVA) {
            throw new BLException("La cuenta del " + rol + " " + usuario.getCodigoPUCP() + " esta "
                    + usuario.getEstadoCuenta() + "; debe estar ACTIVA");
        }
        return usuario;
    }

    protected UsuarioPUCP buscarUsuarioHabilitado(UsuarioPUCP referencia, String rol) throws BLException {
        if (referencia == null) {
            throw new BLException("Debe indicar el " + rol);
        }
        return buscarUsuarioHabilitado(referencia.getIdUsuario(), rol);
    }

    protected Anuncio buscarAnuncio(int idAnuncio) throws BLException {
        Anuncio anuncio;
        try {
            anuncio = anuncioDAO.findById(idAnuncio);
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el anuncio", e);
        }
        if (anuncio == null || !anuncio.isActivo()) {
            throw new BLException("No existe el anuncio con id " + idAnuncio);
        }
        return anuncio;
    }

    protected Anuncio buscarAnuncio(Anuncio referencia) throws BLException {
        if (referencia == null) {
            throw new BLException("Debe indicar el anuncio");
        }
        return buscarAnuncio(referencia.getIdAnuncio());
    }

    protected Transaccion buscarTransaccion(int idTransaccion) throws BLException {
        Transaccion transaccion;
        try {
            transaccion = transaccionDAO.findById(idTransaccion);
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la transaccion", e);
        }
        if (transaccion == null || !transaccion.isActivo()) {
            throw new BLException("No existe la transaccion con id " + idTransaccion);
        }
        return transaccion;
    }

    // El vendedor no se guarda en la transaccion: se obtiene navegando por el anuncio.
    protected static int idVendedor(Transaccion transaccion) {
        return transaccion.getAnuncio().getVendedor().getIdUsuario();
    }
}
