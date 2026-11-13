package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;
import pe.edu.pucp.reuse.rest.dto.FavoritoDTO;

public final class FavoritoMapper {
    private FavoritoMapper() {
    }

    public static FavoritoDTO toDTO(Favorito favorito) {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdFavorito(favorito.getIdFavorito());
        dto.setFechaGuardado(Fechas.aTexto(favorito.getFechaGuardado()));
        if (favorito.getUsuario() != null) {
            dto.setIdUsuario(favorito.getUsuario().getIdUsuario());
        }
        if (favorito.getAnuncio() != null) {
            dto.setIdAnuncio(favorito.getAnuncio().getIdAnuncio());
            dto.setTituloAnuncio(favorito.getAnuncio().getTitulo());
            dto.setPrecioAnuncio(favorito.getAnuncio().getPrecio());
            dto.setEstadoAnuncio(favorito.getAnuncio().getEstado() == null ? null
                    : favorito.getAnuncio().getEstado().name());
        }
        return dto;
    }

    public static List<FavoritoDTO> toDTOs(List<Favorito> favoritos) {
        List<FavoritoDTO> dtos = new ArrayList<>();
        for (Favorito favorito : favoritos) {
            dtos.add(toDTO(favorito));
        }
        return dtos;
    }

    public static Favorito toModelo(FavoritoDTO dto) {
        return new Favorito(UsuarioMapper.referencia(dto.getIdUsuario()), AnuncioMapper.referencia(dto.getIdAnuncio()));
    }
}
