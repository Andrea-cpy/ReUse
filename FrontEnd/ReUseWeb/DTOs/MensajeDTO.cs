namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.MensajeDTO (JSON en camelCase).</summary>
public class MensajeDTO
{
    public int IdMensaje { get; set; }
    public string? Contenido { get; set; }
    public string? FechaHora { get; set; }
    public bool Leido { get; set; }
    public int IdChat { get; set; }
    public int IdEmisor { get; set; }
    public string? NombreEmisor { get; set; }
}
