using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-15 / RF-18: calificar transacciones completadas y ver calificaciones.</summary>
public interface ICalificacionService
{
    Task<List<CalificacionDTO>> Recibidas(int idUsuario);
    Task<List<CalificacionDTO>> Hechas(int idUsuario);
    Task<CalificacionDTO> Calificar(CalificacionDTO calificacion);
}
