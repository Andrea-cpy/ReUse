namespace ReUseWeb.DTOs;

/// <summary>Respuesta de GET /bloqueos/entre: si existe un bloqueo activo entre dos usuarios.</summary>
public class EstadoBloqueoDTO
{
    public bool Bloqueado { get; set; }
    public BloqueoDTO? Bloqueo { get; set; }
}

/// <summary>Respuesta de POST /auth/recuperar: el mismo mensaje exista o no la cuenta.</summary>
public class RecuperacionRespuestaDTO
{
    public string? Mensaje { get; set; }
}

/// <summary>Respuesta de POST /usuarios/{id}/insignias/evaluar.</summary>
public class EvaluacionInsigniasDTO
{
    public List<string> Nuevas { get; set; } = [];
    public List<InsigniaUsuarioDTO> Insignias { get; set; } = [];
}
