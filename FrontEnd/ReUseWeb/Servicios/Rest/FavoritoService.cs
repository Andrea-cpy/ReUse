using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /favoritos — RF-07: anuncios guardados como favoritos.</summary>
public class FavoritoService : IFavoritoService
{
    private string BaseUrl { get; }

    public FavoritoService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/favoritos";
    }

    public Task<List<FavoritoDTO>> ListarPorUsuario(int idUsuario) => HttpUtils.DoGet<List<FavoritoDTO>>($"{BaseUrl}?usuario={idUsuario}");

    public Task<FavoritoDTO> Agregar(int idUsuario, int idAnuncio) => HttpUtils.DoPost<FavoritoDTO, FavoritoDTO>(BaseUrl, new FavoritoDTO { IdUsuario = idUsuario, IdAnuncio = idAnuncio });

    public Task Quitar(int idFavorito) => HttpUtils.DoDelete($"{BaseUrl}/{idFavorito}");
}
