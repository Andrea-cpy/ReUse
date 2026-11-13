using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /puntos-entrega — RF-05: puntos seguros de entrega en el campus (administrador).</summary>
public class PuntoEntregaService : IPuntoEntregaService
{
    private string BaseUrl { get; }

    public PuntoEntregaService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/puntos-entrega";
    }

    public Task<List<PuntoEntregaDTO>> Listar() => HttpUtils.DoGet<List<PuntoEntregaDTO>>(BaseUrl);

    public Task<PuntoEntregaDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<PuntoEntregaDTO>($"{BaseUrl}/{id}");

    public Task<PuntoEntregaDTO> Registrar(PuntoEntregaDTO punto) => HttpUtils.DoPost<PuntoEntregaDTO, PuntoEntregaDTO>(BaseUrl, punto);

    public Task<PuntoEntregaDTO> Actualizar(PuntoEntregaDTO punto) => HttpUtils.DoPut<PuntoEntregaDTO, PuntoEntregaDTO>($"{BaseUrl}/{punto.IdPuntoEntrega}", punto);

    public Task Eliminar(int id) => HttpUtils.DoDelete($"{BaseUrl}/{id}");
}
