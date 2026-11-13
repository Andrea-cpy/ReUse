package pe.edu.pucp.reuse.rest.dto;

/** ID token (JWT) que Google entrego al frontend al iniciar sesion. */
public class GoogleTokenDTO {
    private String idToken;

    public GoogleTokenDTO() {
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
