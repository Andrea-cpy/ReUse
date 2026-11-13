namespace ReUseWeb.DTOs;

/// <summary>Espejo de GoogleTokenDTO (Java): ID token que entregó Google.</summary>
public class GoogleTokenDTO
{
    public string? IdToken { get; set; }
}

/// <summary>Espejo de RegistroGoogleDTO (Java): token + datos que completa el estudiante.</summary>
public class RegistroGoogleDTO
{
    public string? IdToken { get; set; }
    public UsuarioDTO? Usuario { get; set; }
}

/// <summary>
/// Espejo de IngresoGoogleDTO (Java). Registrado = true: Usuario es la cuenta que inicia sesión.
/// Registrado = false: Usuario trae los datos de Google para prellenar el registro.
/// </summary>
public class IngresoGoogleDTO
{
    public bool Registrado { get; set; }
    public UsuarioDTO? Usuario { get; set; }
}
