namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.ReglaInsigniaDTO (JSON en camelCase).</summary>
public class ReglaInsigniaDTO
{
    public int IdRegla { get; set; }
    public string TipoMetrica { get; set; } = "VENTAS_COMPLETADAS";
    public string Operador { get; set; } = "MAYOR_O_IGUAL";
    public double ValorObjetivo { get; set; }
}
