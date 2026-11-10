using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-17: ofertas, contraofertas y su aceptación.</summary>
public interface IOfertaService
{
    Task<List<OfertaDTO>> ListarPorComprador(int idComprador);
    Task<OfertaDTO> Ofertar(int idComprador, int idAnuncio, decimal monto);
    Task Retirar(int idOferta);
    Task<TransaccionCreadaDTO> Aceptar(int idOferta);
    Task<OfertaDTO> Rechazar(int idOferta);
    Task<OfertaDTO> Contraofertar(int idOferta, decimal monto);
    Task<TransaccionCreadaDTO> AceptarContraoferta(int idOferta);
    Task<OfertaDTO> RechazarContraoferta(int idOferta);
}
