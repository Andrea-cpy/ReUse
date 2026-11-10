package pe.edu.pucp.reuse.rest;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CitaEntregaBL;
import pe.edu.pucp.reuse.bl.TransaccionBL;
import pe.edu.pucp.reuse.bl.impl.CitaEntregaBLImpl;
import pe.edu.pucp.reuse.bl.impl.TransaccionBLImpl;
import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.rest.dto.TransaccionDTO;
import pe.edu.pucp.reuse.rest.mapper.CitaEntregaMapper;
import pe.edu.pucp.reuse.rest.mapper.TransaccionMapper;

/**
 * RF-15: historial de compras y ventas. RF-11: confirmacion independiente de comprador y vendedor.
 * RF-03: confirmar la cita (la propuesta y la renegociacion estan en /citas).
 */
@Path("/transacciones")
@Produces("application/json")
@Consumes("application/json")
public class TransaccionesResource {
    private final TransaccionBL transaccionBL;
    private final CitaEntregaBL citaBL;

    public TransaccionesResource() {
        transaccionBL = new TransaccionBLImpl();
        citaBL = new CitaEntregaBLImpl();
    }

    @GET
    public Response listarPorUsuario(@QueryParam("usuario") int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el usuario (?usuario=ID)");
        }
        List<TransaccionDTO> dtos = TransaccionMapper.toDTOs(transaccionBL.listarPorUsuario(idUsuario));
        NombresUsuario nombres = new NombresUsuario();
        for (TransaccionDTO dto : dtos) {
            dto.setNombreVendedor(nombres.de(dto.getIdVendedor()));
        }
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        Transaccion transaccion = transaccionBL.findById(id);
        if (transaccion == null) {
            return Respuestas.noEncontrado("No existe la transaccion con id " + id);
        }
        TransaccionDTO dto = TransaccionMapper.toDTO(transaccion);
        dto.setNombreVendedor(new NombresUsuario().de(dto.getIdVendedor()));
        return Response.ok(dto).build();
    }

    /** Cita de entrega de la transaccion; 404 si aun no se propuso. */
    @GET
    @Path("/{id}/cita")
    public Response cita(@PathParam("id") int id) throws BLException {
        CitaEntrega cita = citaBL.obtenerPorTransaccion(id);
        if (cita == null) {
            return Respuestas.noEncontrado("La transaccion " + id + " aun no tiene cita");
        }
        return Response.ok(CitaEntregaMapper.toDTO(cita)).build();
    }

    /** RF-03: la cita queda CONFIRMADA, la transaccion CITA_CONFIRMADA y el anuncio RESERVADO. */
    @PUT
    @Path("/{id}/confirmar-cita")
    public Response confirmarCita(@PathParam("id") int id) throws BLException {
        transaccionBL.confirmarCita(id);
        return findById(id);
    }

    /** RF-11: confirma el comprador o el vendedor (?usuario=ID); con ambos, COMPLETADA. */
    @PUT
    @Path("/{id}/confirmar-entrega")
    public Response confirmarEntrega(@PathParam("id") int id, @QueryParam("usuario") int idUsuario)
            throws BLException {
        transaccionBL.confirmarEntrega(id, idUsuario);
        return findById(id);
    }

    @PUT
    @Path("/{id}/cancelar")
    public Response cancelar(@PathParam("id") int id) throws BLException {
        transaccionBL.cancelar(id);
        return findById(id);
    }
}
