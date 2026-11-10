using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-06: categorías de materiales (administrador).</summary>
public interface ICategoriaService
{
    Task<List<CategoriaMaterialDTO>> Listar();
    Task<CategoriaMaterialDTO?> Obtener(int id);
    Task<CategoriaMaterialDTO> Registrar(CategoriaMaterialDTO categoria);
    Task<CategoriaMaterialDTO> Actualizar(CategoriaMaterialDTO categoria);
    Task Eliminar(int id);
}
