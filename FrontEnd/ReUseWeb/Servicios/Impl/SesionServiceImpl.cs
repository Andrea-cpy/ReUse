using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using ReUseWeb.DTOs;
using ReUseWeb.Seguridad;
using ReUseWeb.Servicios.Rest;

namespace ReUseWeb.Servicios.Impl;

public class SesionServiceImpl : ISesionService
{
    // Une el ticket de /registro/google con el navegador que inició el ingreso con Google.
    private const string CookieRegistroGoogle = "ReUse.RegistroGoogle";

    private readonly string urlAuth;
    private readonly RegistrosGooglePendientes pendientes;

    public SesionServiceImpl(IConfiguration configuracion, RegistrosGooglePendientes pendientes)
    {
        urlAuth = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/auth";
        this.pendientes = pendientes;
    }

    public async Task<string> IniciarSesion(HttpContext context, string correo, string contrasena)
    {
        UsuarioDTO usuario;
        try
        {
            // POST /auth/login: el backend compara el hash BCrypt y valida que la cuenta esté ACTIVA
            usuario = await HttpUtils.DoPost<CredencialesDTO, UsuarioDTO>($"{urlAuth}/login",
                new CredencialesDTO { Correo = correo.Trim(), Contrasena = contrasena });
        }
        catch (ServicioException ex)
        {
            return SeguridadExtensions.RutaError(ex.Message);
        }
        return await CrearSesion(context, usuario);
    }

    public async Task<string> ContinuarConGoogle(HttpContext context)
    {
        AuthenticateResult externo = await context.AuthenticateAsync(SeguridadExtensions.EsquemaGoogleExterno);
        await context.SignOutAsync(SeguridadExtensions.EsquemaGoogleExterno);
        string? idToken = externo.Succeeded ? externo.Properties?.GetTokenValue("id_token") : null;
        if (string.IsNullOrEmpty(idToken))
        {
            return SeguridadExtensions.RutaError("No se pudo completar el ingreso con Google. Vuelve a intentarlo.");
        }
        return await IngresarConGoogle(context, idToken);
    }

    public async Task<string> EntrarTrasRegistroGoogle(HttpContext context, string ticket)
    {
        RegistroGooglePendiente? pendiente = pendientes.Obtener(ticket);
        bool mismoNavegador = context.Request.Cookies[CookieRegistroGoogle] == ticket;
        context.Response.Cookies.Delete(CookieRegistroGoogle);
        if (pendiente is null || !mismoNavegador)
        {
            return SeguridadExtensions.RutaError("Tu cuenta fue creada, pero la sesión con Google expiró. Vuelve a ingresar con Google.");
        }
        pendientes.Quitar(ticket);
        return await IngresarConGoogle(context, pendiente.IdToken);
    }

    public async Task CerrarSesion(HttpContext context)
    {
        await context.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
    }

    /// <summary>POST /auth/google: el backend valida el token (firma, Client ID, dominio pucp.edu.pe).</summary>
    private async Task<string> IngresarConGoogle(HttpContext context, string idToken)
    {
        IngresoGoogleDTO ingreso;
        try
        {
            ingreso = await HttpUtils.DoPost<GoogleTokenDTO, IngresoGoogleDTO>($"{urlAuth}/google",
                new GoogleTokenDTO { IdToken = idToken });
        }
        catch (ServicioException ex)
        {
            return SeguridadExtensions.RutaError(ex.Message);
        }

        if (ingreso.Registrado && ingreso.Usuario is not null)
        {
            return await CrearSesion(context, ingreso.Usuario);
        }

        // Primera vez con Google: se guarda el token en el servidor y se piden los datos que faltan.
        string ticket = pendientes.Crear(idToken, ingreso.Usuario ?? new UsuarioDTO());
        context.Response.Cookies.Append(CookieRegistroGoogle, ticket, new CookieOptions
        {
            HttpOnly = true,
            Secure = context.Request.IsHttps,
            SameSite = SameSiteMode.Lax,
            MaxAge = TimeSpan.FromMinutes(15)
        });
        return $"/registro/google?t={ticket}";
    }

    private static async Task<string> CrearSesion(HttpContext context, UsuarioDTO usuario)
    {
        string rol = usuario.Administrador ? Roles.Administrador : Roles.Estudiante;
        string nombre = $"{usuario.Nombres} {usuario.Apellidos}".Trim();
        List<Claim> claims =
        [
            new Claim(ClaimTypes.NameIdentifier, usuario.IdUsuario.ToString()),
            new Claim(ClaimTypes.Name, nombre),
            new Claim(ClaimTypes.Email, usuario.CorreoInstitucional ?? ""),
            new Claim(ClaimTypes.Role, rol),
            new Claim(UsuarioClaims.Codigo, usuario.CodigoPUCP ?? ""),
            new Claim(UsuarioClaims.Carrera, usuario.NombreCarrera ?? "")
        ];
        ClaimsIdentity identidad = new(claims, CookieAuthenticationDefaults.AuthenticationScheme);
        await context.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(identidad));
        return usuario.Administrador ? "/admin" : "/";
    }
}
