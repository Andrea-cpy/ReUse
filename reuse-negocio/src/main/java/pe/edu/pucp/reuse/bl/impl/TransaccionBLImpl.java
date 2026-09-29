package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.TransaccionBL;
import pe.edu.pucp.reuse.dao.CitaEntregaDAO;
import pe.edu.pucp.reuse.dao.OfertaDAO;
import pe.edu.pucp.reuse.dao.impl.CitaEntregaDAOImpl;
import pe.edu.pucp.reuse.dao.impl.OfertaDAOImpl;
import pe.edu.pucp.reuse.dao.transacciones.TransactionsManager;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;
import pe.edu.pucp.reuse.modelo.enums.EstadoCita;
import pe.edu.pucp.reuse.modelo.enums.EstadoOferta;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class TransaccionBLImpl extends BaseBLImpl implements TransaccionBL {

    private final CitaEntregaDAO citaDAO = new CitaEntregaDAOImpl();
    private final OfertaDAO ofertaDAO = new OfertaDAOImpl();

    // Inicia una negociacion (EN_NEGOCIACION) sobre un anuncio disponible.
    @Override
    public int insert(Transaccion transaccion) throws BLException {
        if (transaccion == null) {
            throw new BLException("La transaccion no puede ser nula");
        }
        Anuncio anuncio = buscarAnuncio(transaccion.getAnuncio());
        if (anuncio.getEstado() != EstadoAnuncio.DISPONIBLE) {
            throw new BLException("No se puede negociar un anuncio " + anuncio.getEstado());
        }
        UsuarioPUCP comprador = buscarUsuarioHabilitado(transaccion.getComprador(), "comprador");
        if (anuncio.getVendedor().getIdUsuario() == comprador.getIdUsuario()) {
            throw new BLException("El comprador no puede ser el vendedor del anuncio");
        }
        validarOferta(transaccion);
        validarSinNegociacionAbierta(anuncio.getIdAnuncio(), comprador.getIdUsuario());

        transaccion.setEstado(EstadoTransaccion.EN_NEGOCIACION);
        transaccion.setConfirmacionComprador(false);
        transaccion.setConfirmacionVendedor(false);
        try {
            return transaccionDAO.insert(transaccion);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la transaccion", e);
        }
    }

    // Mientras se negocia solo cambia la oferta asociada; el estado y las
    // confirmaciones cambian unicamente con confirmarCita, confirmarEntrega y cancelar.
    @Override
    public int update(Transaccion transaccion) throws BLException {
        if (transaccion == null) {
            throw new BLException("La transaccion no puede ser nula");
        }
        Transaccion actual = buscarTransaccion(transaccion.getIdTransaccion());
        if (actual.getEstado() != EstadoTransaccion.EN_NEGOCIACION) {
            throw new BLException("Solo se puede update una transaccion EN_NEGOCIACION");
        }
        if (transaccion.getAnuncio() == null || transaccion.getComprador() == null
                || transaccion.getAnuncio().getIdAnuncio() != actual.getAnuncio().getIdAnuncio()
                || transaccion.getComprador().getIdUsuario() != actual.getComprador().getIdUsuario()) {
            throw new BLException("No se puede cambiar el anuncio ni el comprador de una transaccion");
        }
        validarOferta(transaccion);
        transaccion.setEstado(actual.getEstado());
        transaccion.setConfirmacionComprador(actual.isConfirmacionComprador());
        transaccion.setConfirmacionVendedor(actual.isConfirmacionVendedor());
        try {
            return transaccionDAO.update(transaccion);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la transaccion", e);
        }
    }

    // No se borra el historial de ventas: solo negociaciones abiertas o canceladas.
    @Override
    public int delete(int idTransaccion) throws BLException {
        Transaccion actual = buscarTransaccion(idTransaccion);
        if (actual.getEstado() == EstadoTransaccion.COMPLETADA
                || actual.getEstado() == EstadoTransaccion.CITA_CONFIRMADA) {
            throw new BLException("No se puede delete una transaccion " + actual.getEstado()
                    + " (forma parte del historial o tiene una cita confirmada)");
        }
        TransactionsManager.iniciar();
        try {
            if (actual.getCitaEntrega() != null) {
                citaDAO.delete(actual.getCitaEntrega().getIdCita());
            }
            int filas = transaccionDAO.delete(idTransaccion);
            TransactionsManager.commit();
            return filas;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo delete la transaccion", e);
        }
    }

    @Override
    public Transaccion findById(int idTransaccion) throws BLException {
        try {
            return transaccionDAO.findById(idTransaccion);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la transaccion", e);
        }
    }

    @Override
    public ArrayList<Transaccion> findAll() throws BLException {
        try {
            return transaccionDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las transacciones", e);
        }
    }

    @Override
    public ArrayList<Transaccion> listarPorAnuncio(int idAnuncio) throws BLException {
        try {
            return transaccionDAO.listarPorAnuncio(idAnuncio);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las transacciones del anuncio", e);
        }
    }

    @Override
    public void confirmarCita(int idTransaccion) throws BLException {
        Transaccion transaccion = buscarTransaccion(idTransaccion);
        if (transaccion.getEstado() != EstadoTransaccion.EN_NEGOCIACION) {
            throw new BLException("Solo se confirma la cita de una transaccion EN_NEGOCIACION (estado actual: "
                    + transaccion.getEstado() + ")");
        }
        CitaEntrega cita = transaccion.getCitaEntrega();
        if (cita == null || cita.getEstado() != EstadoCita.PROPUESTA) {
            throw new BLException("La transaccion " + idTransaccion + " no tiene una cita PROPUESTA");
        }
        int idAnuncio = transaccion.getAnuncio().getIdAnuncio();

        TransactionsManager.iniciar();
        try {
            transaccionDAO.cambiarEstado(idTransaccion, EstadoTransaccion.CITA_CONFIRMADA);
            citaDAO.cambiarEstado(cita.getIdCita(), EstadoCita.CONFIRMADA);
            // Control optimista: solo se reserva si el anuncio sigue DISPONIBLE en la base de datos.
            if (anuncioDAO.cambiarEstado(idAnuncio, EstadoAnuncio.DISPONIBLE, EstadoAnuncio.RESERVADO) == 0) {
                throw new BLException("El anuncio " + idAnuncio + " ya no esta DISPONIBLE; "
                        + "se revierte la confirmacion de la cita (rollback)");
            }
            // Solo una transaccion por anuncio puede quedar confirmada: las demas se cancelan.
            for (Transaccion otra : transaccionDAO.listarPorAnuncio(idAnuncio)) {
                if (otra.getIdTransaccion() != idTransaccion && otra.getEstado() == EstadoTransaccion.EN_NEGOCIACION) {
                    transaccionDAO.finalizar(otra.getIdTransaccion(), EstadoTransaccion.CANCELADA);
                    if (otra.getCitaEntrega() != null) {
                        citaDAO.cambiarEstado(otra.getCitaEntrega().getIdCita(), EstadoCita.CANCELADA);
                    }
                }
            }
            TransactionsManager.commit();
        } catch (BLException e) {
            TransactionsManager.rollback();
            throw e;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo confirmar la cita; se hizo rollback", e);
        }
    }

    @Override
    public void confirmarEntrega(int idTransaccion, int idUsuario) throws BLException {
        Transaccion transaccion = buscarTransaccion(idTransaccion);
        if (transaccion.getEstado() != EstadoTransaccion.CITA_CONFIRMADA) {
            throw new BLException("Solo se confirma la entrega de una transaccion con CITA_CONFIRMADA (estado actual: "
                    + transaccion.getEstado() + ")");
        }
        boolean esComprador = transaccion.getComprador().getIdUsuario() == idUsuario;
        boolean esVendedor = idVendedor(transaccion) == idUsuario;
        if (!esComprador && !esVendedor) {
            throw new BLException("Solo el comprador o el vendedor pueden confirmar la entrega");
        }
        if ((esComprador && transaccion.isConfirmacionComprador())
                || (esVendedor && transaccion.isConfirmacionVendedor())) {
            throw new BLException("El usuario " + idUsuario + " ya confirmo esta entrega");
        }
        int idAnuncio = transaccion.getAnuncio().getIdAnuncio();

        TransactionsManager.iniciar();
        try {
            transaccionDAO.registrarConfirmacion(idTransaccion, esComprador);
            // Se relee dentro de la transaccion para ver ambas confirmaciones actualizadas.
            Transaccion actual = transaccionDAO.findById(idTransaccion);
            if (actual.isConfirmacionComprador() && actual.isConfirmacionVendedor()) {
                transaccionDAO.finalizar(idTransaccion, EstadoTransaccion.COMPLETADA);
                if (anuncioDAO.cambiarEstado(idAnuncio, EstadoAnuncio.RESERVADO, EstadoAnuncio.VENDIDO) == 0) {
                    throw new BLException("El anuncio " + idAnuncio + " no estaba RESERVADO; se revierte (rollback)");
                }
                if (actual.getCitaEntrega() != null) {
                    citaDAO.cambiarEstado(actual.getCitaEntrega().getIdCita(), EstadoCita.REALIZADA);
                }
            }
            TransactionsManager.commit();
        } catch (BLException e) {
            TransactionsManager.rollback();
            throw e;
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo confirmar la entrega; se hizo rollback", e);
        }
    }

    @Override
    public void cancelar(int idTransaccion) throws BLException {
        Transaccion transaccion = buscarTransaccion(idTransaccion);
        EstadoTransaccion estado = transaccion.getEstado();
        if (estado == EstadoTransaccion.COMPLETADA || estado == EstadoTransaccion.CANCELADA) {
            throw new BLException("La transaccion " + idTransaccion + " ya esta " + estado);
        }
        int idAnuncio = transaccion.getAnuncio().getIdAnuncio();
        CitaEntrega cita = transaccion.getCitaEntrega();

        TransactionsManager.iniciar();
        try {
            transaccionDAO.finalizar(idTransaccion, EstadoTransaccion.CANCELADA);
            if (cita != null) {
                citaDAO.cambiarEstado(cita.getIdCita(), EstadoCita.CANCELADA);
            }
            // Si la cita estaba confirmada, el anuncio se libera para otros compradores.
            if (estado == EstadoTransaccion.CITA_CONFIRMADA) {
                anuncioDAO.cambiarEstado(idAnuncio, EstadoAnuncio.RESERVADO, EstadoAnuncio.DISPONIBLE);
            }
            TransactionsManager.commit();
        } catch (SQLException | RuntimeException e) {
            if (TransactionsManager.activa()) {
                TransactionsManager.rollback();
            }
            throw new BLException("No se pudo cancelar la transaccion; se hizo rollback", e);
        }
    }

    // La oferta es opcional; si se indica, debe ser del mismo comprador y anuncio y no estar rechazada.
    private void validarOferta(Transaccion transaccion) throws BLException {
        if (transaccion.getOferta() == null) {
            return;
        }
        Oferta oferta;
        try {
            oferta = ofertaDAO.findById(transaccion.getOferta().getIdOferta());
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la oferta", e);
        }
        if (oferta == null || !oferta.isActivo()) {
            throw new BLException("No existe la oferta " + transaccion.getOferta().getIdOferta());
        }
        if (oferta.getAnuncio().getIdAnuncio() != transaccion.getAnuncio().getIdAnuncio()
                || oferta.getComprador().getIdUsuario() != transaccion.getComprador().getIdUsuario()) {
            throw new BLException("La oferta debe ser del mismo comprador y del mismo anuncio que la transaccion");
        }
        if (oferta.getEstado() == EstadoOferta.RECHAZADA || oferta.getEstado() == EstadoOferta.CONTRAOFERTADA) {
            throw new BLException("No se puede usar una oferta " + oferta.getEstado());
        }
    }

    private void validarSinNegociacionAbierta(int idAnuncio, int idComprador) throws BLException {
        for (Transaccion existente : listarPorAnuncio(idAnuncio)) {
            if (existente.getComprador().getIdUsuario() == idComprador
                    && (existente.getEstado() == EstadoTransaccion.EN_NEGOCIACION
                    || existente.getEstado() == EstadoTransaccion.CITA_CONFIRMADA)) {
                throw new BLException("El comprador ya tiene una negociacion abierta para este anuncio");
            }
        }
    }
}
