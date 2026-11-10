using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /ofertas — RF-17: ofertas, contraofertas y su aceptación.</summary>
public class OfertaService : IOfertaService
{
    private string BaseUrl { get; }

    public OfertaService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/ofertas";
    }

    public Task<List<OfertaDTO>> ListarPorComprador(int idComprador) => HttpUtils.DoGet<List<OfertaDTO>>($"{BaseUrl}?comprador={idComprador}");

    public Task<OfertaDTO> Ofertar(int idComprador, int idAnuncio, decimal monto) => HttpUtils.DoPost<OfertaDTO, OfertaDTO>(BaseUrl, new OfertaDTO { IdComprador = idComprador, IdAnuncio = idAnuncio, MontoPropuesto = monto });

    public Task Retirar(int idOferta) => HttpUtils.DoDelete($"{BaseUrl}/{idOferta}");

    public Task<TransaccionCreadaDTO> Aceptar(int idOferta) => HttpUtils.DoPutAccion<TransaccionCreadaDTO>($"{BaseUrl}/{idOferta}/aceptar");

    public Task<OfertaDTO> Rechazar(int idOferta) => HttpUtils.DoPutAccion<OfertaDTO>($"{BaseUrl}/{idOferta}/rechazar");

    public Task<OfertaDTO> Contraofertar(int idOferta, decimal monto) => HttpUtils.DoPut<MontoDTO, OfertaDTO>($"{BaseUrl}/{idOferta}/contraoferta", new MontoDTO { Monto = monto });

    public Task<TransaccionCreadaDTO> AceptarContraoferta(int idOferta) => HttpUtils.DoPutAccion<TransaccionCreadaDTO>($"{BaseUrl}/{idOferta}/contraoferta/aceptar");

    public Task<OfertaDTO> RechazarContraoferta(int idOferta) => HttpUtils.DoPutAccion<OfertaDTO>($"{BaseUrl}/{idOferta}/contraoferta/rechazar");
}
