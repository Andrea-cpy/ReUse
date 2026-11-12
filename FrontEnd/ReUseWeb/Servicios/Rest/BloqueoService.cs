using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /bloqueos — RF-10: al bloquear se cierran los chats y se ocultan los anuncios entre ambos.</summary>
public class BloqueoService : IBloqueoService
{
    private string BaseUrl { get; }

    public BloqueoService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/bloqueos";
    }

    public Task<List<BloqueoDTO>> Listar(int idUsuario) => HttpUtils.DoGet<List<BloqueoDTO>>($"{BaseUrl}?usuario={idUsuario}");

    public Task<EstadoBloqueoDTO> Entre(int idUsuarioA, int idUsuarioB) =>
        HttpUtils.DoGet<EstadoBloqueoDTO>($"{BaseUrl}/entre?usuarioA={idUsuarioA}&usuarioB={idUsuarioB}");

    public Task<BloqueoDTO> Bloquear(int idBloqueador, int idBloqueado) =>
        HttpUtils.DoPost<BloqueoDTO, BloqueoDTO>(BaseUrl, new BloqueoDTO { IdBloqueador = idBloqueador, IdBloqueado = idBloqueado });

    public Task<BloqueoDTO> Desbloquear(int idBloqueo) => HttpUtils.DoPutAccion<BloqueoDTO>($"{BaseUrl}/{idBloqueo}/desbloquear");
}
