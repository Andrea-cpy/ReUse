namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.InsigniaDTO (JSON en camelCase).</summary>
public class InsigniaDTO
{
    public int IdInsignia { get; set; }
    public string Nombre { get; set; } = "";
    public string Descripcion { get; set; } = "";
    public string? Icono { get; set; }
    public string Tipo { get; set; } = "GENERAL";
    public bool Activo { get; set; } = true;
    public List<ReglaInsigniaDTO> Reglas { get; set; } = [];
}
