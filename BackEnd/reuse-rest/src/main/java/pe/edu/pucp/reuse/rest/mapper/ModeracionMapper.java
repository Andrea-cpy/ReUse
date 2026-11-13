package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.MotivoReporteAnuncio;
import pe.edu.pucp.reuse.modelo.enums.MotivoReporteUsuario;
import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;
import pe.edu.pucp.reuse.modelo.moderacion.Reporte;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;
import pe.edu.pucp.reuse.rest.dto.BloqueoDTO;
import pe.edu.pucp.reuse.rest.dto.DenunciaDTO;

/** RF-04 / RF-10 / RF-13 / RF-14: denuncias (reportes de usuario y de anuncio) y bloqueos. */
public final class ModeracionMapper {
    public static final String TIPO_USUARIO = "USUARIO";
    public static final String TIPO_ANUNCIO = "ANUNCIO";

    private ModeracionMapper() {
    }

    public static DenunciaDTO toDTO(ReporteUsuario reporte) {
        DenunciaDTO dto = base(reporte, TIPO_USUARIO);
        dto.setMotivo(reporte.getMotivo() == null ? null : reporte.getMotivo().name());
        if (reporte.getDenunciado() != null) {
            dto.setIdDenunciado(reporte.getDenunciado().getIdUsuario());
            dto.setNombreDenunciado(UsuarioMapper.nombre(reporte.getDenunciado()));
            dto.setCodigoDenunciado(reporte.getDenunciado().getCodigoPUCP());
        }
        if (reporte.getTransaccion() != null) {
            dto.setIdTransaccion(reporte.getTransaccion().getIdTransaccion());
        }
        return dto;
    }

    public static DenunciaDTO toDTO(ReporteAnuncio reporte) {
        DenunciaDTO dto = base(reporte, TIPO_ANUNCIO);
        dto.setMotivo(reporte.getMotivo() == null ? null : reporte.getMotivo().name());
        Anuncio anuncio = reporte.getAnuncio();
        if (anuncio != null) {
            dto.setIdAnuncio(anuncio.getIdAnuncio());
            dto.setTituloAnuncio(anuncio.getTitulo());
            dto.setEstadoAnuncio(anuncio.getEstado() == null ? null : anuncio.getEstado().name());
            if (anuncio.getVendedor() != null) {
                dto.setIdVendedorAnuncio(anuncio.getVendedor().getIdUsuario());
            }
        }
        return dto;
    }

    private static DenunciaDTO base(Reporte reporte, String tipo) {
        DenunciaDTO dto = new DenunciaDTO();
        dto.setIdReporte(reporte.getIdReporte());
        dto.setTipo(tipo);
        dto.setDescripcion(reporte.getDescripcion());
        dto.setFechaRegistro(Fechas.aTexto(reporte.getFechaRegistro()));
        dto.setEstadoRevision(reporte.getEstadoRevision() == null ? null : reporte.getEstadoRevision().name());
        dto.setFechaRevision(Fechas.aTexto(reporte.getFechaRevision()));
        if (reporte.getDenunciante() != null) {
            dto.setIdDenunciante(reporte.getDenunciante().getIdUsuario());
            dto.setNombreDenunciante(UsuarioMapper.nombre(reporte.getDenunciante()));
        }
        if (reporte.getRevisor() != null) {
            dto.setIdRevisor(reporte.getRevisor().getIdUsuario());
            dto.setNombreRevisor(UsuarioMapper.nombre(reporte.getRevisor()));
        }
        return dto;
    }

    /** Denuncia contra un usuario (opcionalmente ligada a una transaccion de ambos). */
    public static ReporteUsuario toReporteUsuario(DenunciaDTO dto) {
        ReporteUsuario reporte = new ReporteUsuario();
        reporte.setDescripcion(dto.getDescripcion());
        reporte.setMotivo(enumOpcional(MotivoReporteUsuario.class, dto.getMotivo()));
        reporte.setDenunciante(UsuarioMapper.referencia(dto.getIdDenunciante()));
        reporte.setDenunciado(UsuarioMapper.referencia(dto.getIdDenunciado()));
        if (dto.getIdTransaccion() > 0) {
            reporte.setTransaccion(TransaccionMapper.referencia(dto.getIdTransaccion()));
        }
        return reporte;
    }

    /** Denuncia contra un anuncio: al registrarse, el anuncio DISPONIBLE pasa a OBSERVADO. */
    public static ReporteAnuncio toReporteAnuncio(DenunciaDTO dto) {
        ReporteAnuncio reporte = new ReporteAnuncio();
        reporte.setDescripcion(dto.getDescripcion());
        reporte.setMotivo(enumOpcional(MotivoReporteAnuncio.class, dto.getMotivo()));
        reporte.setDenunciante(UsuarioMapper.referencia(dto.getIdDenunciante()));
        Anuncio anuncio = new Anuncio();
        anuncio.setIdAnuncio(dto.getIdAnuncio());
        reporte.setAnuncio(anuncio);
        return reporte;
    }

    public static BloqueoDTO toDTO(Bloqueo bloqueo) {
        BloqueoDTO dto = new BloqueoDTO();
        dto.setIdBloqueo(bloqueo.getIdBloqueo());
        dto.setFecha(Fechas.aTexto(bloqueo.getFecha()));
        dto.setEstado(bloqueo.getEstado() == null ? null : bloqueo.getEstado().name());
        if (bloqueo.getBloqueador() != null) {
            dto.setIdBloqueador(bloqueo.getBloqueador().getIdUsuario());
            dto.setNombreBloqueador(UsuarioMapper.nombre(bloqueo.getBloqueador()));
        }
        if (bloqueo.getBloqueado() != null) {
            dto.setIdBloqueado(bloqueo.getBloqueado().getIdUsuario());
            dto.setNombreBloqueado(UsuarioMapper.nombre(bloqueo.getBloqueado()));
            dto.setCodigoBloqueado(bloqueo.getBloqueado().getCodigoPUCP());
        }
        return dto;
    }

    public static List<BloqueoDTO> bloqueosToDTOs(List<Bloqueo> bloqueos) {
        List<BloqueoDTO> dtos = new ArrayList<>();
        for (Bloqueo bloqueo : bloqueos) {
            dtos.add(toDTO(bloqueo));
        }
        return dtos;
    }

    /** Un motivo vacio queda en null para que la capa de negocio responda "el motivo es obligatorio". */
    static <E extends Enum<E>> E enumOpcional(Class<E> tipo, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valor no valido para " + tipo.getSimpleName() + ": " + valor);
        }
    }
}
