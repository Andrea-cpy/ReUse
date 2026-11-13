using System.Net;
using ReUseWeb.Seguridad;

namespace ReUseWeb.Servicios;

/// <summary>
/// RF-04: descarga de los reportes PDF. Blazor Server no puede entregar un archivo desde un componente,
/// así que el botón apunta a este endpoint: llama a GET /reportes/{nombre} del backend Java (JasperReports)
/// y devuelve el PDF al navegador como archivo adjunto. Solo para administradores.
/// </summary>
public static class DescargasExtensions
{
    public static void MapDescargasEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/descargas/reportes/{nombre}", DescargarReporte)
            .RequireAuthorization(politica => politica.RequireRole(Roles.Administrador));
    }

    private static async Task<IResult> DescargarReporte(string nombre, HttpContext context, IReportePdfService reportes)
    {
        Dictionary<string, string?> filtros = context.Request.Query
            .ToDictionary(q => q.Key, q => (string?)q.Value.ToString());
        try
        {
            byte[] pdf = await reportes.Descargar(nombre, context.User.IdUsuario(), filtros);
            string archivo = $"reuse-{nombre}-{DateTime.Now:yyyyMMdd-HHmm}.pdf";
            return Results.File(pdf, "application/pdf", archivo);
        }
        catch (ServicioException ex)
        {
            // Página mínima con el mensaje del backend (la descarga se abre en otra pestaña).
            string html = $"<!doctype html><meta charset=\"utf-8\"><title>Reporte no disponible</title>" +
                          $"<body style=\"font-family:sans-serif;padding:2rem\"><h1>No se pudo generar el reporte</h1>" +
                          $"<p>{WebUtility.HtmlEncode(ex.Message)}</p></body>";
            return Results.Content(html, "text/html; charset=utf-8", statusCode: ex.CodigoHttp > 0 ? ex.CodigoHttp : 502);
        }
    }
}
