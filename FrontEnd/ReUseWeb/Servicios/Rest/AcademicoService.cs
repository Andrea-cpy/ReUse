using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de / — Facultades, carreras y materiales académicos (listas para los formularios).</summary>
public class AcademicoService : IAcademicoService
{
    private string BaseUrl { get; }

    public AcademicoService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}";
    }

    public Task<List<FacultadDTO>> ListarFacultades() => HttpUtils.DoGet<List<FacultadDTO>>($"{BaseUrl}/facultades");

    public Task<List<CarreraDTO>> ListarCarreras(int idFacultad) => HttpUtils.DoGet<List<CarreraDTO>>($"{BaseUrl}/facultades/{idFacultad}/carreras");

    public Task<List<MaterialAcademicoDTO>> ListarMateriales() => HttpUtils.DoGet<List<MaterialAcademicoDTO>>($"{BaseUrl}/materiales");
}
