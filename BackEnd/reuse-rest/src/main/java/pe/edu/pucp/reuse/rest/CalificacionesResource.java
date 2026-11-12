package pe.edu.pucp.reuse.rest;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CalificacionBL;
import pe.edu.pucp.reuse.bl.impl.CalificacionBLImpl;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.rest.dto.CalificacionDTO;
import pe.edu.pucp.reuse.rest.mapper.GamificacionMapper;

/**
 * RF-15: calificar desde el historial una transaccion COMPLETADA (1 a 5 estrellas).
 * RF-18: al calificar se recalcula la reputacion del calificado (recalcular_reputacion) y se
 * evaluan sus insignias.
 */
@Path("/calificaciones")
@Produces("application/json")
@Consumes("application/json")
public class CalificacionesResource {
    private final CalificacionBL calificacionBL;

    public CalificacionesResource() {
        calificacionBL = new CalificacionBLImpl();
    }

    /** ?calificado=ID (las que recibio, para su perfil) o ?calificador=ID (las que hizo). */
    @GET
    public Response listar(@QueryParam("calificado") int idCalificado,
                           @QueryParam("calificador") int idCalificador) throws BLException {
        List<Calificacion> calificaciones;
        if (idCalificado > 0) {
            calificaciones = calificacionBL.listarPorCalificado(idCalificado);
        } else if (idCalificador > 0) {
            calificaciones = calificacionBL.listarPorCalificador(idCalificador);
        } else {
            return Respuestas.solicitudInvalida("Debe indicar ?calificado=ID o ?calificador=ID");
        }
        List<CalificacionDTO> dtos = GamificacionMapper.calificacionesToDTOs(calificaciones);
        dtos.sort(Comparator.comparing(CalificacionDTO::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        return Response.ok(dtos).build();
    }

    @POST
    public Response calificar(CalificacionDTO dto) throws BLException {
        if (dto == null) {
            return Respuestas.solicitudInvalida("Debe enviar la calificacion");
        }
        int id = calificacionBL.insert(GamificacionMapper.toModelo(dto));
        return Response.created(URI.create("calificaciones/" + id))
                .entity(GamificacionMapper.toDTO(calificacionBL.findById(id)))
                .build();
    }
}
