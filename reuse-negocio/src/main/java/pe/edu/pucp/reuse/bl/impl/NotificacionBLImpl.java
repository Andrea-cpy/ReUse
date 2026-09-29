package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.NotificacionBL;
import pe.edu.pucp.reuse.dao.NotificacionDAO;
import pe.edu.pucp.reuse.dao.impl.NotificacionDAOImpl;
import pe.edu.pucp.reuse.modelo.enums.EstadoNotificacion;
import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;

public class NotificacionBLImpl extends BaseBLImpl implements NotificacionBL {

    private final NotificacionDAO notificacionDAO = new NotificacionDAOImpl();

    @Override
    public int insert(Notificacion notificacion) throws BLException {
        validarDatos(notificacion);
        notificacion.setEstado(EstadoNotificacion.NO_LEIDA);
        try {
            return notificacionDAO.insert(notificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la notificacion", e);
        }
    }

    @Override
    public int update(Notificacion notificacion) throws BLException {
        validarDatos(notificacion);
        buscarNotificacion(notificacion.getIdNotificacion());
        if (notificacion.getEstado() == null) {
            throw new BLException("El estado de la notificacion es obligatorio");
        }
        try {
            return notificacionDAO.update(notificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la notificacion", e);
        }
    }

    @Override
    public int delete(int idNotificacion) throws BLException {
        buscarNotificacion(idNotificacion);
        try {
            return notificacionDAO.delete(idNotificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo delete la notificacion", e);
        }
    }

    @Override
    public Notificacion findById(int idNotificacion) throws BLException {
        try {
            return notificacionDAO.findById(idNotificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la notificacion", e);
        }
    }

    @Override
    public ArrayList<Notificacion> findAll() throws BLException {
        try {
            return notificacionDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las notificaciones", e);
        }
    }

    @Override
    public ArrayList<Notificacion> listarPorDestinatario(int idDestinatario) throws BLException {
        try {
            return notificacionDAO.listarPorDestinatario(idDestinatario);
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las notificaciones del usuario", e);
        }
    }

    @Override
    public void marcarComoLeida(int idNotificacion) throws BLException {
        Notificacion notificacion = buscarNotificacion(idNotificacion);
        if (notificacion.getEstado() == EstadoNotificacion.LEIDA) {
            return;
        }
        notificacion.setEstado(EstadoNotificacion.LEIDA);
        try {
            notificacionDAO.update(notificacion);
        } catch (SQLException e) {
            throw new BLException("No se pudo marcar la notificacion como leida", e);
        }
    }

    private void validarDatos(Notificacion notificacion) throws BLException {
        if (notificacion == null) {
            throw new BLException("La notificacion no puede ser nula");
        }
        validarTexto(notificacion.getMensaje(), "El mensaje de la notificacion es obligatorio");
        validarLongitudMaxima(notificacion.getMensaje(), 500, "El mensaje de la notificacion");
        if (notificacion.getTipo() == null) {
            throw new BLException("El tipo de notificacion es obligatorio");
        }
        buscarUsuario(notificacion.getDestinatario(), "destinatario");
    }

    private Notificacion buscarNotificacion(int idNotificacion) throws BLException {
        Notificacion notificacion = findById(idNotificacion);
        if (notificacion == null || !notificacion.isActivo()) {
            throw new BLException("No existe la notificacion con id " + idNotificacion);
        }
        return notificacion;
    }
}
