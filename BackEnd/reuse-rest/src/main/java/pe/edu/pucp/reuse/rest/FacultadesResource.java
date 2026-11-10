package pe.edu.pucp.reuse.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CarreraBL;
import pe.edu.pucp.reuse.bl.FacultadBL;
import pe.edu.pucp.reuse.bl.impl.CarreraBLImpl;
import pe.edu.pucp.reuse.bl.impl.FacultadBLImpl;
import pe.edu.pucp.reuse.rest.mapper.AcademicoMapper;

/** Facultades y sus carreras (combos del registro y del perfil). Solo lectura. */
@Path("/facultades")
@Produces("application/json")
public class FacultadesResource {
    private final FacultadBL facultadBL;
    private final CarreraBL carreraBL;

    public FacultadesResource() {
        facultadBL = new FacultadBLImpl();
        carreraBL = new CarreraBLImpl();
    }

    @GET
    public Response findAll() throws BLException {
        return Response.ok(AcademicoMapper.facultadesToDTOs(facultadBL.findAll())).build();
    }

    @GET
    @Path("/{id}/carreras")
    public Response carreras(@PathParam("id") int idFacultad) throws BLException {
        return Response.ok(AcademicoMapper.carrerasToDTOs(carreraBL.listarPorFacultad(idFacultad))).build();
    }
}
