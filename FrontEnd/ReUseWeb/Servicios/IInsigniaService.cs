using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-18: catálogo de insignias (administrador), insignias del usuario y su reputación.</summary>
public interface IInsigniaService
{
    Task<List<InsigniaDTO>> Listar();
    Task<InsigniaDTO?> Obtener(int id);
    Task<InsigniaDTO> Crear(InsigniaDTO insignia);
    Task<InsigniaDTO> Actualizar(InsigniaDTO insignia);
    Task Eliminar(int id);
    Task<List<InsigniaUsuarioDTO>> DeUsuario(int idUsuario);
    Task<EvaluacionInsigniasDTO> Evaluar(int idUsuario);
    Task<ReputacionDTO?> Reputacion(int idUsuario);
}
