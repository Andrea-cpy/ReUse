using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /calificaciones — al calificar, el backend recalcula la reputación y evalúa insignias.</summary>
public class CalificacionService : ICalificacionService
{
    private string BaseUrl { get; }

    public CalificacionService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/calificaciones";
    }

    public Task<List<CalificacionDTO>> Recibidas(int idUsuario) => HttpUtils.DoGet<List<CalificacionDTO>>($"{BaseUrl}?calificado={idUsuario}");

    public Task<List<CalificacionDTO>> Hechas(int idUsuario) => HttpUtils.DoGet<List<CalificacionDTO>>($"{BaseUrl}?calificador={idUsuario}");

    public Task<CalificacionDTO> Calificar(CalificacionDTO calificacion) => HttpUtils.DoPost<CalificacionDTO, CalificacionDTO>(BaseUrl, calificacion);
}
