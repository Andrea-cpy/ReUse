using Microsoft.Extensions.Caching.Memory;
using ReUseWeb.DTOs;

namespace ReUseWeb.Seguridad;

/// <summary>Estudiante que entró con Google pero aún no tiene cuenta en ReUse.</summary>
public sealed record RegistroGooglePendiente(string IdToken, UsuarioDTO Perfil);

/// <summary>
/// Guarda en memoria del servidor, por 15 minutos, el ID token de Google de quien está
/// completando su registro. La página /registro/google solo recibe un ticket aleatorio:
/// el token y los datos personales nunca viajan en la URL.
/// </summary>
public class RegistrosGooglePendientes(IMemoryCache cache)
{
    private static readonly TimeSpan Vigencia = TimeSpan.FromMinutes(15);

    public string Crear(string idToken, UsuarioDTO perfil)
    {
        string ticket = Guid.NewGuid().ToString("N");
        cache.Set(Clave(ticket), new RegistroGooglePendiente(idToken, perfil), Vigencia);
        return ticket;
    }

    public RegistroGooglePendiente? Obtener(string? ticket) =>
        string.IsNullOrEmpty(ticket) ? null : cache.Get<RegistroGooglePendiente>(Clave(ticket));

    public void Quitar(string ticket) => cache.Remove(Clave(ticket));

    private static string Clave(string ticket) => $"registro-google:{ticket}";
}
