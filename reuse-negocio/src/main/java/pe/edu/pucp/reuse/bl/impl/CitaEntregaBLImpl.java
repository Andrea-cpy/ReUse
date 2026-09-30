package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CitaEntregaBL;
import pe.edu.pucp.reuse.dao.CitaEntregaDAO;
import pe.edu.pucp.reuse.dao.PuntoEntregaDAO;
import pe.edu.pucp.reuse.dao.impl.CitaEntregaDAOImpl;
import pe.edu.pucp.reuse.dao.impl.PuntoEntregaDAOImpl;
import pe.edu.pucp.reuse.modelo.enums.EstadoCita;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class CitaEntregaBLImpl extends BaseBLImpl implements CitaEntregaBL {

    private final CitaEntregaDAO citaDAO = new CitaEntregaDAOImpl();
    private final PuntoEntregaDAO puntoDAO = new PuntoEntregaDAOImpl();

    // Proponer una cita para una transaccion en negociacion.
    @Override
    public int insert(CitaEntrega cita) throws BLException {
        validarDatos(cita);
        try {
            if (citaDAO.obtenerPorTransaccion(cita.getTransaccion().getIdTransaccion()) != null) {
                throw new BLException("La transaccion ya tiene una cita; use update para renegociarla");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la cita de la transaccion", e);
        }
        cita.setEstado(EstadoCita.PROPUESTA);
        try {
            return citaDAO.insert(cita);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la cita", e);
        }
    }

    // Renegociar fecha o punto mientras la cita siga PROPUESTA.
    @Override
    public int update(CitaEntrega cita) throws BLException {
        validarDatos(cita);
        CitaEntrega actual = buscarCita(cita.getIdCita());
        if (actual.getEstado() != EstadoCita.PROPUESTA) {
            throw new BLException("Solo se puede renegociar una cita PROPUESTA (estado actual: "
                    + actual.getEstado() + ")");
        }
        if (actual.getTransaccion().getIdTransaccion() != cita.getTransaccion().getIdTransaccion()) {
            throw new BLException("No se puede mover una cita a otra transaccion");
        }
        cita.setEstado(EstadoCita.PROPUESTA);
        try {
            return citaDAO.update(cita);
        } catch (SQLException e) {
            throw new BLException("No se pudo renegociar la cita", e);
        }
    }

    @Override
    public int delete(int idCita) throws BLException {
        CitaEntrega actual = buscarCita(idCita);
        if (actual.getEstado() == EstadoCita.CONFIRMADA || actual.getEstado() == EstadoCita.REALIZADA) {
            throw new BLException("No se puede delete una cita " + actual.getEstado()
                    + "; cancele la transaccion si corresponde");
        }
        try {
            return citaDAO.delete(idCita);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la cita", e);
        }
    }

    @Override
    public CitaEntrega findById(int idCita) throws BLException {
        try {
            return citaDAO.findById(idCita);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cita", e);
        }
    }

    @Override
    public ArrayList<CitaEntrega> findAll() throws BLException {
        try {
            return citaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las citas", e);
        }
    }

    @Override
    public CitaEntrega obtenerPorTransaccion(int idTransaccion) throws BLException {
        try {
            return citaDAO.obtenerPorTransaccion(idTransaccion);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la cita de la transaccion", e);
        }
    }

    private void validarDatos(CitaEntrega cita) throws BLException {
        if (cita == null) {
            throw new BLException("La cita no puede ser nula");
        }
        if (cita.getFechaHora() == null || !cita.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new BLException("La cita debe programarse en una fecha y hora futuras");
        }
        if (cita.getPuntoEntrega() == null) {
            throw new BLException("La cita debe indicar el punto de entrega");
        }
        try {
            PuntoEntrega punto = puntoDAO.findById(cita.getPuntoEntrega().getIdPuntoEntrega());
            if (punto == null || !punto.isActivo()) {
                throw new BLException("El punto de entrega " + cita.getPuntoEntrega().getIdPuntoEntrega()
                        + " no existe o esta deshabilitado");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el punto de entrega", e);
        }
        if (cita.getTransaccion() == null) {
            throw new BLException("La cita debe pertenecer a una transaccion");
        }
        Transaccion transaccion = buscarTransaccion(cita.getTransaccion().getIdTransaccion());
        if (transaccion.getEstado() != EstadoTransaccion.EN_NEGOCIACION) {
            throw new BLException("Solo se proponen citas para transacciones EN_NEGOCIACION (estado actual: "
                    + transaccion.getEstado() + ")");
        }
    }

    private CitaEntrega buscarCita(int idCita) throws BLException {
        CitaEntrega cita = findById(idCita);
        if (cita == null || !cita.isActivo()) {
            throw new BLException("No existe la cita con id " + idCita);
        }
        return cita;
    }
}
