namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.ReputacionDTO (JSON en camelCase).</summary>
public class ReputacionDTO
{
    public int IdUsuario { get; set; }
    public string? Nivel { get; set; }
    public decimal ReputacionVendedor { get; set; }
    public decimal ReputacionComprador { get; set; }
    public int VentasCompletadas { get; set; }
    public int ComprasCompletadas { get; set; }
    public int TransaccionesCompletadas { get; set; }
    public double CalificacionPromedio { get; set; }
    public int SancionesRecientes { get; set; }
}
