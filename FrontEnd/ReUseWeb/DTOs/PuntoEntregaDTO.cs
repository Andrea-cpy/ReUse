namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.PuntoEntregaDTO (JSON en camelCase).</summary>
public class PuntoEntregaDTO
{
    public int IdPuntoEntrega { get; set; }
    public string? Nombre { get; set; }
    public string? Referencia { get; set; }
    public string? Ubicacion { get; set; }
    public bool Activo { get; set; }
}
