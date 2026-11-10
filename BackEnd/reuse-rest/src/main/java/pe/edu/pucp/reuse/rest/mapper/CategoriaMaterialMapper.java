package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.catalogo.CategoriaMaterial;
import pe.edu.pucp.reuse.rest.dto.CategoriaMaterialDTO;

public final class CategoriaMaterialMapper {
    private CategoriaMaterialMapper() {
    }

    public static CategoriaMaterialDTO toDTO(CategoriaMaterial categoria) {
        CategoriaMaterialDTO dto = new CategoriaMaterialDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setActivo(categoria.isActivo());
        return dto;
    }

    public static List<CategoriaMaterialDTO> toDTOs(List<CategoriaMaterial> categorias) {
        List<CategoriaMaterialDTO> dtos = new ArrayList<>();
        for (CategoriaMaterial categoria : categorias) {
            dtos.add(toDTO(categoria));
        }
        return dtos;
    }

    public static CategoriaMaterial toModelo(CategoriaMaterialDTO dto) {
        CategoriaMaterial categoria = new CategoriaMaterial();
        categoria.setIdCategoria(dto.getIdCategoria());
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setActivo(dto.isActivo());
        return categoria;
    }
}
