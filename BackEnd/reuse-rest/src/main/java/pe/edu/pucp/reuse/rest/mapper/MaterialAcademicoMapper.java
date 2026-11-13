package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;
import pe.edu.pucp.reuse.rest.dto.MaterialAcademicoDTO;

/** Materiales: solo lectura (combo al publicar un anuncio). */
public final class MaterialAcademicoMapper {
    private MaterialAcademicoMapper() {
    }

    public static MaterialAcademicoDTO toDTO(MaterialAcademico material) {
        MaterialAcademicoDTO dto = new MaterialAcademicoDTO();
        dto.setIdMaterial(material.getIdMaterial());
        dto.setTitulo(material.getTitulo());
        if (material.getCategoria() != null) {
            dto.setIdCategoria(material.getCategoria().getIdCategoria());
            dto.setNombreCategoria(material.getCategoria().getNombre());
        }
        return dto;
    }

    public static List<MaterialAcademicoDTO> toDTOs(List<MaterialAcademico> materiales) {
        List<MaterialAcademicoDTO> dtos = new ArrayList<>();
        for (MaterialAcademico material : materiales) {
            dtos.add(toDTO(material));
        }
        return dtos;
    }
}
