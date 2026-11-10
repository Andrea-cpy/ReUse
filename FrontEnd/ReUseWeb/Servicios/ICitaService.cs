using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-03: proponer y renegociar la cita de entrega.</summary>
public interface ICitaService
{
    Task<CitaEntregaDTO> Proponer(CitaEntregaDTO cita);
    Task<CitaEntregaDTO> Renegociar(CitaEntregaDTO cita);
    Task Retirar(int idCita);
}
