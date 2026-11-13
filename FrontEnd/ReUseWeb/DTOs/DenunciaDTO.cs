namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.DenunciaDTO (JSON en camelCase).</summary>
public class DenunciaDTO
{
    public int IdReporte { get; set; }
    public string? Tipo { get; set; }
    public string? Motivo { get; set; }
    public string? Descripcion { get; set; }
    public string? FechaRegistro { get; set; }
    public string? EstadoRevision { get; set; }
    public string? FechaRevision { get; set; }
    public int IdDenunciante { get; set; }
    public string? NombreDenunciante { get; set; }
    public int IdRevisor { get; set; }
    public string? NombreRevisor { get; set; }
    public int IdDenunciado { get; set; }
    public string? NombreDenunciado { get; set; }
    public string? CodigoDenunciado { get; set; }
    public int IdTransaccion { get; set; }
    public int IdAnuncio { get; set; }
    public string? TituloAnuncio { get; set; }
    public string? EstadoAnuncio { get; set; }
    public int IdVendedorAnuncio { get; set; }
    public bool CuentaSuspendida { get; set; }
}
