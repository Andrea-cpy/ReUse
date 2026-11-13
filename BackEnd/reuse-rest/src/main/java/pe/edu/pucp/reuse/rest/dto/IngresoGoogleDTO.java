package pe.edu.pucp.reuse.rest.dto;

/**
 * Respuesta de POST /auth/google. Si registrado es true, usuario es la cuenta que inicia sesion.
 * Si es false, el correo aun no tiene cuenta y usuario trae los datos que Google conoce
 * (correo, nombres, apellidos y el codigo deducido del correo) para prellenar el registro.
 */
public class IngresoGoogleDTO {
    private boolean registrado;
    private UsuarioDTO usuario;

    public IngresoGoogleDTO() {
    }

    public IngresoGoogleDTO(boolean registrado, UsuarioDTO usuario) {
        this.registrado = registrado;
        this.usuario = usuario;
    }

    public boolean isRegistrado() {
        return registrado;
    }

    public void setRegistrado(boolean registrado) {
        this.registrado = registrado;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }
}
