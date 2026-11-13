using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /citas — RF-03: proponer y renegociar la cita de entrega.</summary>
public class CitaService : ICitaService
{
    private string BaseUrl { get; }

    public CitaService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/citas";
    }

    public Task<CitaEntregaDTO> Proponer(CitaEntregaDTO cita) => HttpUtils.DoPost<CitaEntregaDTO, CitaEntregaDTO>(BaseUrl, cita);

    public Task<CitaEntregaDTO> Renegociar(CitaEntregaDTO cita) => HttpUtils.DoPut<CitaEntregaDTO, CitaEntregaDTO>($"{BaseUrl}/{cita.IdCita}", cita);

    public Task Retirar(int idCita) => HttpUtils.DoDelete($"{BaseUrl}/{idCita}");
}
