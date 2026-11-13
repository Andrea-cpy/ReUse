package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.enums.OperadorComparacion;
import pe.edu.pucp.reuse.modelo.enums.TipoInsignia;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Calificacion;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;
import pe.edu.pucp.reuse.modelo.gamificacion.ReglaInsignia;
import pe.edu.pucp.reuse.rest.dto.CalificacionDTO;
import pe.edu.pucp.reuse.rest.dto.InsigniaDTO;
import pe.edu.pucp.reuse.rest.dto.InsigniaUsuarioDTO;
import pe.edu.pucp.reuse.rest.dto.ReglaInsigniaDTO;

/** RF-15 / RF-18: calificaciones, insignias con sus reglas e insignias obtenidas. */
public final class GamificacionMapper {
    private GamificacionMapper() {
    }

    public static CalificacionDTO toDTO(Calificacion calificacion) {
        CalificacionDTO dto = new CalificacionDTO();
        dto.setIdCalificacion(calificacion.getIdCalificacion());
        dto.setPuntaje(calificacion.getPuntaje());
        dto.setComentario(calificacion.getComentario());
        dto.setFecha(Fechas.aTexto(calificacion.getFecha()));
        dto.setTipo(calificacion.getTipoCalificacion() == null ? null : calificacion.getTipoCalificacion().name());
        if (calificacion.getTransaccion() != null) {
            dto.setIdTransaccion(calificacion.getTransaccion().getIdTransaccion());
        }
        if (calificacion.getCalificador() != null) {
            dto.setIdCalificador(calificacion.getCalificador().getIdUsuario());
            dto.setNombreCalificador(UsuarioMapper.nombre(calificacion.getCalificador()));
        }
        if (calificacion.getCalificado() != null) {
            dto.setIdCalificado(calificacion.getCalificado().getIdUsuario());
            dto.setNombreCalificado(UsuarioMapper.nombre(calificacion.getCalificado()));
        }
        return dto;
    }

    public static List<CalificacionDTO> calificacionesToDTOs(List<Calificacion> calificaciones) {
        List<CalificacionDTO> dtos = new ArrayList<>();
        for (Calificacion calificacion : calificaciones) {
            dtos.add(toDTO(calificacion));
        }
        return dtos;
    }

    /** El calificado y el tipo los deduce la capa de negocio a partir del calificador. */
    public static Calificacion toModelo(CalificacionDTO dto) {
        Calificacion calificacion = new Calificacion();
        calificacion.setPuntaje(dto.getPuntaje());
        calificacion.setComentario(dto.getComentario() == null || dto.getComentario().isBlank()
                ? null : dto.getComentario().trim());
        calificacion.setTransaccion(TransaccionMapper.referencia(dto.getIdTransaccion()));
        calificacion.setCalificador(UsuarioMapper.referencia(dto.getIdCalificador()));
        return calificacion;
    }

    public static InsigniaDTO toDTO(Insignia insignia) {
        InsigniaDTO dto = new InsigniaDTO();
        dto.setIdInsignia(insignia.getIdInsignia());
        dto.setNombre(insignia.getNombre());
        dto.setDescripcion(insignia.getDescripcion());
        dto.setIcono(insignia.getIcono());
        dto.setTipo(insignia.getTipo() == null ? null : insignia.getTipo().name());
        dto.setActivo(insignia.isActivo());
        List<ReglaInsigniaDTO> reglas = new ArrayList<>();
        for (ReglaInsignia regla : insignia.getReglas()) {
            ReglaInsigniaDTO reglaDTO = new ReglaInsigniaDTO();
            reglaDTO.setIdRegla(regla.getIdRegla());
            reglaDTO.setTipoMetrica(regla.getTipoMetrica() == null ? null : regla.getTipoMetrica().name());
            reglaDTO.setOperador(regla.getOperador() == null ? null : regla.getOperador().name());
            reglaDTO.setValorObjetivo(regla.getValorObjetivo());
            reglas.add(reglaDTO);
        }
        dto.setReglas(reglas);
        return dto;
    }

    public static List<InsigniaDTO> insigniasToDTOs(List<Insignia> insignias) {
        List<InsigniaDTO> dtos = new ArrayList<>();
        for (Insignia insignia : insignias) {
            dtos.add(toDTO(insignia));
        }
        return dtos;
    }

    /** Insignia con sus reglas (maestro-detalle), lista para insert o actualizarConReglas. */
    public static Insignia toModelo(InsigniaDTO dto) {
        Insignia insignia = new Insignia(dto.getNombre(), dto.getDescripcion(),
                dto.getIcono() == null || dto.getIcono().isBlank() ? null : dto.getIcono().trim(),
                ModeracionMapper.enumOpcional(TipoInsignia.class, dto.getTipo()));
        insignia.setIdInsignia(dto.getIdInsignia());
        insignia.setActivo(dto.isActivo());
        if (dto.getReglas() != null) {
            for (ReglaInsigniaDTO reglaDTO : dto.getReglas()) {
                ReglaInsignia regla = new ReglaInsignia(
                        ModeracionMapper.enumOpcional(TipoMetricaInsignia.class, reglaDTO.getTipoMetrica()),
                        ModeracionMapper.enumOpcional(OperadorComparacion.class, reglaDTO.getOperador()),
                        reglaDTO.getValorObjetivo(), null);
                insignia.agregarRegla(regla);
            }
        }
        return insignia;
    }

    /** El listado de otorgamientos solo trae nombre y tipo; descripcion e icono salen del catalogo. */
    public static InsigniaUsuarioDTO toDTO(InsigniaUsuario otorgamiento, Insignia detalle) {
        InsigniaUsuarioDTO dto = new InsigniaUsuarioDTO();
        dto.setIdInsigniaUsuario(otorgamiento.getIdInsigniaUsuario());
        dto.setFechaObtencion(Fechas.aTexto(otorgamiento.getFechaObtencion()));
        Insignia insignia = detalle != null ? detalle : otorgamiento.getInsignia();
        if (insignia != null) {
            dto.setIdInsignia(insignia.getIdInsignia());
            dto.setNombre(insignia.getNombre());
            dto.setDescripcion(insignia.getDescripcion());
            dto.setIcono(insignia.getIcono());
            dto.setTipo(insignia.getTipo() == null ? null : insignia.getTipo().name());
        }
        return dto;
    }
}
