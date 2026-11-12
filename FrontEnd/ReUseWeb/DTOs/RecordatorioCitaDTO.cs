namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.RecordatorioCitaDTO (JSON en camelCase).</summary>
public class RecordatorioCitaDTO
{
    public int IdCita { get; set; }
    public int IdTransaccion { get; set; }
    public int IdAnuncio { get; set; }
    public string? TituloAnuncio { get; set; }
    public string? FechaHora { get; set; }
    public string? PuntoEntrega { get; set; }
    public string? Rol { get; set; }
    public string? Contraparte { get; set; }
}
