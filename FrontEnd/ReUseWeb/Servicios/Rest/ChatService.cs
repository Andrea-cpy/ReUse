using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /chats — RF-09 solicitud de chat y RF-08 mensajería.</summary>
public class ChatService : IChatService
{
    private string BaseUrl { get; }

    public ChatService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/chats";
    }

    public Task<List<CanalChatDTO>> ListarPorUsuario(int idUsuario) => HttpUtils.DoGet<List<CanalChatDTO>>($"{BaseUrl}?usuario={idUsuario}");

    public Task<CanalChatDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<CanalChatDTO>($"{BaseUrl}/{id}");

    public Task<CanalChatDTO> Solicitar(int idAnuncio, int idComprador) => HttpUtils.DoPost<CanalChatDTO, CanalChatDTO>(BaseUrl, new CanalChatDTO { IdAnuncio = idAnuncio, IdComprador = idComprador });

    public Task<CanalChatDTO> Aceptar(int id) => HttpUtils.DoPutAccion<CanalChatDTO>($"{BaseUrl}/{id}/aceptar");

    public Task<CanalChatDTO> Rechazar(int id) => HttpUtils.DoPutAccion<CanalChatDTO>($"{BaseUrl}/{id}/rechazar");

    public Task<CanalChatDTO> Cerrar(int id) => HttpUtils.DoPutAccion<CanalChatDTO>($"{BaseUrl}/{id}/cerrar");

    public Task<List<MensajeDTO>> ListarMensajes(int idChat) => HttpUtils.DoGet<List<MensajeDTO>>($"{BaseUrl}/{idChat}/mensajes");

    public Task<MensajeDTO> Enviar(int idChat, int idEmisor, string contenido) => HttpUtils.DoPost<MensajeDTO, MensajeDTO>($"{BaseUrl}/{idChat}/mensajes", new MensajeDTO { IdEmisor = idEmisor, Contenido = contenido });

    public Task<MensajeDTO> EnviarRespuestaRapida(int idChat, int idEmisor, int idRespuesta) => HttpUtils.DoPost<MensajeDTO>($"{BaseUrl}/{idChat}/respuestas-rapidas/{idRespuesta}?emisor={idEmisor}");
}
