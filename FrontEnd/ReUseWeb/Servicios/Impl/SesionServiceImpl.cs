using System.Net.Http.Json;
using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using ReUseWeb.DTOs;
using ReUseWeb.Seguridad;
using ReUseWeb.Servicios.Rest;

namespace ReUseWeb.Servicios.Impl;

public class SesionServiceImpl : ISesionService
{
    private readonly string urlLogin;

    public SesionServiceImpl(IConfiguration configuracion)
    {
        urlLogin = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/auth/login";
    }

    public async Task<string> IniciarSesion(HttpContext context, string correo, string contrasena)
    {
        UsuarioDTO usuario;
        try
        {
            // POST /auth/login: el backend compara el hash BCrypt y valida que la cuenta esté ACTIVA
            usuario = await HttpUtils.DoPost<CredencialesDTO, UsuarioDTO>(urlLogin,
                new CredencialesDTO { Correo = correo.Trim(), Contrasena = contrasena });
        }
        catch (ServicioException ex)
        {
            return $"/login?error={Uri.EscapeDataString(ex.Message)}";
        }

        string rol = usuario.Administrador ? Roles.Administrador : Roles.Estudiante;
        string nombre = $"{usuario.Nombres} {usuario.ApellidoPaterno}".Trim();
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

    public async Task CerrarSesion(HttpContext context)
    {
        await context.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
    }
}
