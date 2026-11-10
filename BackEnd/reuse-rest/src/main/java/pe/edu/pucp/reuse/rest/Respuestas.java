package pe.edu.pucp.reuse.rest;

import java.util.Map;

import jakarta.ws.rs.core.Response;

/** Respuestas comunes de los recursos. */
final class Respuestas {
    private Respuestas() {
    }

    static Response noEncontrado(String mensaje) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", mensaje))
                .build();
    }

    static Response solicitudInvalida(String mensaje) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", mensaje))
                .build();
    }
}
