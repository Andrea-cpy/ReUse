package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;
import pe.edu.pucp.reuse.modelo.catalogo.MaterialAcademico;
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;
import pe.edu.pucp.reuse.rest.dto.AnuncioDTO;

public final class AnuncioMapper {
    private AnuncioMapper() {
    }

    public static AnuncioDTO toDTO(Anuncio anuncio) {
        AnuncioDTO dto = new AnuncioDTO();
        dto.setIdAnuncio(anuncio.getIdAnuncio());
        dto.setTitulo(anuncio.getTitulo());
        dto.setPrecio(anuncio.getPrecio());
        dto.setDescripcion(anuncio.getDescripcion());
        dto.setCondicion(anuncio.getCondicion() == null ? null : anuncio.getCondicion().name());
        dto.setEstado(anuncio.getEstado() == null ? null : anuncio.getEstado().name());
        dto.setFechaPublicacion(Fechas.aTexto(anuncio.getFechaPublicacion()));
        if (anuncio.getVendedor() != null) {
            dto.setIdVendedor(anuncio.getVendedor().getIdUsuario());
            dto.setNombreVendedor(UsuarioMapper.nombre(anuncio.getVendedor()));
        }
        MaterialAcademico material = anuncio.getMaterialAcademico();
        if (material != null) {
            dto.setIdMaterial(material.getIdMaterial());
            dto.setTituloMaterial(material.getTitulo());
            if (material.getCategoria() != null) {
                dto.setIdCategoria(material.getCategoria().getIdCategoria());
                dto.setNombreCategoria(material.getCategoria().getNombre());
            }
        }
        for (ImagenProducto imagen : anuncio.getImagenes()) {
            dto.getImagenes().add(imagen.getUrl());
        }
        return dto;
    }

    public static List<AnuncioDTO> toDTOs(List<Anuncio> anuncios) {
        List<AnuncioDTO> dtos = new ArrayList<>();
        for (Anuncio anuncio : anuncios) {
            dtos.add(toDTO(anuncio));
        }
        return dtos;
    }

    /** El vendedor y el estado los fija el servidor, no el cliente. */
    public static Anuncio toModelo(AnuncioDTO dto) {
        Anuncio anuncio = new Anuncio();
        anuncio.setIdAnuncio(dto.getIdAnuncio());
        anuncio.setTitulo(dto.getTitulo());
        anuncio.setPrecio(dto.getPrecio());
        anuncio.setDescripcion(dto.getDescripcion());
        if (dto.getCondicion() != null && !dto.getCondicion().isBlank()) {
            anuncio.setCondicion(CondicionMaterial.valueOf(dto.getCondicion()));
        }
        anuncio.setVendedor(UsuarioMapper.referencia(dto.getIdVendedor()));
        MaterialAcademico material = new MaterialAcademico();
        material.setIdMaterial(dto.getIdMaterial());
        anuncio.setMaterialAcademico(material);
        return anuncio;
    }

    public static Anuncio referencia(int idAnuncio) {
        Anuncio anuncio = new Anuncio();
        anuncio.setIdAnuncio(idAnuncio);
        return anuncio;
    }
}
