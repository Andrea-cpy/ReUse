namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.BloqueoDTO (JSON en camelCase).</summary>
public class BloqueoDTO
{
    public int IdBloqueo { get; set; }
    public string? Fecha { get; set; }
    public string? Estado { get; set; }
    public int IdBloqueador { get; set; }
    public string? NombreBloqueador { get; set; }
    public int IdBloqueado { get; set; }
    public string? NombreBloqueado { get; set; }
    public string? CodigoBloqueado { get; set; }
}
