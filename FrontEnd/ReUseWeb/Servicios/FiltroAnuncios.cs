using System.Globalization;
using Microsoft.AspNetCore.WebUtilities;

namespace ReUseWeb.Servicios;

/// <summary>Filtros del catálogo (RF-02). Solo se envían al backend los que tienen valor.</summary>
public class FiltroAnuncios
{
    public string? Texto { get; set; }
    public int IdCategoria { get; set; }
    public string? Condicion { get; set; }
    public decimal? PrecioMinimo { get; set; }
    public decimal? PrecioMaximo { get; set; }
    /// <summary>El usuario que busca: sus propios anuncios no aparecen en el catálogo.</summary>
    public int IdUsuarioExcluido { get; set; }

    public string AUrl(string baseUrl)
    {
        Dictionary<string, string?> parametros = new();
        if (!string.IsNullOrWhiteSpace(Texto)) parametros["texto"] = Texto.Trim();
        if (IdCategoria > 0) parametros["categoria"] = IdCategoria.ToString(CultureInfo.InvariantCulture);
        if (!string.IsNullOrWhiteSpace(Condicion)) parametros["condicion"] = Condicion;
        if (PrecioMinimo.HasValue) parametros["precioMin"] = PrecioMinimo.Value.ToString(CultureInfo.InvariantCulture);
        if (PrecioMaximo.HasValue) parametros["precioMax"] = PrecioMaximo.Value.ToString(CultureInfo.InvariantCulture);
        if (IdUsuarioExcluido > 0) parametros["excluirUsuario"] = IdUsuarioExcluido.ToString(CultureInfo.InvariantCulture);
        return QueryHelpers.AddQueryString(baseUrl, parametros);
    }
}
