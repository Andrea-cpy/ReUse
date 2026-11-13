namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.CitaEntregaDTO (JSON en camelCase).</summary>
public class CitaEntregaDTO
{
    public int IdCita { get; set; }
    public string? FechaHora { get; set; }
    public string? Estado { get; set; }
    public int IdPuntoEntrega { get; set; }
    public string? NombrePuntoEntrega { get; set; }
    public int IdTransaccion { get; set; }
}
