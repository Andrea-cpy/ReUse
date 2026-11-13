using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-04 / RF-13 / RF-14: denuncias de usuarios y anuncios y su resolución por el administrador.</summary>
public interface IDenunciaService
{
    Task<List<DenunciaDTO>> Listar(string? tipo = null, string? estado = null, int idDenunciante = 0);
    Task<DenunciaDTO?> Obtener(int id);
    Task<DenunciaDTO> DenunciarUsuario(DenunciaDTO denuncia);
    Task<DenunciaDTO> DenunciarAnuncio(DenunciaDTO denuncia);
    Task<DenunciaDTO> Sancionar(int id, int idAdministrador);
    Task<DenunciaDTO> Desestimar(int id, int idAdministrador);
}
