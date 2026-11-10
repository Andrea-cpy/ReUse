namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.FavoritoDTO (JSON en camelCase).</summary>
public class FavoritoDTO
{
    public int IdFavorito { get; set; }
    public string? FechaGuardado { get; set; }
    public int IdUsuario { get; set; }
    public int IdAnuncio { get; set; }
    public string? TituloAnuncio { get; set; }
    public decimal PrecioAnuncio { get; set; }
    public string? EstadoAnuncio { get; set; }
}
