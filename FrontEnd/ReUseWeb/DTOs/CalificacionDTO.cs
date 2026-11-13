namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.CalificacionDTO (JSON en camelCase).</summary>
public class CalificacionDTO
{
    public int IdCalificacion { get; set; }
    public int Puntaje { get; set; }
    public string? Comentario { get; set; }
    public string? Fecha { get; set; }
    public string? Tipo { get; set; }
    public int IdTransaccion { get; set; }
    public int IdCalificador { get; set; }
    public string? NombreCalificador { get; set; }
    public int IdCalificado { get; set; }
    public string? NombreCalificado { get; set; }
}
