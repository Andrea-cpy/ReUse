namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.OfertaDTO (JSON en camelCase).</summary>
public class OfertaDTO
{
    public int IdOferta { get; set; }
    public decimal MontoPropuesto { get; set; }
    public string? Estado { get; set; }
    public string? Fecha { get; set; }
    public int IdComprador { get; set; }
    public string? NombreComprador { get; set; }
    public int IdAnuncio { get; set; }
    public string? TituloAnuncio { get; set; }
    public decimal PrecioAnuncio { get; set; }
    public int IdVendedor { get; set; }
}
