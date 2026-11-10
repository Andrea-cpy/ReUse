package pe.edu.pucp.reuse.rest;

import java.net.URI;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.FavoritoBL;
import pe.edu.pucp.reuse.bl.impl.FavoritoBLImpl;
import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;
import pe.edu.pucp.reuse.rest.dto.FavoritoDTO;
import pe.edu.pucp.reuse.rest.mapper.FavoritoMapper;

/** RF-07: agregar, listar y quitar anuncios guardados. */
@Path("/favoritos")
@Produces("application/json")
@Consumes("application/json")
public class FavoritosResource {
    private final FavoritoBL favoritoBL;

    @Context
    private UriInfo uriInfo;

    public FavoritosResource() {
        favoritoBL = new FavoritoBLImpl();
    }

    @GET
    public Response listarPorUsuario(@QueryParam("usuario") int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el usuario (?usuario=ID)");
        }
        return Response.ok(FavoritoMapper.toDTOs(favoritoBL.listarPorUsuario(idUsuario))).build();
    }

    @POST
    public Response insert(FavoritoDTO favoritoDTO) throws BLException {
        Favorito favorito = FavoritoMapper.toModelo(favoritoDTO);
        int id = favoritoBL.insert(favorito);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(FavoritoMapper.toDTO(favoritoBL.findById(id))).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        favoritoBL.delete(id);
        return Response.noContent().build();
    }
}
