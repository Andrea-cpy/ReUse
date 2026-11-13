namespace ReUseWeb.Servicios;

/// <summary>Inicio y cierre de sesión con cookie (patrón visto en clase).</summary>
public interface ISesionService
{
    /// <summary>Valida contra el backend y crea la cookie. Devuelve la ruta a la que se redirige.</summary>
    Task<string> IniciarSesion(HttpContext context, string correo, string contrasena);

    /// <summary>
    /// Vuelta de Google: envía el ID token al backend. Si el correo ya tiene cuenta, abre la sesión;
    /// si no, devuelve la ruta de /registro/google para completar los datos.
    /// </summary>
    Task<string> ContinuarConGoogle(HttpContext context);

    /// <summary>Abre la sesión de quien acaba de registrarse con Google (ticket de /registro/google).</summary>
    Task<string> EntrarTrasRegistroGoogle(HttpContext context, string ticket);

    Task CerrarSesion(HttpContext context);
}
