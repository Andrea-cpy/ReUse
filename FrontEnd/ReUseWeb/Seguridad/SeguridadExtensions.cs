using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication.OpenIdConnect;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Protocols.OpenIdConnect;
using ReUseWeb.Servicios;
using ReUseWeb.Servicios.Impl;

namespace ReUseWeb.Seguridad;

/// <summary>Autenticación con cookie y endpoints de login/logout (patrón de la sesión de clase).</summary>
public static class SeguridadExtensions
{
    /// <summary>Esquema OpenID Connect que redirige a la pantalla de Google.</summary>
    public const string EsquemaGoogle = "Google";

    /// <summary>Cookie temporal donde queda el resultado de Google hasta que el backend lo valida.</summary>
    public const string EsquemaGoogleExterno = "GoogleExterno";

    /// <summary>True si appsettings / user-secrets tienen Google:ClientId y Google:ClientSecret.</summary>
    public static bool GoogleConfigurado(IConfiguration configuracion) =>
        !string.IsNullOrWhiteSpace(configuracion["Google:ClientId"])
        && !string.IsNullOrWhiteSpace(configuracion["Google:ClientSecret"]);

    public static void AddSeguridad(this IServiceCollection services, IConfiguration configuracion)
    {
        AuthenticationBuilder autenticacion = services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
            .AddCookie(opciones =>
            {
                opciones.LoginPath = "/login";
                opciones.AccessDeniedPath = "/";
                opciones.ExpireTimeSpan = TimeSpan.FromHours(8);
            });

        if (GoogleConfigurado(configuracion))
        {
            AgregarGoogle(autenticacion, configuracion);
        }

        services.AddAuthorization();
        services.AddCascadingAuthenticationState();
        services.AddHttpContextAccessor();
        services.AddMemoryCache();
        services.AddSingleton<RegistrosGooglePendientes>();
        services.AddScoped<ISesionService, SesionServiceImpl>();
    }

    /// <summary>
    /// Ingreso con Google por OpenID Connect (flujo authorization code). Google devuelve un ID token
    /// que se guarda en una cookie temporal; luego el backend Java valida su firma, el Client ID y
    /// que el correo sea @pucp.edu.pe antes de abrir la sesión de ReUse.
    /// </summary>
    private static void AgregarGoogle(AuthenticationBuilder autenticacion, IConfiguration configuracion)
    {
        autenticacion
            .AddCookie(EsquemaGoogleExterno, opciones =>
            {
                opciones.Cookie.Name = "ReUse.GoogleExterno";
                opciones.ExpireTimeSpan = TimeSpan.FromMinutes(10);
            })
            .AddOpenIdConnect(EsquemaGoogle, "Google", opciones =>
            {
                opciones.Authority = "https://accounts.google.com";
                opciones.ClientId = configuracion["Google:ClientId"];
                opciones.ClientSecret = configuracion["Google:ClientSecret"];
                opciones.ResponseType = OpenIdConnectResponseType.Code;
                // Google vuelve con una redireccion GET (no con un POST entre sitios): asi las cookies de
                // correlacion y nonce (SameSite=Lax) llegan tambien en http://localhost:5180, donde el
                // navegador descartaria las SameSite=None por no ser HTTPS ("Correlation failed").
                opciones.ResponseMode = OpenIdConnectResponseMode.Query;
                opciones.CorrelationCookie.SameSite = SameSiteMode.Lax;
                opciones.NonceCookie.SameSite = SameSiteMode.Lax;
                opciones.CorrelationCookie.SecurePolicy = CookieSecurePolicy.SameAsRequest;
                opciones.NonceCookie.SecurePolicy = CookieSecurePolicy.SameAsRequest;
                opciones.CallbackPath = "/signin-google";
                opciones.SignInScheme = EsquemaGoogleExterno;
                opciones.SaveTokens = true; // deja el id_token disponible para enviarlo al backend
                opciones.MapInboundClaims = false;
                opciones.Scope.Clear();
                opciones.Scope.Add("openid");
                opciones.Scope.Add("email");
                opciones.Scope.Add("profile");
                opciones.Events.OnRedirectToIdentityProvider = contexto =>
                {
                    // hd: Google muestra primero las cuentas @pucp.edu.pe. Es solo una sugerencia;
                    // la regla del dominio la hace cumplir el backend al validar el token.
                    contexto.ProtocolMessage.SetParameter("hd", "pucp.edu.pe");
                    contexto.ProtocolMessage.Prompt = "select_account";
                    return Task.CompletedTask;
                };
                opciones.Events.OnAccessDenied = contexto =>
                {
                    contexto.Response.Redirect(RutaError("Cancelaste el ingreso con Google."));
                    contexto.HandleResponse();
                    return Task.CompletedTask;
                };
                opciones.Events.OnRemoteFailure = contexto =>
                {
                    contexto.Response.Redirect(RutaError("No se pudo completar el ingreso con Google. Vuelve a intentarlo."));
                    contexto.HandleResponse();
                    return Task.CompletedTask;
                };
            });
    }

    public static void MapSesionEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapPost("/cuenta/login", IniciarSesion);
        app.MapPost("/cuenta/logout", CerrarSesion);
        app.MapPost("/cuenta/google", IrAGoogle);
        app.MapGet("/cuenta/google/continuar", ContinuarConGoogle);
        app.MapGet("/cuenta/google/entrar", EntrarTrasRegistroGoogle);
    }

    public static string RutaError(string mensaje) => $"/login?error={Uri.EscapeDataString(mensaje)}";

    private static async Task<IResult> IniciarSesion(HttpContext context, ISesionService sesionService,
        [FromForm] string correo, [FromForm] string contrasena)
    {
        string destino = await sesionService.IniciarSesion(context, correo, contrasena);
        return Results.Redirect(destino);
    }

    private static async Task<IResult> CerrarSesion(HttpContext context, ISesionService sesionService)
    {
        await sesionService.CerrarSesion(context);
        return Results.Redirect("/login");
    }

    /// <summary>Botón "Continuar con Google": lleva a la pantalla de Google para elegir la cuenta.</summary>
    private static IResult IrAGoogle(IConfiguration configuracion, ILoggerFactory logs)
    {
        if (!GoogleConfigurado(configuracion))
        {
            // El detalle de configuración va al log del servidor, no a la pantalla.
            logs.CreateLogger("ReUseWeb.Seguridad").LogWarning(
                "Ingreso con Google sin configurar: faltan Google:ClientId y/o Google:ClientSecret (ver FrontEnd/README.md).");
            return Results.Redirect(RutaError(
                "El ingreso con Google no está disponible por ahora. Usa tu correo y contraseña."));
        }
        return Results.Challenge(new AuthenticationProperties { RedirectUri = "/cuenta/google/continuar" },
            [EsquemaGoogle]);
    }

    private static async Task<IResult> ContinuarConGoogle(HttpContext context, ISesionService sesionService) =>
        Results.Redirect(await sesionService.ContinuarConGoogle(context));

    private static async Task<IResult> EntrarTrasRegistroGoogle(HttpContext context, ISesionService sesionService,
        [FromQuery] string? t) =>
        Results.Redirect(await sesionService.EntrarTrasRegistroGoogle(context, t ?? ""));
}
