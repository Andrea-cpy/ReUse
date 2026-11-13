using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /transacciones — RF-15 historial, RF-11 confirmación de entrega y RF-03 confirmación de cita.</summary>
public class TransaccionService : ITransaccionService
{
    private string BaseUrl { get; }

    public TransaccionService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/transacciones";
    }

    public Task<List<TransaccionDTO>> ListarPorUsuario(int idUsuario) => HttpUtils.DoGet<List<TransaccionDTO>>($"{BaseUrl}?usuario={idUsuario}");

    public Task<TransaccionDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<TransaccionDTO>($"{BaseUrl}/{id}");

    public Task<CitaEntregaDTO?> ObtenerCita(int idTransaccion) => HttpUtils.DoGetOpcional<CitaEntregaDTO>($"{BaseUrl}/{idTransaccion}/cita");

    public Task<TransaccionDTO> ConfirmarCita(int id) => HttpUtils.DoPutAccion<TransaccionDTO>($"{BaseUrl}/{id}/confirmar-cita");

    public Task<TransaccionDTO> ConfirmarEntrega(int id, int idUsuario) => HttpUtils.DoPutAccion<TransaccionDTO>($"{BaseUrl}/{id}/confirmar-entrega?usuario={idUsuario}");

    public Task<TransaccionDTO> Cancelar(int id) => HttpUtils.DoPutAccion<TransaccionDTO>($"{BaseUrl}/{id}/cancelar");
}
