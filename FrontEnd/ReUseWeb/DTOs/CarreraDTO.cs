namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.CarreraDTO (JSON en camelCase).</summary>
public class CarreraDTO
{
    public int IdCarrera { get; set; }
    public string? Nombre { get; set; }
    public int IdFacultad { get; set; }
    public string? NombreFacultad { get; set; }
}
