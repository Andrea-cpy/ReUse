using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-10: bloquear y desbloquear contacto con otro usuario.</summary>
public interface IBloqueoService
{
    Task<List<BloqueoDTO>> Listar(int idUsuario);
    Task<EstadoBloqueoDTO> Entre(int idUsuarioA, int idUsuarioB);
    Task<BloqueoDTO> Bloquear(int idBloqueador, int idBloqueado);
    Task<BloqueoDTO> Desbloquear(int idBloqueo);
}
