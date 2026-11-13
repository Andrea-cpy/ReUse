using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /denuncias — RF-04 (incidentes), RF-13 (suspensión de reincidentes), RF-14 (anuncios observados).</summary>
public class DenunciaService : IDenunciaService
{
    private string BaseUrl { get; }

    public DenunciaService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/denuncias";
    }

    public Task<List<DenunciaDTO>> Listar(string? tipo = null, string? estado = null, int idDenunciante = 0)
    {
        List<string> filtros = [];
        if (!string.IsNullOrEmpty(tipo)) filtros.Add($"tipo={Uri.EscapeDataString(tipo)}");
        if (!string.IsNullOrEmpty(estado)) filtros.Add($"estado={Uri.EscapeDataString(estado)}");
        if (idDenunciante > 0) filtros.Add($"denunciante={idDenunciante}");
        string url = filtros.Count == 0 ? BaseUrl : $"{BaseUrl}?{string.Join('&', filtros)}";
        return HttpUtils.DoGet<List<DenunciaDTO>>(url);
    }

    public Task<DenunciaDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<DenunciaDTO>($"{BaseUrl}/{id}");

    public Task<DenunciaDTO> DenunciarUsuario(DenunciaDTO denuncia) => HttpUtils.DoPost<DenunciaDTO, DenunciaDTO>($"{BaseUrl}/usuarios", denuncia);

    public Task<DenunciaDTO> DenunciarAnuncio(DenunciaDTO denuncia) => HttpUtils.DoPost<DenunciaDTO, DenunciaDTO>($"{BaseUrl}/anuncios", denuncia);

    public Task<DenunciaDTO> Sancionar(int id, int idAdministrador) => HttpUtils.DoPutAccion<DenunciaDTO>($"{BaseUrl}/{id}/sancionar?administrador={idAdministrador}");

    public Task<DenunciaDTO> Desestimar(int id, int idAdministrador) => HttpUtils.DoPutAccion<DenunciaDTO>($"{BaseUrl}/{id}/desestimar?administrador={idAdministrador}");
}
