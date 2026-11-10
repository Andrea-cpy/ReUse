package pe.edu.pucp.reuse.rest;

import java.net.URI;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CitaEntregaBL;
import pe.edu.pucp.reuse.bl.impl.CitaEntregaBLImpl;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.rest.dto.CitaEntregaDTO;
import pe.edu.pucp.reuse.rest.mapper.CitaEntregaMapper;

/** RF-03: proponer, renegociar o retirar una cita de entrega (la confirmacion esta en /transacciones). */
@Path("/citas")
@Produces("application/json")
@Consumes("application/json")
public class CitasResource {
    private final CitaEntregaBL citaBL;

    @Context
    private UriInfo uriInfo;

    public CitasResource() {
        citaBL = new CitaEntregaBLImpl();
    }

    @POST
    public Response proponer(CitaEntregaDTO citaDTO) throws BLException {
        CitaEntrega cita = CitaEntregaMapper.toModelo(citaDTO);
        int id = citaBL.insert(cita);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(CitaEntregaMapper.toDTO(citaBL.findById(id))).build();
    }

    /** Renegociar fecha o punto mientras la cita siga PROPUESTA. */
    @PUT
    @Path("/{id}")
    public Response renegociar(@PathParam("id") int id, CitaEntregaDTO citaDTO) throws BLException {
        if (citaDTO == null || id != citaDTO.getIdCita()) {
            return Respuestas.solicitudInvalida("El id y el payload no coinciden");
        }
        citaBL.update(CitaEntregaMapper.toModelo(citaDTO));
        return Response.ok(CitaEntregaMapper.toDTO(citaBL.findById(id))).build();
    }

    /** Retira una cita PROPUESTA (no se puede si ya fue confirmada). */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        citaBL.delete(id);
        return Response.noContent().build();
    }
}
