using System.Globalization;

namespace ReUseWeb.Utiles;

/// <summary>Ayudas de presentación: textos de enums, fechas ISO del backend, montos e iniciales.</summary>
public static class Formato
{
    private static readonly CultureInfo Peru = CultureInfo.GetCultureInfo("es-PE");

    /// <summary>"EN_NEGOCIACION" → "En negociacion".</summary>
    public static string Texto(string? valorEnum) =>
        string.IsNullOrEmpty(valorEnum) ? "" :
        char.ToUpper(valorEnum[0]) + valorEnum[1..].ToLower().Replace('_', ' ');

    public static string Soles(decimal monto) => "S/ " + monto.ToString("0.00", CultureInfo.InvariantCulture);

    public static DateTime? AFecha(string? iso) =>
        DateTime.TryParse(iso, CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime fecha) ? fecha : null;

    /// <summary>"2026-11-12T12:30" → "12/11/2026".</summary>
    public static string Fecha(string? iso) => AFecha(iso)?.ToString("dd/MM/yyyy", Peru) ?? "—";

    /// <summary>"2026-11-12T12:30" → "12/11/2026 12:30".</summary>
    public static string FechaHora(string? iso) => AFecha(iso)?.ToString("dd/MM/yyyy HH:mm", Peru) ?? "—";

    /// <summary>Formato que espera el backend Java (LocalDateTime.parse).</summary>
    public static string AIso(DateTime fecha) => fecha.ToString("yyyy-MM-dd'T'HH:mm", CultureInfo.InvariantCulture);

    /// <summary>"FABRICIO BENJAMIN DE LA CRUZ" → "Fabricio".</summary>
    public static string PrimerNombre(string? nombreCompleto)
    {
        if (string.IsNullOrWhiteSpace(nombreCompleto)) return "";
        string primero = nombreCompleto.Split(' ', StringSplitOptions.RemoveEmptyEntries)[0];
        return char.ToUpper(primero[0], Peru) + primero[1..].ToLower(Peru);
    }

    public static string Iniciales(string? nombre)
    {
        if (string.IsNullOrWhiteSpace(nombre)) return "?";
        string[] partes = nombre.Split(' ', StringSplitOptions.RemoveEmptyEntries);
        return partes.Length >= 2
            ? $"{partes[0][0]}{partes[1][0]}".ToUpper()
            : nombre[..Math.Min(2, nombre.Length)].ToUpper();
    }

    /// <summary>Color de fondo del marcador de imagen según la categoría.</summary>
    public static string TonoCategoria(string? categoria) => (categoria ?? "") switch
    {
        "Libro" => "tono-1",
        "Útiles" => "tono-2",
        "Maqueta" => "tono-3",
        "Calculadora" => "tono-5",
        "Equipo de estudio" => "tono-4",
        _ => "tono-6"
    };

    public static string IconoCategoria(string? categoria) => (categoria ?? "") switch
    {
        "Libro" => "bi-book",
        "Útiles" => "bi-rulers",
        "Maqueta" => "bi-buildings",
        "Calculadora" => "bi-calculator",
        "Equipo de estudio" => "bi-lamp",
        _ => "bi-box-seam"
    };

    public static readonly string[] Condiciones = { "NUEVO", "USADO", "REACONDICIONADO" };
}
