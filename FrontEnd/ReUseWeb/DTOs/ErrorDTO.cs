namespace ReUseWeb.DTOs;

/// <summary>Cuerpo de error que devuelve el backend: {"error": "...", "idSolicitud": "..."}.</summary>
public class ErrorDTO
{
    public string? Error { get; set; }
    public string? IdSolicitud { get; set; }
}
