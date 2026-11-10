using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using ReUseWeb.Servicios;
using ReUseWeb.Servicios.Impl;

namespace ReUseWeb.Seguridad;

/// <summary>Autenticación con cookie y endpoints de login/logout (patrón de la sesión de clase).</summary>
public static class SeguridadExtensions
{
    public static void AddSeguridad(this IServiceCollection services)
    {
        services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
            .AddCookie(opciones =>
            {
                opciones.LoginPath = "/login";
                opciones.AccessDeniedPath = "/";
                opciones.ExpireTimeSpan = TimeSpan.FromHours(8);
            });
        services.AddAuthorization();
        services.AddCascadingAuthenticationState();
        services.AddHttpContextAccessor();
        services.AddScoped<ISesionService, SesionServiceImpl>();
    }

    public static void MapSesionEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapPost("/cuenta/login", IniciarSesion);
        app.MapPost("/cuenta/logout", CerrarSesion);
    }

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
}
