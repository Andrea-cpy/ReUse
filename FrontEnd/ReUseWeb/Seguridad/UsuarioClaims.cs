using System.Security.Claims;

namespace ReUseWeb.Seguridad;

/// <summary>Lectura de los datos del usuario que se guardaron en la cookie al iniciar sesión.</summary>
public static class UsuarioClaims
{
    public const string Codigo = "reuse:codigo";
    public const string Carrera = "reuse:carrera";

    public static int IdUsuario(this ClaimsPrincipal usuario)
    {
        string? valor = usuario.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        return int.TryParse(valor, out int id) ? id : 0;
    }

    public static string NombreCompleto(this ClaimsPrincipal usuario) => usuario.Identity?.Name ?? "";

    public static string CodigoPucp(this ClaimsPrincipal usuario) => usuario.FindFirst(Codigo)?.Value ?? "";

    public static bool EsAdministrador(this ClaimsPrincipal usuario) => usuario.IsInRole(Roles.Administrador);
}
