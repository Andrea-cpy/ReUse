namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.MaterialAcademicoDTO (JSON en camelCase).</summary>
public class MaterialAcademicoDTO
{
    public int IdMaterial { get; set; }
    public string? Titulo { get; set; }
    public int IdCategoria { get; set; }
    public string? NombreCategoria { get; set; }
}
