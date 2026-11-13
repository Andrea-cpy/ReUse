package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.academico.Facultad;
import pe.edu.pucp.reuse.rest.dto.CarreraDTO;
import pe.edu.pucp.reuse.rest.dto.FacultadDTO;

/** Facultades y carreras: solo lectura (combos del registro y del perfil). */
public final class AcademicoMapper {
    private AcademicoMapper() {
    }

    public static FacultadDTO toDTO(Facultad facultad) {
        FacultadDTO dto = new FacultadDTO();
        dto.setIdFacultad(facultad.getIdFacultad());
        dto.setNombre(facultad.getNombre());
        return dto;
    }

    public static List<FacultadDTO> facultadesToDTOs(List<Facultad> facultades) {
        List<FacultadDTO> dtos = new ArrayList<>();
        for (Facultad facultad : facultades) {
            dtos.add(toDTO(facultad));
        }
        return dtos;
    }

    public static CarreraDTO toDTO(Carrera carrera) {
        CarreraDTO dto = new CarreraDTO();
        dto.setIdCarrera(carrera.getIdCarrera());
        dto.setNombre(carrera.getNombre());
        if (carrera.getFacultad() != null) {
            dto.setIdFacultad(carrera.getFacultad().getIdFacultad());
            dto.setNombreFacultad(carrera.getFacultad().getNombre());
        }
        return dto;
    }

    public static List<CarreraDTO> carrerasToDTOs(List<Carrera> carreras) {
        List<CarreraDTO> dtos = new ArrayList<>();
        for (Carrera carrera : carreras) {
            dtos.add(toDTO(carrera));
        }
        return dtos;
    }
}
