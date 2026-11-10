package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.transacciones.PuntoEntrega;
import pe.edu.pucp.reuse.rest.dto.PuntoEntregaDTO;

public final class PuntoEntregaMapper {
    private PuntoEntregaMapper() {
    }

    public static PuntoEntregaDTO toDTO(PuntoEntrega punto) {
        PuntoEntregaDTO dto = new PuntoEntregaDTO();
        dto.setIdPuntoEntrega(punto.getIdPuntoEntrega());
        dto.setNombre(punto.getNombre());
        dto.setReferencia(punto.getReferencia());
        dto.setUbicacion(punto.getUbicacion());
        dto.setActivo(punto.isActivo());
        return dto;
    }

    public static List<PuntoEntregaDTO> toDTOs(List<PuntoEntrega> puntos) {
        List<PuntoEntregaDTO> dtos = new ArrayList<>();
        for (PuntoEntrega punto : puntos) {
            dtos.add(toDTO(punto));
        }
        return dtos;
    }

    public static PuntoEntrega toModelo(PuntoEntregaDTO dto) {
        PuntoEntrega punto = new PuntoEntrega();
        punto.setIdPuntoEntrega(dto.getIdPuntoEntrega());
        punto.setNombre(dto.getNombre());
        punto.setReferencia(dto.getReferencia());
        punto.setUbicacion(dto.getUbicacion());
        punto.setActivo(dto.isActivo());
        return punto;
    }
}
