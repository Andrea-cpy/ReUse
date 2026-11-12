namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /reportes — devuelve los bytes del PDF (application/pdf) generado con JasperReports.</summary>
public class ReportePdfService : IReportePdfService
{
    private static readonly HashSet<string> Permitidos = ["incidentes", "transacciones", "usuarios"];
    private static readonly HashSet<string> FiltrosPermitidos = ["estado", "tipo"];
    private string BaseUrl { get; }

    public ReportePdfService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/reportes";
    }

    public Task<byte[]> Descargar(string nombre, int idAdministrador, IDictionary<string, string?> filtros)
    {
        if (!Permitidos.Contains(nombre))
        {
            throw new ServicioException($"No existe el reporte «{nombre}».", 404);
        }
        List<string> parametros = [$"administrador={idAdministrador}"];
        foreach (KeyValuePair<string, string?> filtro in filtros)
        {
            if (FiltrosPermitidos.Contains(filtro.Key) && !string.IsNullOrWhiteSpace(filtro.Value))
            {
                parametros.Add($"{filtro.Key}={Uri.EscapeDataString(filtro.Value)}");
            }
        }
        return HttpUtils.DoGetBytes($"{BaseUrl}/{nombre}?{string.Join('&', parametros)}");
    }
}
