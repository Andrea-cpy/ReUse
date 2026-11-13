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
import pe.edu.pucp.reuse.bl.CategoriaMaterialBL;
import pe.edu.pucp.reuse.bl.impl.CategoriaMaterialBLImpl;
import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;
import pe.edu.pucp.reuse.rest.dto.CategoriaMaterialDTO;
import pe.edu.pucp.reuse.rest.mapper.CategoriaMaterialMapper;

/** RF-06: categorias de materiales (solo administrador). */
@Path("/categorias")
@Produces("application/json")
@Consumes("application/json")
public class CategoriasResource {
    private final CategoriaMaterialBL categoriaBL;

    @Context
    private UriInfo uriInfo;

    public CategoriasResource() {
        categoriaBL = new CategoriaMaterialBLImpl();
    }

    @GET
    public Response findAll() throws BLException {
        return Response.ok(CategoriaMaterialMapper.toDTOs(categoriaBL.findAll())).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        CategoriaMaterial categoria = categoriaBL.findById(id);
        if (categoria == null) {
            return Respuestas.noEncontrado("La categoria no existe o ya no esta disponible");
        }
        return Response.ok(CategoriaMaterialMapper.toDTO(categoria)).build();
    }

    @POST
    public Response insert(CategoriaMaterialDTO dto) throws BLException {
        CategoriaMaterial categoria = CategoriaMaterialMapper.toModelo(dto);
        int id = categoriaBL.insert(categoria);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(CategoriaMaterialMapper.toDTO(categoriaBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, CategoriaMaterialDTO dto) throws BLException {
        if (dto == null || id != dto.getIdCategoria()) {
            return Respuestas.solicitudInvalida("No se pudo guardar el cambio.");
        }
        categoriaBL.update(CategoriaMaterialMapper.toModelo(dto));
        return Response.ok(CategoriaMaterialMapper.toDTO(categoriaBL.findById(id))).build();
    }

    /** Deshabilita la categoria; la capa de negocio lo impide si aun tiene materiales activos. */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        categoriaBL.delete(id);
        return Response.noContent().build();
    }
}
