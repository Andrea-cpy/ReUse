using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-16: plantillas de respuesta rápida.</summary>
public interface IRespuestaRapidaService
{
    Task<List<RespuestaRapidaDTO>> ListarPorCreador(int idCreador);
    Task<RespuestaRapidaDTO> Registrar(int idCreador, string texto);
    Task<RespuestaRapidaDTO> Actualizar(RespuestaRapidaDTO respuesta);
    Task Eliminar(int id);
}
