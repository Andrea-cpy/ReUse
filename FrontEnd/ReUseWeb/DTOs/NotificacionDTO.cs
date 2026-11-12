namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.NotificacionDTO (JSON en camelCase).</summary>
public class NotificacionDTO
{
    public int IdNotificacion { get; set; }
    public string? Mensaje { get; set; }
    public string? FechaHora { get; set; }
    public string? Tipo { get; set; }
    public string? Estado { get; set; }
    public int IdDestinatario { get; set; }
}
