namespace ReUseWeb.DTOs;

/// <summary>Espejo de pe.edu.pucp.reuse.rest.dto.UsuarioDTO (JSON en camelCase).</summary>
public class UsuarioDTO
{
    public int IdUsuario { get; set; }
    public string? CodigoPUCP { get; set; }
    public string? Nombres { get; set; }
    public string? ApellidoPaterno { get; set; }
    public string? ApellidoMaterno { get; set; }
    public string? CorreoInstitucional { get; set; }
    public string? Contrasena { get; set; }
    public int IdCarrera { get; set; }
    public string? NombreCarrera { get; set; }
    public int IdFacultad { get; set; }
    public string? NombreFacultad { get; set; }
    public bool Verificado { get; set; }
    public string? EstadoCuenta { get; set; }
    public decimal ReputacionVendedor { get; set; }
    public decimal ReputacionComprador { get; set; }
    public int ContadorReportes { get; set; }
    public string? FotoPerfil { get; set; }
    public string? FechaRegistro { get; set; }
    public bool Administrador { get; set; }
}
