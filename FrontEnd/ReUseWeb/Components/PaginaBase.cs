using Microsoft.AspNetCore.Components;
using Microsoft.AspNetCore.Components.Authorization;
using ReUseWeb.Seguridad;
using ReUseWeb.Servicios;

namespace ReUseWeb.Components;

/// <summary>
/// Base de las páginas que consumen los servicios web: da el usuario de la sesión
/// y un manejo común de errores (el mensaje de la regla de negocio que devuelve Java).
/// </summary>
public abstract class PaginaBase : ComponentBase
{
    [CascadingParameter]
    protected Task<AuthenticationState>? EstadoAutenticacion { get; set; }

    /// <summary>Mensaje de error que se muestra en la página (null si no hay).</summary>
    protected string? Error { get; set; }

    /// <summary>Mensaje de éxito de la última acción.</summary>
    protected string? Exito { get; set; }

    /// <summary>True mientras se espera la respuesta de una acción (para deshabilitar botones).</summary>
    protected bool Procesando { get; set; }

    private int idUsuario;
    private string nombreUsuario = "";

    protected async Task<int> IdUsuarioAsync()
    {
        await CargarUsuario();
        return idUsuario;
    }

    protected async Task<string> NombreUsuarioAsync()
    {
        await CargarUsuario();
        return nombreUsuario;
    }

    private async Task CargarUsuario()
    {
        if (idUsuario > 0 || EstadoAutenticacion is null) return;
        AuthenticationState estado = await EstadoAutenticacion;
        idUsuario = estado.User.IdUsuario();
        nombreUsuario = estado.User.NombreCompleto();
    }

    /// <summary>Ejecuta una llamada al backend. Si falla, guarda el mensaje en Error y devuelve false.</summary>
    protected async Task<bool> Ejecutar(Func<Task> accion, string? mensajeExito = null)
    {
        Error = null;
        Exito = null;
        Procesando = true;
        try
        {
            await accion();
            Exito = mensajeExito;
            return true;
        }
        catch (ServicioException ex)
        {
            Error = ex.Message;
            return false;
        }
        finally
        {
            Procesando = false;
        }
    }
}
