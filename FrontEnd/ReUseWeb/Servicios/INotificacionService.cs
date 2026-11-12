using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>Notificaciones del usuario y RF-12: recordatorios de citas confirmadas.</summary>
public interface INotificacionService
{
    Task<List<NotificacionDTO>> Listar(int idUsuario);
    Task MarcarLeida(int idNotificacion);
    Task MarcarTodas(int idUsuario);
    Task<List<RecordatorioCitaDTO>> Recordatorios(int idUsuario, int horas = 24);
}
