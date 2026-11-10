package pe.edu.pucp.reuse.rest;

import java.net.URI;
import java.util.List;

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
import pe.edu.pucp.reuse.bl.AnuncioBL;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.MaterialAcademicoBL;
import pe.edu.pucp.reuse.bl.OfertaBL;
import pe.edu.pucp.reuse.bl.impl.AnuncioBLImpl;
import pe.edu.pucp.reuse.bl.impl.MaterialAcademicoBLImpl;
import pe.edu.pucp.reuse.bl.impl.OfertaBLImpl;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;
import pe.edu.pucp.reuse.rest.dto.AnuncioDTO;
import pe.edu.pucp.reuse.rest.mapper.AnuncioMapper;
import pe.edu.pucp.reuse.rest.mapper.OfertaMapper;

/** RF-02: publicar, buscar con filtros, editar, archivar y eliminar anuncios. */
@Path("/anuncios")
@Produces("application/json")
@Consumes("application/json")
public class AnunciosResource {
    private final AnuncioBL anuncioBL;
    private final MaterialAcademicoBL materialBL;
    private final OfertaBL ofertaBL;

    @Context
    private UriInfo uriInfo;

    public AnunciosResource() {
        anuncioBL = new AnuncioBLImpl();
        materialBL = new MaterialAcademicoBLImpl();
        ofertaBL = new OfertaBLImpl();
    }

    /**
     * Con ?vendedor=ID devuelve los anuncios de ese vendedor (cualquier estado).
     * Sin vendedor es el catalogo: solo DISPONIBLE, con filtros opcionales
     * texto, categoria, condicion, precioMin, precioMax y excluirUsuario (el que busca).
     */
    @GET
    public Response findAll(@QueryParam("vendedor") int idVendedor,
                            @QueryParam("texto") String texto,
                            @QueryParam("categoria") int idCategoria,
                            @QueryParam("condicion") String condicion,
                            @QueryParam("precioMin") Double precioMin,
                            @QueryParam("precioMax") Double precioMax,
                            @QueryParam("excluirUsuario") int idUsuarioExcluido) throws BLException {
        List<Anuncio> anuncios;
        if (idVendedor > 0) {
            anuncios = anuncioBL.listarPorVendedor(idVendedor);
        } else {
            CondicionMaterial filtroCondicion = condicion == null || condicion.isBlank()
                    ? null : CondicionMaterial.valueOf(condicion);
            anuncios = anuncioBL.buscar(texto, idCategoria, filtroCondicion, precioMin, precioMax, idUsuarioExcluido);
        }
        return Response.ok(AnuncioMapper.toDTOs(anuncios)).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        Anuncio anuncio = anuncioBL.findById(id);
        if (anuncio == null || !anuncio.isActivo()) {
            return Respuestas.noEncontrado("No existe el anuncio con id " + id);
        }
        // El anuncio trae solo id y titulo del material: se completa con su categoria.
        MaterialAcademico material = materialBL.findById(anuncio.getMaterialAcademico().getIdMaterial());
        if (material != null) {
            anuncio.setMaterialAcademico(material);
        }
        return Response.ok(AnuncioMapper.toDTO(anuncio)).build();
    }

    @POST
    public Response insert(AnuncioDTO anuncioDTO) throws BLException {
        Anuncio anuncio = AnuncioMapper.toModelo(anuncioDTO);
        int id = anuncioBL.insert(anuncio);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(AnuncioMapper.toDTO(anuncioBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, AnuncioDTO anuncioDTO) throws BLException {
        if (anuncioDTO == null || id != anuncioDTO.getIdAnuncio()) {
            return Respuestas.solicitudInvalida("El id y el payload no coinciden");
        }
        anuncioBL.update(AnuncioMapper.toModelo(anuncioDTO));
        return Response.ok(AnuncioMapper.toDTO(anuncioBL.findById(id))).build();
    }

    /** Solo si el anuncio no tiene transacciones asociadas (lo valida la capa de negocio). */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        anuncioBL.delete(id);
        return Response.noContent().build();
    }

    /** El vendedor retira su anuncio del catalogo (DISPONIBLE -> ARCHIVADO). */
    @PUT
    @Path("/{id}/archivar")
    public Response archivar(@PathParam("id") int id, @QueryParam("vendedor") int idVendedor) throws BLException {
        anuncioBL.archivar(id, idVendedor);
        return findById(id);
    }

    /** El vendedor vuelve a publicar su anuncio (ARCHIVADO -> DISPONIBLE). */
    @PUT
    @Path("/{id}/republicar")
    public Response republicar(@PathParam("id") int id, @QueryParam("vendedor") int idVendedor) throws BLException {
        anuncioBL.republicar(id, idVendedor);
        return findById(id);
    }

    /** RF-17: ofertas recibidas por el anuncio (vista del vendedor). */
    @GET
    @Path("/{id}/ofertas")
    public Response ofertas(@PathParam("id") int id) throws BLException {
        return Response.ok(OfertaMapper.toDTOs(ofertaBL.listarPorAnuncio(id))).build();
    }
}
