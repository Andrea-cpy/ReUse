namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.TransaccionDTO (JSON en camelCase).</summary>
public class TransaccionDTO
{
    public int IdTransaccion { get; set; }
    public string? FechaInicio { get; set; }
    public string? FechaFin { get; set; }
    public string? Estado { get; set; }
    public bool ConfirmacionComprador { get; set; }
    public bool ConfirmacionVendedor { get; set; }
    public int IdAnuncio { get; set; }
    public string? TituloAnuncio { get; set; }
    public decimal PrecioAnuncio { get; set; }
    public int IdVendedor { get; set; }
    public string? NombreVendedor { get; set; }
    public int IdComprador { get; set; }
    public string? NombreComprador { get; set; }
    public decimal MontoAcordado { get; set; }
    public int IdCita { get; set; }
    public string? FechaCita { get; set; }
    public string? EstadoCita { get; set; }
}
