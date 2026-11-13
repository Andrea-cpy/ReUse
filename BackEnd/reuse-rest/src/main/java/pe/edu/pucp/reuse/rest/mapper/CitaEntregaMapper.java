package pe.edu.pucp.reuse.rest.mapper;

import pe.edu.pucp.reuse.modelo.transacciones.CitaEntrega;
import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;
import pe.edu.pucp.reuse.rest.dto.CitaEntregaDTO;

public final class CitaEntregaMapper {
    private CitaEntregaMapper() {
    }

    public static CitaEntregaDTO toDTO(CitaEntrega cita) {
        CitaEntregaDTO dto = new CitaEntregaDTO();
        dto.setIdCita(cita.getIdCita());
        dto.setFechaHora(Fechas.aTexto(cita.getFechaHora()));
        dto.setEstado(cita.getEstado() == null ? null : cita.getEstado().name());
        if (cita.getPuntoEntrega() != null) {
            dto.setIdPuntoEntrega(cita.getPuntoEntrega().getIdPuntoEntrega());
            dto.setNombrePuntoEntrega(cita.getPuntoEntrega().getNombre());
        }
        if (cita.getTransaccion() != null) {
            dto.setIdTransaccion(cita.getTransaccion().getIdTransaccion());
        }
        return dto;
    }

    public static CitaEntrega toModelo(CitaEntregaDTO dto) {
        PuntoEntrega punto = new PuntoEntrega();
        punto.setIdPuntoEntrega(dto.getIdPuntoEntrega());
        CitaEntrega cita = new CitaEntrega(Fechas.aFecha(dto.getFechaHora()),
                TransaccionMapper.referencia(dto.getIdTransaccion()), punto);
        cita.setIdCita(dto.getIdCita());
        return cita;
    }
}
