using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>RF-05: puntos seguros de entrega en el campus (administrador).</summary>
public interface IPuntoEntregaService
{
    Task<List<PuntoEntregaDTO>> Listar();
    Task<PuntoEntregaDTO?> Obtener(int id);
    Task<PuntoEntregaDTO> Registrar(PuntoEntregaDTO punto);
    Task<PuntoEntregaDTO> Actualizar(PuntoEntregaDTO punto);
    Task Eliminar(int id);
}
