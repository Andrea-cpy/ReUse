package pe.edu.pucp.reuse.rest.mapper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.mensajeria.Mensaje;
import pe.edu.pucp.reuse.modelo.mensajeria.RespuestaRapida;
import pe.edu.pucp.reuse.rest.dto.CanalChatDTO;
import pe.edu.pucp.reuse.rest.dto.MensajeDTO;
import pe.edu.pucp.reuse.rest.dto.RespuestaRapidaDTO;

/** Canales de chat, mensajes y respuestas rapidas. */
public final class ChatMapper {
    private ChatMapper() {
    }

    public static CanalChatDTO toDTO(CanalChat chat) {
        CanalChatDTO dto = new CanalChatDTO();
        dto.setIdChat(chat.getIdChat());
        dto.setEstado(chat.getEstado() == null ? null : chat.getEstado().name());
        dto.setFechaSolicitud(Fechas.aTexto(chat.getFechaSolicitud()));
        if (chat.getAnuncio() != null) {
            dto.setIdAnuncio(chat.getAnuncio().getIdAnuncio());
            dto.setTituloAnuncio(chat.getAnuncio().getTitulo());
            if (chat.getAnuncio().getVendedor() != null) {
                dto.setIdVendedor(chat.getAnuncio().getVendedor().getIdUsuario());
            }
        }
        if (chat.getComprador() != null) {
            dto.setIdComprador(chat.getComprador().getIdUsuario());
            dto.setNombreComprador(UsuarioMapper.nombre(chat.getComprador()));
        }
        return dto;
    }

    public static List<CanalChatDTO> chatsToDTOs(List<CanalChat> chats) {
        List<CanalChatDTO> dtos = new ArrayList<>();
        for (CanalChat chat : chats) {
            dtos.add(toDTO(chat));
        }
        return dtos;
    }

    public static CanalChat toModelo(CanalChatDTO dto) {
        return new CanalChat(AnuncioMapper.referencia(dto.getIdAnuncio()), UsuarioMapper.referencia(dto.getIdComprador()));
    }

    public static MensajeDTO toDTO(Mensaje mensaje) {
        MensajeDTO dto = new MensajeDTO();
        dto.setIdMensaje(mensaje.getIdMensaje());
        dto.setContenido(mensaje.getContenido());
        dto.setFechaHora(Fechas.aTexto(mensaje.getFechaHora()));
        dto.setLeido(mensaje.isLeido());
        if (mensaje.getCanalChat() != null) {
            dto.setIdChat(mensaje.getCanalChat().getIdChat());
        }
        if (mensaje.getEmisor() != null) {
            dto.setIdEmisor(mensaje.getEmisor().getIdUsuario());
            dto.setNombreEmisor(UsuarioMapper.nombre(mensaje.getEmisor()));
        }
        return dto;
    }

    public static List<MensajeDTO> mensajesToDTOs(List<Mensaje> mensajes) {
        List<MensajeDTO> dtos = new ArrayList<>();
        for (Mensaje mensaje : mensajes) {
            dtos.add(toDTO(mensaje));
        }
        return dtos;
    }

    public static Mensaje toModelo(MensajeDTO dto, int idChat) {
        CanalChat chat = new CanalChat();
        chat.setIdChat(idChat);
        return new Mensaje(dto.getContenido(), chat, UsuarioMapper.referencia(dto.getIdEmisor()));
    }

    public static RespuestaRapidaDTO toDTO(RespuestaRapida respuesta) {
        RespuestaRapidaDTO dto = new RespuestaRapidaDTO();
        dto.setIdRespuesta(respuesta.getIdRespuesta());
        dto.setTexto(respuesta.getTexto());
        if (respuesta.getCreador() != null) {
            dto.setIdCreador(respuesta.getCreador().getIdUsuario());
        }
        return dto;
    }

    public static List<RespuestaRapidaDTO> respuestasToDTOs(List<RespuestaRapida> respuestas) {
        List<RespuestaRapidaDTO> dtos = new ArrayList<>();
        for (RespuestaRapida respuesta : respuestas) {
            dtos.add(toDTO(respuesta));
        }
        return dtos;
    }

    public static RespuestaRapida toModelo(RespuestaRapidaDTO dto) {
        RespuestaRapida respuesta = new RespuestaRapida(dto.getTexto(), UsuarioMapper.referencia(dto.getIdCreador()));
        respuesta.setIdRespuesta(dto.getIdRespuesta());
        return respuesta;
    }
}
