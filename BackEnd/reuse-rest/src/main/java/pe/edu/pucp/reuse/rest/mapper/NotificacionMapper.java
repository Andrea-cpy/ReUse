package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.rest.dto.NotificacionDTO;
import pe.edu.pucp.reuse.rest.dto.RecordatorioCitaDTO;

/** Notificaciones del usuario y recordatorios de cita (RF-12). Solo de salida. */
public final class NotificacionMapper {
    private NotificacionMapper() {
    }

    public static NotificacionDTO toDTO(Notificacion notificacion) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setIdNotificacion(notificacion.getIdNotificacion());
        dto.setMensaje(notificacion.getMensaje());
        dto.setFechaHora(Fechas.aTexto(notificacion.getFechaHora()));
        dto.setTipo(notificacion.getTipo() == null ? null : notificacion.getTipo().name());
        dto.setEstado(notificacion.getEstado() == null ? null : notificacion.getEstado().name());
        if (notificacion.getDestinatario() != null) {
            dto.setIdDestinatario(notificacion.getDestinatario().getIdUsuario());
        }
        return dto;
    }

    public static List<NotificacionDTO> toDTOs(List<Notificacion> notificaciones) {
        List<NotificacionDTO> dtos = new ArrayList<>();
        for (Notificacion notificacion : notificaciones) {
            dtos.add(toDTO(notificacion));
        }
        return dtos;
    }

    /** El rol y la contraparte se calculan respecto del usuario que consulta. */
    public static RecordatorioCitaDTO toRecordatorio(CitaEntrega cita, int idUsuario) {
        RecordatorioCitaDTO dto = new RecordatorioCitaDTO();
        dto.setIdCita(cita.getIdCita());
        dto.setFechaHora(Fechas.aTexto(cita.getFechaHora()));
        dto.setPuntoEntrega(cita.getPuntoEntrega() == null ? null : cita.getPuntoEntrega().getNombre());
        Transaccion transaccion = cita.getTransaccion();
        if (transaccion != null) {
            dto.setIdTransaccion(transaccion.getIdTransaccion());
            if (transaccion.getAnuncio() != null) {
                dto.setIdAnuncio(transaccion.getAnuncio().getIdAnuncio());
                dto.setTituloAnuncio(transaccion.getAnuncio().getTitulo());
            }
            boolean soyComprador = transaccion.getComprador() != null
                    && transaccion.getComprador().getIdUsuario() == idUsuario;
            dto.setRol(soyComprador ? "COMPRADOR" : "VENDEDOR");
            if (soyComprador) {
                dto.setContraparte(null); // el nombre del vendedor lo completa el recurso
            } else {
                dto.setContraparte(UsuarioMapper.nombre(transaccion.getComprador()));
            }
        }
        return dto;
    }
}
