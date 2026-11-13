package pe.edu.pucp.reuse.rest.error;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Datos mal formados en la solicitud (un enum o una fecha invalidos): 400. */
@Provider
public class EntradaInvalidaMapper implements ExceptionMapper<IllegalArgumentException> {
    private static final Logger LOG = Logger.getLogger(EntradaInvalidaMapper.class.getName());

    @Override
    public Response toResponse(IllegalArgumentException ex) {
        String idSolicitud = UUID.randomUUID().toString();
        LOG.info("[" + idSolicitud + "] Solicitud invalida: " + ex.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                // El detalle (clases, enums, formatos internos) solo va al log, con el idSolicitud para ubicarlo.
                .entity(Map.of("error", "Los datos enviados no son validos.", "idSolicitud", idSolicitud))
                .build();
    }
}
