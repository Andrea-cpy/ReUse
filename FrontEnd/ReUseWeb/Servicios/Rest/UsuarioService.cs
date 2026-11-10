using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /usuarios — RF-01: registro, perfil, contraseña y estado de cuenta.</summary>
public class UsuarioService : IUsuarioService
{
    private string BaseUrl { get; }

    public UsuarioService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/usuarios";
    }

    public Task<List<UsuarioDTO>> Listar() => HttpUtils.DoGet<List<UsuarioDTO>>(BaseUrl);

    public Task<UsuarioDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<UsuarioDTO>($"{BaseUrl}/{id}");

    public Task<UsuarioDTO> Registrar(UsuarioDTO usuario) => HttpUtils.DoPost<UsuarioDTO, UsuarioDTO>(BaseUrl, usuario);

    public Task<UsuarioDTO> ActualizarPerfil(UsuarioDTO usuario) => HttpUtils.DoPut<UsuarioDTO, UsuarioDTO>($"{BaseUrl}/{usuario.IdUsuario}", usuario);

    public Task CambiarContrasena(int id, string actual, string nueva) => HttpUtils.DoPut($"{BaseUrl}/{id}/contrasena", new CambioContrasenaDTO { ContrasenaActual = actual, ContrasenaNueva = nueva });

    public Task<UsuarioDTO> Verificar(int id) => HttpUtils.DoPutAccion<UsuarioDTO>($"{BaseUrl}/{id}/verificar");

    public Task<UsuarioDTO> Suspender(int id) => HttpUtils.DoPutAccion<UsuarioDTO>($"{BaseUrl}/{id}/suspender");

    public Task<UsuarioDTO> Reactivar(int id) => HttpUtils.DoPutAccion<UsuarioDTO>($"{BaseUrl}/{id}/reactivar");
}
