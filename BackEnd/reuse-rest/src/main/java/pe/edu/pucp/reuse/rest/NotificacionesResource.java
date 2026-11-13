package pe.edu.pucp.reuse.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.NotificacionBL;
import pe.edu.pucp.reuse.bl.impl.NotificacionBLImpl;
import pe.edu.pucp.reuse.modelo.mensajeria.Notificacion;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.rest.dto.NotificacionDTO;
import pe.edu.pucp.reuse.rest.dto.RecordatorioCitaDTO;
import pe.edu.pucp.reuse.rest.mapper.NotificacionMapper;

/**
 * Bandeja de notificaciones del usuario.
 * RF-12: GET /notificaciones/recordatorios devuelve las citas confirmadas de las proximas horas
 * (24 por defecto) y deja una notificacion RECORDATORIO_CITA por cada una (sin duplicarla).
 */
@Path("/notificaciones")
@Produces("application/json")
@Consumes("application/json")
public class NotificacionesResource {
    private final NotificacionBL notificacionBL;

    public NotificacionesResource() {
        notificacionBL = new NotificacionBLImpl();
    }

    /** Notificaciones del usuario, mas recientes primero. */
    @GET
    public Response listar(@QueryParam("usuario") int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el usuario (?usuario=ID)");
        }
        List<NotificacionDTO> dtos = new ArrayList<>();
        for (Notificacion notificacion : notificacionBL.listarPorDestinatario(idUsuario)) {
            if (notificacion.isActivo()) {
                dtos.add(NotificacionMapper.toDTO(notificacion));
            }
        }
        dtos.sort(Comparator.comparing(NotificacionDTO::getFechaHora, Comparator.nullsLast(Comparator.reverseOrder())));
        return Response.ok(dtos).build();
    }

    @PUT
    @Path("/{id}/leer")
    public Response marcarLeida(@PathParam("id") int id) throws BLException {
        notificacionBL.marcarComoLeida(id);
        return Response.ok(NotificacionMapper.toDTO(notificacionBL.findById(id))).build();
    }

    @PUT
    @Path("/leer-todas")
    public Response marcarTodas(@QueryParam("usuario") int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el usuario (?usuario=ID)");
        }
        return Response.ok(Map.of("marcadas", notificacionBL.marcarTodasComoLeidas(idUsuario))).build();
    }

    /** RF-12: recordatorios de citas confirmadas en las proximas 'horas' (24 por defecto). */
    @GET
    @Path("/recordatorios")
    public Response recordatorios(@QueryParam("usuario") int idUsuario,
                                  @QueryParam("horas") @DefaultValue("24") int horas) throws BLException {
        if (idUsuario <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el usuario (?usuario=ID)");
        }
        NombresUsuario nombres = new NombresUsuario();
        List<RecordatorioCitaDTO> dtos = new ArrayList<>();
        for (CitaEntrega cita : notificacionBL.generarRecordatoriosCitas(idUsuario, horas)) {
            RecordatorioCitaDTO dto = NotificacionMapper.toRecordatorio(cita, idUsuario);
            if (dto.getContraparte() == null && cita.getTransaccion() != null
                    && cita.getTransaccion().getAnuncio() != null
                    && cita.getTransaccion().getAnuncio().getVendedor() != null) {
                dto.setContraparte(nombres.de(cita.getTransaccion().getAnuncio().getVendedor().getIdUsuario()));
            }
            dtos.add(dto);
        }
        return Response.ok(dtos).build();
    }
}
