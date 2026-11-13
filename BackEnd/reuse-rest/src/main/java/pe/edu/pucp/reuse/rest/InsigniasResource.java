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
import jakarta.ws.rs.core.Response;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.InsigniaBL;
import pe.edu.pucp.reuse.bl.impl.InsigniaBLImpl;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.rest.dto.InsigniaDTO;
import pe.edu.pucp.reuse.rest.mapper.GamificacionMapper;

/**
 * RF-18 (administrador): catalogo de insignias y sus reglas. Cada insignia es una
 * especificacion compuesta por sus reglas (patron Specification en la capa de negocio).
 */
@Path("/insignias")
@Produces("application/json")
@Consumes("application/json")
public class InsigniasResource {
    private final InsigniaBL insigniaBL;

    public InsigniasResource() {
        insigniaBL = new InsigniaBLImpl();
    }

    @GET
    public Response listar() throws BLException {
        return Response.ok(GamificacionMapper.insigniasToDTOs(insigniaBL.listarConReglas())).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        Insignia insignia = insigniaBL.findById(id);
        if (insignia == null) {
            return Respuestas.noEncontrado("La insignia no existe o ya no esta disponible");
        }
        return Response.ok(GamificacionMapper.toDTO(insignia)).build();
    }

    @POST
    public Response insert(InsigniaDTO dto) throws BLException {
        if (dto == null) {
            return Respuestas.solicitudInvalida("Debe enviar la insignia");
        }
        int id = insigniaBL.insert(GamificacionMapper.toModelo(dto));
        return Response.created(URI.create("insignias/" + id))
                .entity(GamificacionMapper.toDTO(insigniaBL.findById(id)))
                .build();
    }

    /** Reemplaza la cabecera y todas las reglas en una sola transaccion. */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, InsigniaDTO dto) throws BLException {
        if (dto == null) {
            return Respuestas.solicitudInvalida("Debe enviar la insignia");
        }
        dto.setIdInsignia(id);
        insigniaBL.actualizarConReglas(GamificacionMapper.toModelo(dto));
        return Response.ok(GamificacionMapper.toDTO(insigniaBL.findById(id))).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        insigniaBL.delete(id);
        return Response.noContent().build();
    }
}
