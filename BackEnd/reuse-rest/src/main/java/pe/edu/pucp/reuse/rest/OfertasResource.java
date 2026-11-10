package pe.edu.pucp.reuse.rest;

import java.net.URI;
import java.util.Map;

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
import pe.edu.pucp.reuse.bl.OfertaBL;
import pe.edu.pucp.reuse.bl.impl.OfertaBLImpl;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.rest.dto.MontoDTO;
import pe.edu.pucp.reuse.rest.dto.OfertaDTO;
import pe.edu.pucp.reuse.rest.mapper.OfertaMapper;

/**
 * RF-17: el comprador propone un precio; el vendedor acepta, rechaza o contraoferta.
 * Las ofertas recibidas por un anuncio estan en GET /anuncios/{id}/ofertas.
 */
@Path("/ofertas")
@Produces("application/json")
@Consumes("application/json")
public class OfertasResource {
    private final OfertaBL ofertaBL;

    @Context
    private UriInfo uriInfo;

    public OfertasResource() {
        ofertaBL = new OfertaBLImpl();
    }

    /** Ofertas hechas por el comprador (?comprador=ID). */
    @GET
    public Response listarPorComprador(@QueryParam("comprador") int idComprador) throws BLException {
        if (idComprador <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el comprador (?comprador=ID)");
        }
        return Response.ok(OfertaMapper.toDTOs(ofertaBL.listarPorComprador(idComprador))).build();
    }

    @POST
    public Response insert(OfertaDTO ofertaDTO) throws BLException {
        Oferta oferta = OfertaMapper.toModelo(ofertaDTO);
        int id = ofertaBL.insert(oferta);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(OfertaMapper.toDTO(ofertaBL.findById(id))).build();
    }

    /** El comprador retira su oferta (no se puede si ya fue aceptada). */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) throws BLException {
        ofertaBL.delete(id);
        return Response.noContent().build();
    }

    /** Acepta la oferta, rechaza las demas pendientes y abre la transaccion. Devuelve su id. */
    @PUT
    @Path("/{id}/aceptar")
    public Response aceptar(@PathParam("id") int id) throws BLException {
        int idTransaccion = ofertaBL.aceptar(id);
        return Response.ok(Map.of("idTransaccion", idTransaccion)).build();
    }

    @PUT
    @Path("/{id}/rechazar")
    public Response rechazar(@PathParam("id") int id) throws BLException {
        ofertaBL.rechazar(id);
        return Response.ok(OfertaMapper.toDTO(ofertaBL.findById(id))).build();
    }

    /** El vendedor propone otro monto. */
    @PUT
    @Path("/{id}/contraoferta")
    public Response contraofertar(@PathParam("id") int id, MontoDTO monto) throws BLException {
        if (monto == null) {
            return Respuestas.solicitudInvalida("Debe enviar el monto de la contraoferta");
        }
        ofertaBL.contraofertar(id, monto.getMonto());
        return Response.ok(OfertaMapper.toDTO(ofertaBL.findById(id))).build();
    }

    /** El comprador acepta la contraoferta: se abre la transaccion con el nuevo monto. */
    @PUT
    @Path("/{id}/contraoferta/aceptar")
    public Response aceptarContraoferta(@PathParam("id") int id) throws BLException {
        int idTransaccion = ofertaBL.responderContraoferta(id, true);
        return Response.ok(Map.of("idTransaccion", idTransaccion)).build();
    }

    @PUT
    @Path("/{id}/contraoferta/rechazar")
    public Response rechazarContraoferta(@PathParam("id") int id) throws BLException {
        ofertaBL.responderContraoferta(id, false);
        return Response.ok(OfertaMapper.toDTO(ofertaBL.findById(id))).build();
    }
}
