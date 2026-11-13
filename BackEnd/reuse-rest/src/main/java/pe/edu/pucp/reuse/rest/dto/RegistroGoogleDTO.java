package pe.edu.pucp.reuse.rest.dto;

/**
 * Registro con Google: el ID token y los datos que completa el estudiante (nombres, apellidos,
 * codigo PUCP, carrera y contrasena). El correo se toma del token, no del payload.
 */
public class RegistroGoogleDTO {
    private String idToken;
    private UsuarioDTO usuario;

    public RegistroGoogleDTO() {
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }
}
