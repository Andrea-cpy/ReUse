namespace ReUseWeb.Servicios;

/// <summary>
/// Error al consumir un servicio web. Lleva el mensaje que devolvió el backend Java
/// (la regla de negocio incumplida) para mostrarlo tal cual en la pantalla.
/// </summary>
public class ServicioException : Exception
{
    public int CodigoHttp { get; }

    public ServicioException(string mensaje, int codigoHttp = 0) : base(mensaje)
    {
        CodigoHttp = codigoHttp;
    }

    public ServicioException(string mensaje, Exception causa) : base(mensaje, causa) { }
}
