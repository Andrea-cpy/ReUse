using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-09 solicitud de chat y RF-08 mensajería.</summary>
public interface IChatService
{
    Task<List<CanalChatDTO>> ListarPorUsuario(int idUsuario);
    Task<CanalChatDTO?> Obtener(int id);
    Task<CanalChatDTO> Solicitar(int idAnuncio, int idComprador);
    Task<CanalChatDTO> Aceptar(int id);
    Task<CanalChatDTO> Rechazar(int id);
    Task<CanalChatDTO> Cerrar(int id);
    Task<List<MensajeDTO>> ListarMensajes(int idChat);
    Task<MensajeDTO> Enviar(int idChat, int idEmisor, string contenido);
    Task<MensajeDTO> EnviarRespuestaRapida(int idChat, int idEmisor, int idRespuesta);
}
