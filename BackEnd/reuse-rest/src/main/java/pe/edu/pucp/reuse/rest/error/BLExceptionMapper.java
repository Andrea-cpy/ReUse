package pe.edu.pucp.reuse.rest.error;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.pucp.reuse.bl.BLException;

/**
 * Manejo centralizado de errores (RNF-06). Cada error se registra en el log del
 * servidor con un identificador unico, que tambien se devuelve al cliente.
 * Regla de negocio incumplida: 400. Falla de base de datos: 500.
 */
@Provider
public class BLExceptionMapper implements ExceptionMapper<BLException> {
    private static final Logger LOG = Logger.getLogger(BLExceptionMapper.class.getName());

    @Override
    public Response toResponse(BLException ex) {
        String idSolicitud = UUID.randomUUID().toString();
        boolean errorDeDatos = ex.getCause() instanceof SQLException;
        Response.Status status = errorDeDatos ? Response.Status.INTERNAL_SERVER_ERROR : Response.Status.BAD_REQUEST;
        LOG.log(errorDeDatos ? Level.SEVERE : Level.INFO, "[" + idSolicitud + "] " + ex.getMessage(), ex);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", ex.getMessage(), "idSolicitud", idSolicitud))
                .build();
    }
}
