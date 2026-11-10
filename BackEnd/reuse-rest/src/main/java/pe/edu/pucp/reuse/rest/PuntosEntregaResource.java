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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.PuntoEntregaBL;
import pe.edu.pucp.reuse.bl.impl.PuntoEntregaBLImpl;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;
import pe.edu.pucp.reuse.rest.dto.PuntoEntregaDTO;
import pe.edu.pucp.reuse.rest.mapper.PuntoEntregaMapper;

/** RF-05: puntos seguros del campus para las citas (solo administrador). */
@Path("/puntos-entrega")
@Produces("application/json")
@Consumes("application/json")
public class PuntosEntregaResource {
    private final PuntoEntregaBL puntoEntregaBL;

    @Context
    private UriInfo uriInfo;

    public PuntosEntregaResource() {
        puntoEntregaBL = new PuntoEntregaBLImpl();
    }

    @GET
    public Response findAll() throws BLException {
        return Response.ok(PuntoEntregaMapper.toDTOs(puntoEntregaBL.findAll())).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        PuntoEntrega punto = puntoEntregaBL.findById(id);
        if (punto == null) {
            return Respuestas.noEncontrado("No existe el punto de entrega con id " + id);
        }
        return Response.ok(PuntoEntregaMapper.toDTO(punto)).build();
    }

    @POST
    public Response insert(PuntoEntregaDTO dto) throws BLException {
        PuntoEntrega punto = PuntoEntregaMapper.toModelo(dto);
        int id = puntoEntregaBL.insert(punto);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(PuntoEntregaMapper.toDTO(puntoEntregaBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, PuntoEntregaDTO dto) throws BLException {
        if (dto == null || id != dto.getIdPuntoEntrega()) {
            return Respuestas.solicitudInvalida("El id y el payload no coinciden");
        }
        puntoEntregaBL.update(PuntoEntregaMapper.toModelo(dto));
        return Response.ok(PuntoEntregaMapper.toDTO(puntoEntregaBL.findById(id))).build();
    }

    /** Deshabilita el punto; la capa de negocio lo impide si tiene citas PROPUESTA o CONFIRMADA. */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        puntoEntregaBL.delete(id);
        return Response.noContent().build();
    }
}
