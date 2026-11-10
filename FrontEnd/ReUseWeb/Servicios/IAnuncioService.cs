using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-02: publicar, buscar, editar, archivar y eliminar anuncios.</summary>
public interface IAnuncioService
{
    Task<List<AnuncioDTO>> Buscar(FiltroAnuncios filtro);
    Task<List<AnuncioDTO>> ListarPorVendedor(int idVendedor);
    Task<AnuncioDTO?> Obtener(int id);
    Task<AnuncioDTO> Publicar(AnuncioDTO anuncio);
    Task<AnuncioDTO> Actualizar(AnuncioDTO anuncio);
    Task Eliminar(int id);
    Task<AnuncioDTO> Archivar(int id, int idVendedor);
    Task<AnuncioDTO> Republicar(int id, int idVendedor);
    Task<List<OfertaDTO>> ListarOfertas(int idAnuncio);
}
