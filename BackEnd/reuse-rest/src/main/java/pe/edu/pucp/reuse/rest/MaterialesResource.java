package pe.edu.pucp.reuse.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.MaterialAcademicoBL;
import pe.edu.pucp.reuse.bl.impl.MaterialAcademicoBLImpl;
import pe.edu.pucp.reuse.rest.mapper.MaterialAcademicoMapper;

/** Materiales academicos (combo al publicar un anuncio). Solo lectura. */
@Path("/materiales")
@Produces("application/json")
public class MaterialesResource {
    private final MaterialAcademicoBL materialBL;

    public MaterialesResource() {
        materialBL = new MaterialAcademicoBLImpl();
    }

    @GET
    public Response findAll() throws BLException {
        return Response.ok(MaterialAcademicoMapper.toDTOs(materialBL.findAll())).build();
    }
}
