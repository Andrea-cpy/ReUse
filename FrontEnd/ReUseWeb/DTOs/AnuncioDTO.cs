namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.AnuncioDTO (JSON en camelCase).</summary>
public class AnuncioDTO
{
    public int IdAnuncio { get; set; }
    public string? Titulo { get; set; }
    public decimal Precio { get; set; }
    public string? Descripcion { get; set; }
    public string? Condicion { get; set; }
    public string? Estado { get; set; }
    public string? FechaPublicacion { get; set; }
    public int IdVendedor { get; set; }
    public string? NombreVendedor { get; set; }
    public int IdMaterial { get; set; }
    public string? TituloMaterial { get; set; }
    public int IdCategoria { get; set; }
    public string? NombreCategoria { get; set; }
    public List<string> Imagenes { get; set; } = [];
}
