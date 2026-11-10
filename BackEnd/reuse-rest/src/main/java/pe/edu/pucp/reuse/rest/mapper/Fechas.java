package pe.edu.pucp.reuse.rest.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/** Las fechas viajan como texto ISO-8601 (2026-11-12T12:30) para que C# las lea sin configuracion extra. */
public final class Fechas {
    private Fechas() {
    }

    public static String aTexto(LocalDateTime fecha) {
        return fecha == null ? null : fecha.toString();
    }

    public static LocalDateTime aFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha invalida: " + texto + " (formato esperado 2026-11-12T12:30)");
        }
    }
}
