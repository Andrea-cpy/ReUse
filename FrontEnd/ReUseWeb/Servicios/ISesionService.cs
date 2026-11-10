namespace ReUseWeb.Servicios;

/// <summary>Inicio y cierre de sesión con cookie (patrón visto en clase).</summary>
public interface ISesionService
{
    /// <summary>Valida contra el backend y crea la cookie. Devuelve la ruta a la que se redirige.</summary>
    Task<string> IniciarSesion(HttpContext context, string correo, string contrasena);

    Task CerrarSesion(HttpContext context);
}
