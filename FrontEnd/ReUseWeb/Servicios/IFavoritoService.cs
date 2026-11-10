using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-07: anuncios guardados como favoritos.</summary>
public interface IFavoritoService
{
    Task<List<FavoritoDTO>> ListarPorUsuario(int idUsuario);
    Task<FavoritoDTO> Agregar(int idUsuario, int idAnuncio);
    Task Quitar(int idFavorito);
}
