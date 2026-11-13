using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>Cliente REST de /categorias — RF-06: categorías de materiales (administrador).</summary>
public class CategoriaService : ICategoriaService
{
    private string BaseUrl { get; }

    public CategoriaService(IConfiguration configuracion)
    {
        BaseUrl = $"{configuracion["RestApi:BaseUrl"]!.TrimEnd('/')}/categorias";
    }

    public Task<List<CategoriaMaterialDTO>> Listar() => HttpUtils.DoGet<List<CategoriaMaterialDTO>>(BaseUrl);

    public Task<CategoriaMaterialDTO?> Obtener(int id) => HttpUtils.DoGetOpcional<CategoriaMaterialDTO>($"{BaseUrl}/{id}");

    public Task<CategoriaMaterialDTO> Registrar(CategoriaMaterialDTO categoria) => HttpUtils.DoPost<CategoriaMaterialDTO, CategoriaMaterialDTO>(BaseUrl, categoria);

    public Task<CategoriaMaterialDTO> Actualizar(CategoriaMaterialDTO categoria) => HttpUtils.DoPut<CategoriaMaterialDTO, CategoriaMaterialDTO>($"{BaseUrl}/{categoria.IdCategoria}", categoria);

    public Task Eliminar(int id) => HttpUtils.DoDelete($"{BaseUrl}/{id}");
}
