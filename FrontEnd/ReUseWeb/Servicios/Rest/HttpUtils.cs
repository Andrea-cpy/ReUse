using System.Net;
using System.Net.Http.Json;
using ReUseWeb.DTOs;

namespace ReUseWeb.Servicios.Rest;

/// <summary>
/// Llamadas HTTP al backend Java (mismo patrón visto en clase).
/// Si el backend responde con error, lee {"error": "..."} y lanza ServicioException con ese mensaje.
/// </summary>
public static class HttpUtils
{
    private static readonly HttpClient http = new() { Timeout = TimeSpan.FromSeconds(30) };

    public static async Task<TResponse> DoGet<TResponse>(string url)
    {
        HttpResponseMessage response = await Enviar(() => http.GetAsync(url));
        await VerificarRespuesta(response);
        return (await response.Content.ReadFromJsonAsync<TResponse>())!;
    }

    /// <summary>Igual que DoGet, pero devuelve null si el recurso no existe (404).</summary>
    public static async Task<TResponse?> DoGetOpcional<TResponse>(string url) where TResponse : class
    {
        HttpResponseMessage response = await Enviar(() => http.GetAsync(url));
        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return null;
        }
        await VerificarRespuesta(response);
        return await response.Content.ReadFromJsonAsync<TResponse>();
    }

    public static async Task<TResponse> DoPost<TRequest, TResponse>(string url, TRequest payload)
    {
        HttpResponseMessage response = await Enviar(() => http.PostAsJsonAsync(url, payload));
        await VerificarRespuesta(response);
        return (await response.Content.ReadFromJsonAsync<TResponse>())!;
    }

    /// <summary>POST sin cuerpo (por ejemplo, enviar una respuesta rápida).</summary>
    public static async Task<TResponse> DoPost<TResponse>(string url)
    {
        HttpResponseMessage response = await Enviar(() => http.PostAsync(url, JsonContent.Create(new { })));
        await VerificarRespuesta(response);
        return (await response.Content.ReadFromJsonAsync<TResponse>())!;
    }

    public static async Task<TResponse> DoPut<TRequest, TResponse>(string url, TRequest payload)
    {
        HttpResponseMessage response = await Enviar(() => http.PutAsJsonAsync(url, payload));
        await VerificarRespuesta(response);
        return (await response.Content.ReadFromJsonAsync<TResponse>())!;
    }

    /// <summary>PUT con cuerpo cuya respuesta no se necesita (204 o cualquier 2xx).</summary>
    public static async Task DoPut<TRequest>(string url, TRequest payload)
    {
        HttpResponseMessage response = await Enviar(() => http.PutAsJsonAsync(url, payload));
        await VerificarRespuesta(response);
    }

    /// <summary>PUT sin cuerpo: las acciones de estado (aceptar, rechazar, archivar...).</summary>
    public static async Task<TResponse> DoPutAccion<TResponse>(string url)
    {
        HttpResponseMessage response = await Enviar(() => http.PutAsync(url, JsonContent.Create(new { })));
        await VerificarRespuesta(response);
        return (await response.Content.ReadFromJsonAsync<TResponse>())!;
    }

    public static async Task DoDelete(string url)
    {
        HttpResponseMessage response = await Enviar(() => http.DeleteAsync(url));
        await VerificarRespuesta(response);
    }

    private static async Task<HttpResponseMessage> Enviar(Func<Task<HttpResponseMessage>> llamada)
    {
        try
        {
            return await llamada();
        }
        catch (HttpRequestException ex)
        {
            throw new ServicioException("No se pudo conectar con el servidor de ReUse. Verifica que el backend Java esté desplegado.", ex);
        }
        catch (TaskCanceledException ex)
        {
            throw new ServicioException("El servidor de ReUse tardó demasiado en responder.", ex);
        }
    }

    private static async Task VerificarRespuesta(HttpResponseMessage response)
    {
        if (response.IsSuccessStatusCode)
        {
            return;
        }
        string mensaje = $"El servidor respondió con el código {(int)response.StatusCode}.";
        try
        {
            ErrorDTO? error = await response.Content.ReadFromJsonAsync<ErrorDTO>();
            if (!string.IsNullOrWhiteSpace(error?.Error))
            {
                mensaje = error.Error;
            }
        }
        catch (Exception)
        {
            // El cuerpo no era JSON (por ejemplo, una página de error del servidor): se usa el mensaje genérico.
        }
        throw new ServicioException(mensaje, (int)response.StatusCode);
    }
}
