using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /usuarios — RF-01: registro, perfil, contraseña y estado de cuenta.</summary>
public class UsuarioService : IUsuarioService
{
    private string BaseUrl { get; }
    private string AuthUrl { get; }

    public UsuarioService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/usuarios";
        AuthUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/auth";
    }

    public Task<List<UsuarioDTO>> Listar() => HttpUtils.DoGet<List<UsuarioDTO>>(BaseUrl);

    public Task<UsuarioDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<UsuarioDTO>($"{BaseUrl}/{id}");

    public Task<UsuarioDTO> Registrar(UsuarioDTO usuario) => HttpUtils.DoPost<UsuarioDTO, UsuarioDTO>(BaseUrl, usuario);

    /// <summary>RF-01 con Google: POST /auth/google/registro. La cuenta queda ACTIVA y verificada.</summary>
    public Task<UsuarioDTO> RegistrarConGoogle(string idToken, UsuarioDTO usuario) =>
        HttpUtils.DoPost<RegistroGoogleDTO, UsuarioDTO>($"{AuthUrl}/google/registro",
            new RegistroGoogleDTO { IdToken = idToken, Usuario = usuario });

    public Task<UsuarioDTO> ActualizarPerfil(UsuarioDTO usuario) => HttpUtils.DoPut<UsuarioDTO, UsuarioDTO>($"{BaseUrl}/{usuario.IdUsuario}", usuario);

    public Task CambiarContrasena(int id, string actual, string nueva) => HttpUtils.DoPut($"{BaseUrl}/{id}/contrasena", new CambioContrasenaDTO { ContrasenaActual = actual, ContrasenaNueva = nueva });

    public Task<UsuarioDTO> Verificar(int id) => HttpUtils.DoPutAccion<UsuarioDTO>($"{BaseUrl}/{id}/verificar");

    public Task<UsuarioDTO> Suspender(int id) => HttpUtils.DoPutAccion<UsuarioDTO>($"{BaseUrl}/{id}/suspender");

    public Task<UsuarioDTO> Reactivar(int id) => HttpUtils.DoPutAccion<UsuarioDTO>($"{BaseUrl}/{id}/reactivar");

    /// <summary>RF-01: recuperación de contraseña (POST /auth/recuperar y /auth/restablecer).</summary>
    public Task<RecuperacionRespuestaDTO> SolicitarRecuperacion(string correo) =>
        HttpUtils.DoPost<RecuperacionDTO, RecuperacionRespuestaDTO>($"{AuthUrl}/recuperar", new RecuperacionDTO { Correo = correo });

    public Task RestablecerContrasena(string correo, string codigo, string nueva) =>
        HttpUtils.DoPost<RecuperacionDTO, Dictionary<string, string>>($"{AuthUrl}/restablecer",
            new RecuperacionDTO { Correo = correo, Codigo = codigo, ContrasenaNueva = nueva });
}
