package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.rest.dto.OfertaDTO;

public final class OfertaMapper {
    private OfertaMapper() {
    }

    public static OfertaDTO toDTO(Oferta oferta) {
        OfertaDTO dto = new OfertaDTO();
        dto.setIdOferta(oferta.getIdOferta());
        dto.setMontoPropuesto(oferta.getMontoPropuesto());
        dto.setEstado(oferta.getEstado() == null ? null : oferta.getEstado().name());
        dto.setFecha(Fechas.aTexto(oferta.getFecha()));
        if (oferta.getComprador() != null) {
            dto.setIdComprador(oferta.getComprador().getIdUsuario());
            dto.setNombreComprador(UsuarioMapper.nombre(oferta.getComprador()));
        }
        if (oferta.getAnuncio() != null) {
            dto.setIdAnuncio(oferta.getAnuncio().getIdAnuncio());
            dto.setTituloAnuncio(oferta.getAnuncio().getTitulo());
            dto.setPrecioAnuncio(oferta.getAnuncio().getPrecio());
            if (oferta.getAnuncio().getVendedor() != null) {
                dto.setIdVendedor(oferta.getAnuncio().getVendedor().getIdUsuario());
            }
        }
        return dto;
    }

    public static List<OfertaDTO> toDTOs(List<Oferta> ofertas) {
        List<OfertaDTO> dtos = new ArrayList<>();
        for (Oferta oferta : ofertas) {
            dtos.add(toDTO(oferta));
        }
        return dtos;
    }

    public static Oferta toModelo(OfertaDTO dto) {
        Oferta oferta = new Oferta(dto.getMontoPropuesto(), UsuarioMapper.referencia(dto.getIdComprador()),
                AnuncioMapper.referencia(dto.getIdAnuncio()));
        oferta.setIdOferta(dto.getIdOferta());
        return oferta;
    }
}
