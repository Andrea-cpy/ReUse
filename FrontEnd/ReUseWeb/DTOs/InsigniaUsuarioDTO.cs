namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.InsigniaUsuarioDTO (JSON en camelCase).</summary>
public class InsigniaUsuarioDTO
{
    public int IdInsigniaUsuario { get; set; }
    public int IdInsignia { get; set; }
    public string? Nombre { get; set; }
    public string? Descripcion { get; set; }
    public string? Icono { get; set; }
    public string? Tipo { get; set; }
    public string? FechaObtencion { get; set; }
}
