namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.CanalChatDTO (JSON en camelCase).</summary>
public class CanalChatDTO
{
    public int IdChat { get; set; }
    public string? Estado { get; set; }
    public string? FechaSolicitud { get; set; }
    public int IdAnuncio { get; set; }
    public string? TituloAnuncio { get; set; }
    public int IdComprador { get; set; }
    public string? NombreComprador { get; set; }
    public int IdVendedor { get; set; }
    public string? NombreVendedor { get; set; }
}
