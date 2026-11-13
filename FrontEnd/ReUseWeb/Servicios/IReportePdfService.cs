namespace ReUseWeb.Servicios;

/// <summary>RF-04: reportes PDF que genera el backend Java con JasperReports.</summary>
public interface IReportePdfService
{
    /// <summary>nombre: incidentes | transacciones | usuarios. Filtros admitidos: estado y tipo.</summary>
    Task<byte[]> Descargar(string nombre, int idAdministrador, IDictionary<string, string?> filtros);
}
