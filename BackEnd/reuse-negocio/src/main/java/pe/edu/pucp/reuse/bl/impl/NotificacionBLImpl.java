package pe.edu.pucp.reuse.bl.impl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.NotificacionBL;
import pe.edu.pucp.reuse.dao.CitaEntregaDAO;
import pe.edu.pucp.reuse.dao.NotificacionDAO;
import pe.edu.pucp.reuse.dao.impl.CitaEntregaDAOImpl;
import pe.edu.pucp.reuse.dao.impl.NotificacionDAOImpl;
import pe.edu.pucp.reuse.modelo.enums.EstadoCita;
import pe.edu.pucp.reuse.modelo.enums.EstadoNotificacion;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.enums.TipoNotificacion;
import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;

public class NotificacionBLImpl extends BaseBLImpl implements NotificacionBL {

    private static final int HORAS_MAXIMAS_RECORDATORIO = 7 * 24;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final NotificacionDAO notificacionDAO = new NotificacionDAOImpl();
    private final CitaEntregaDAO citaDAO = new CitaEntregaDAOImpl();

    @Override
    public int marcarTodasComoLeidas(int idDestinatario) throws BLException {
        int cambiadas = 0;
        for (Notificacion notificacion : listarPorDestinatario(idDestinatario)) {
            if (notificacion.isActivo() && notificacion.getEstado() == EstadoNotificacion.NO_LEIDA) {
                notificacion.setEstado(EstadoNotificacion.LEIDA);
                try {
                    notificacionDAO.update(notificacion);
                } catch (SQLException e) {
                    throw new BLException("No se pudo marcar las notificaciones como leidas", e);
                }
                cambiadas++;
            }
        }
        return cambiadas;
    }

    /**
     * RF-12: una cita CONFIRMADA de una transaccion CITA_CONFIRMADA que ocurre entre ahora y
     * las proximas 'horas' genera un recordatorio. La transaccion se identifica en el texto
     * ("transaccion #ID") para no duplicar el aviso si el usuario vuelve a consultar.
     */
    @Override
    public ArrayList<CitaEntrega> generarRecordatoriosCitas(int idUsuario, int horas) throws BLException {
        buscarUsuario(idUsuario, "usuario");
        if (horas <= 0 || horas > HORAS_MAXIMAS_RECORDATORIO) {
            throw new BLException("La ventana del recordatorio debe estar entre 1 y " + HORAS_MAXIMAS_RECORDATORIO + " horas");
        }
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(horas);

        ArrayList<Transaccion> transacciones = new TransaccionBLImpl().listarPorUsuario(idUsuario);
        ArrayList<Notificacion> existentes = listarPorDestinatario(idUsuario);

        ArrayList<CitaEntrega> proximas = new ArrayList<>();
        for (Transaccion transaccion : transacciones) {
            CitaEntrega resumen = transaccion.getCitaEntrega();
            if (transaccion.getEstado() != EstadoTransaccion.CITA_CONFIRMADA || resumen == null
                    || resumen.getEstado() != EstadoCita.CONFIRMADA || resumen.getFechaHora() == null
                    || resumen.getFechaHora().isBefore(ahora) || resumen.getFechaHora().isAfter(limite)) {
                continue;
            }
            CitaEntrega cita;
            try {
                cita = citaDAO.obtenerPorTransaccion(transaccion.getIdTransaccion()); // trae el punto de entrega
            } catch (SQLException e) {
                throw new BLException("No se pudo recuperar la cita de la transaccion " + transaccion.getIdTransaccion(), e);
            }
            if (cita == null) {
                continue;
            }
            cita.setTransaccion(transaccion);
            proximas.add(cita);

            String marca = "transaccion #" + transaccion.getIdTransaccion();
            boolean yaAvisado = existentes.stream().anyMatch(n -> n.isActivo()
                    && n.getTipo() == TipoNotificacion.RECORDATORIO_CITA
                    && n.getMensaje() != null && n.getMensaje().contains(marca));
            if (!yaAvisado) {
                String lugar = cita.getPuntoEntrega() == null || cita.getPuntoEntrega().getNombre() == null
                        ? "el punto acordado" : cita.getPuntoEntrega().getNombre();
                new Notificador().avisar(idUsuario, TipoNotificacion.RECORDATORIO_CITA,
                        "Recordatorio: tienes una cita de entrega por '" + transaccion.getAnuncio().getTitulo()
                                + "' (" + marca + ") el " + cita.getFechaHora().format(FORMATO_FECHA)
                                + " en " + lugar + ".");
            }
        }
        proximas.sort((a, b) -> a.getFechaHora().compareTo(b.getFechaHora()));
        return proximas;
    }

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
            throw new BLException("No se pudo eliminar la notificacion", e);
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
