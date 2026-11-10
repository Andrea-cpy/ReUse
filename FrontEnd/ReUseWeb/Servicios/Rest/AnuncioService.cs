using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /anuncios — RF-02: publicar, buscar, editar, archivar y eliminar anuncios.</summary>
public class AnuncioService : IAnuncioService
{
    private string BaseUrl { get; }

    public AnuncioService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/anuncios";
    }

    public Task<List<AnuncioDTO>> Buscar(FiltroAnuncios filtro) => HttpUtils.DoGet<List<AnuncioDTO>>(filtro.AUrl(BaseUrl));

    public Task<List<AnuncioDTO>> ListarPorVendedor(int idVendedor) => HttpUtils.DoGet<List<AnuncioDTO>>($"{BaseUrl}?vendedor={idVendedor}");

    public Task<AnuncioDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<AnuncioDTO>($"{BaseUrl}/{id}");

    public Task<AnuncioDTO> Publicar(AnuncioDTO anuncio) => HttpUtils.DoPost<AnuncioDTO, AnuncioDTO>(BaseUrl, anuncio);

    public Task<AnuncioDTO> Actualizar(AnuncioDTO anuncio) => HttpUtils.DoPut<AnuncioDTO, AnuncioDTO>($"{BaseUrl}/{anuncio.IdAnuncio}", anuncio);

    public Task Eliminar(int id) => HttpUtils.DoDelete($"{BaseUrl}/{id}");

    public Task<AnuncioDTO> Archivar(int id, int idVendedor) => HttpUtils.DoPutAccion<AnuncioDTO>($"{BaseUrl}/{id}/archivar?vendedor={idVendedor}");

    public Task<AnuncioDTO> Republicar(int id, int idVendedor) => HttpUtils.DoPutAccion<AnuncioDTO>($"{BaseUrl}/{id}/republicar?vendedor={idVendedor}");

    public Task<List<OfertaDTO>> ListarOfertas(int idAnuncio) => HttpUtils.DoGet<List<OfertaDTO>>($"{BaseUrl}/{idAnuncio}/ofertas");
}
