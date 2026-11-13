using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios;

/// <summary>Facultades, carreras y materiales académicos (listas para los formularios).</summary>
public interface IAcademicoService
{
    Task<List<FacultadDTO>> ListarFacultades();
    Task<List<CarreraDTO>> ListarCarreras(int idFacultad);
    Task<List<MaterialAcademicoDTO>> ListarMateriales();
}
