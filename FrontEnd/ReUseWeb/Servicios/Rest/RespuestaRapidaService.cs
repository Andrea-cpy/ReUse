using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /respuestas-rapidas — RF-16: plantillas de respuesta rápida.</summary>
public class RespuestaRapidaService : IRespuestaRapidaService
{
    private string BaseUrl { get; }

    public RespuestaRapidaService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/respuestas-rapidas";
    }

    public Task<List<RespuestaRapidaDTO>> ListarPorCreador(int idCreador) => HttpUtils.DoGet<List<RespuestaRapidaDTO>>($"{BaseUrl}?creador={idCreador}");

    public Task<RespuestaRapidaDTO> Registrar(int idCreador, string texto) => HttpUtils.DoPost<RespuestaRapidaDTO, RespuestaRapidaDTO>(BaseUrl, new RespuestaRapidaDTO { IdCreador = idCreador, Texto = texto });

    public Task<RespuestaRapidaDTO> Actualizar(RespuestaRapidaDTO respuesta) => HttpUtils.DoPut<RespuestaRapidaDTO, RespuestaRapidaDTO>($"{BaseUrl}/{respuesta.IdRespuesta}", respuesta);

    public Task Eliminar(int id) => HttpUtils.DoDelete($"{BaseUrl}/{id}");
}
