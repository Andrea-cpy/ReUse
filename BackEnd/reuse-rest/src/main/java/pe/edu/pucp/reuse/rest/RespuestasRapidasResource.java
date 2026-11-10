package pe.edu.pucp.reuse.rest;

import java.net.URI;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.RespuestaRapidaBL;
import pe.edu.pucp.reuse.bl.impl.RespuestaRapidaBLImpl;
import pe.edu.pucp.reuse.rest.dto.RespuestaRapidaDTO;
import pe.edu.pucp.reuse.rest.mapper.ChatMapper;

/** RF-16: plantillas de respuesta rapida que cada usuario guarda para sus chats. */
@Path("/respuestas-rapidas")
@Produces("application/json")
@Consumes("application/json")
public class RespuestasRapidasResource {
    private final RespuestaRapidaBL respuestaBL;

    @Context
    private UriInfo uriInfo;

    public RespuestasRapidasResource() {
        respuestaBL = new RespuestaRapidaBLImpl();
    }

    @GET
    public Response listarPorCreador(@QueryParam("creador") int idCreador) throws BLException {
        if (idCreador <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el creador (?creador=ID)");
        }
        return Response.ok(ChatMapper.respuestasToDTOs(respuestaBL.listarPorCreador(idCreador))).build();
    }

    @POST
    public Response insert(RespuestaRapidaDTO dto) throws BLException {
        if (dto == null) {
            return Respuestas.solicitudInvalida("Debe enviar el texto y el creador");
        }
        int id = respuestaBL.insert(ChatMapper.toModelo(dto));
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(ChatMapper.toDTO(respuestaBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, RespuestaRapidaDTO dto) throws BLException {
        if (dto == null || id != dto.getIdRespuesta()) {
            return Respuestas.solicitudInvalida("El id y el payload no coinciden");
        }
        respuestaBL.update(ChatMapper.toModelo(dto));
        return Response.ok(ChatMapper.toDTO(respuestaBL.findById(id))).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        respuestaBL.delete(id);
        return Response.noContent().build();
    }
}
