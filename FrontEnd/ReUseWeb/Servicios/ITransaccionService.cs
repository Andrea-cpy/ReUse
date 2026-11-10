using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-15 historial, RF-11 confirmación de entrega y RF-03 confirmación de cita.</summary>
public interface ITransaccionService
{
    Task<List<TransaccionDTO>> ListarPorUsuario(int idUsuario);
    Task<TransaccionDTO?> Obtener(int id);
    Task<CitaEntregaDTO?> ObtenerCita(int idTransaccion);
    Task<TransaccionDTO> ConfirmarCita(int id);
    Task<TransaccionDTO> ConfirmarEntrega(int id, int idUsuario);
    Task<TransaccionDTO> Cancelar(int id);
}
