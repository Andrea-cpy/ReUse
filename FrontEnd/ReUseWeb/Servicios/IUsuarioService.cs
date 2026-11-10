using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-01: registro, perfil, contraseña y estado de cuenta.</summary>
public interface IUsuarioService
{
    Task<List<UsuarioDTO>> Listar();
    Task<UsuarioDTO?> Obtener(int id);
    Task<UsuarioDTO> Registrar(UsuarioDTO usuario);
    Task<UsuarioDTO> ActualizarPerfil(UsuarioDTO usuario);
    Task CambiarContrasena(int id, string actual, string nueva);
    Task<UsuarioDTO> Verificar(int id);
    Task<UsuarioDTO> Suspender(int id);
    Task<UsuarioDTO> Reactivar(int id);
}
