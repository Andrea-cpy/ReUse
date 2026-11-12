using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /insignias y de /usuarios/{id}/insignias | reputacion — RF-18.</summary>
public class InsigniaService : IInsigniaService
{
    private string BaseUrl { get; }
    private string UsuariosUrl { get; }

    public InsigniaService(IConfiguration configuracion)
    {
        string api = configuracion["RestApi:BaseUrl"]!.TrimEnd('/');
        BaseUrl = $"{api}/insignias";
        UsuariosUrl = $"{api}/usuarios";
    }

    public Task<List<InsigniaDTO>> Listar() => HttpUtils.DoGet<List<InsigniaDTO>>(BaseUrl);

    public Task<InsigniaDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<InsigniaDTO>($"{BaseUrl}/{id}");

    public Task<InsigniaDTO> Crear(InsigniaDTO insignia) => HttpUtils.DoPost<InsigniaDTO, InsigniaDTO>(BaseUrl, insignia);

    public Task<InsigniaDTO> Actualizar(InsigniaDTO insignia) => HttpUtils.DoPut<InsigniaDTO, InsigniaDTO>($"{BaseUrl}/{insignia.IdInsignia}", insignia);

    public Task Eliminar(int id) => HttpUtils.DoDelete($"{BaseUrl}/{id}");

    public Task<List<InsigniaUsuarioDTO>> DeUsuario(int idUsuario) => HttpUtils.DoGet<List<InsigniaUsuarioDTO>>($"{UsuariosUrl}/{idUsuario}/insignias");

    public Task<EvaluacionInsigniasDTO> Evaluar(int idUsuario) => HttpUtils.DoPost<EvaluacionInsigniasDTO>($"{UsuariosUrl}/{idUsuario}/insignias/evaluar");

    public Task<ReputacionDTO?> Reputacion(int idUsuario) => HttpUtils.DoGetOpcional<ReputacionDTO>($"{UsuariosUrl}/{idUsuario}/reputacion");
}
