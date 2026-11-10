package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.academico.Carrera;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.dto.UsuarioDTO;

public final class UsuarioMapper {
    private UsuarioMapper() {
    }

    /** Nunca copia la contrasena (ni su hash) hacia el DTO. */
    public static UsuarioDTO toDTO(UsuarioPUCP usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setCodigoPUCP(usuario.getCodigoPUCP());
        dto.setNombres(usuario.getNombres());
        dto.setApellidoPaterno(usuario.getApellidoPaterno());
        dto.setApellidoMaterno(usuario.getApellidoMaterno());
        dto.setCorreoInstitucional(usuario.getCorreoInstitucional());
        if (usuario.getCarrera() != null) {
            dto.setIdCarrera(usuario.getCarrera().getIdCarrera());
            dto.setNombreCarrera(usuario.getCarrera().getNombre());
            if (usuario.getCarrera().getFacultad() != null) {
                dto.setIdFacultad(usuario.getCarrera().getFacultad().getIdFacultad());
                dto.setNombreFacultad(usuario.getCarrera().getFacultad().getNombre());
            }
        }
        dto.setVerificado(usuario.isVerificado());
        dto.setEstadoCuenta(usuario.getEstadoCuenta() == null ? null : usuario.getEstadoCuenta().name());
        dto.setReputacionVendedor(usuario.getReputacionVendedor());
        dto.setReputacionComprador(usuario.getReputacionComprador());
        dto.setContadorReportes(usuario.getContadorReportes());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        dto.setFechaRegistro(Fechas.aTexto(usuario.getFechaRegistro()));
        return dto;
    }

    public static List<UsuarioDTO> toDTOs(List<UsuarioPUCP> usuarios) {
        List<UsuarioDTO> dtos = new ArrayList<>();
        for (UsuarioPUCP usuario : usuarios) {
            dtos.add(toDTO(usuario));
        }
        return dtos;
    }

    /** Solo los campos que el estudiante puede enviar (registro o edicion de perfil). */
    public static UsuarioPUCP toModelo(UsuarioDTO dto) {
        UsuarioPUCP usuario = new UsuarioPUCP();
        usuario.setIdUsuario(dto.getIdUsuario());
        usuario.setCodigoPUCP(dto.getCodigoPUCP());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidoPaterno(dto.getApellidoPaterno());
        usuario.setApellidoMaterno(dto.getApellidoMaterno());
        usuario.setCorreoInstitucional(dto.getCorreoInstitucional());
        usuario.setContrasena(dto.getContrasena());
        usuario.setFotoPerfil(dto.getFotoPerfil());
        if (dto.getIdCarrera() > 0) {
            Carrera carrera = new Carrera();
            carrera.setIdCarrera(dto.getIdCarrera());
            usuario.setCarrera(carrera);
        }
        return usuario;
    }

    /** Referencia minima (solo id) para relaciones. */
    public static UsuarioPUCP referencia(int idUsuario) {
        UsuarioPUCP usuario = new UsuarioPUCP();
        usuario.setIdUsuario(idUsuario);
        return usuario;
    }

    public static String nombre(UsuarioPUCP usuario) {
        if (usuario == null || usuario.getNombres() == null) {
            return null;
        }
        return usuario.getApellidoPaterno() == null ? usuario.getNombres()
                : usuario.getNombres() + " " + usuario.getApellidoPaterno();
    }
}
