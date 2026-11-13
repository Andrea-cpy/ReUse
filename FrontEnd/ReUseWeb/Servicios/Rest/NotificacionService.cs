using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /notificaciones — bandeja y RF-12 (recordatorios de cita).</summary>
public class NotificacionService : INotificacionService
{
    private string BaseUrl { get; }

    public NotificacionService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/notificaciones";
    }

    public Task<List<NotificacionDTO>> Listar(int idUsuario) => HttpUtils.DoGet<List<NotificacionDTO>>($"{BaseUrl}?usuario={idUsuario}");

    public Task MarcarLeida(int idNotificacion) => HttpUtils.DoPutAccion<NotificacionDTO>($"{BaseUrl}/{idNotificacion}/leer");

    public Task MarcarTodas(int idUsuario) => HttpUtils.DoPutAccion<Dictionary<string, int>>($"{BaseUrl}/leer-todas?usuario={idUsuario}");

    public Task<List<RecordatorioCitaDTO>> Recordatorios(int idUsuario, int horas = 24) =>
        HttpUtils.DoGet<List<RecordatorioCitaDTO>>($"{BaseUrl}/recordatorios?usuario={idUsuario}&horas={horas}");
}
