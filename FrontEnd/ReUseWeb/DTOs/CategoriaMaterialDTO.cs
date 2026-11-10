namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.CategoriaMaterialDTO (JSON en camelCase).</summary>
public class CategoriaMaterialDTO
{
    public int IdCategoria { get; set; }
    public string? Nombre { get; set; }
    public string? Descripcion { get; set; }
    public bool Activo { get; set; }
}
