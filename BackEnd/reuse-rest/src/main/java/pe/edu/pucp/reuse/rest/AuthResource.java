package pe.edu.pucp.reuse.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import pe.edu.pucp.reuse.bl.AdministradorBL;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.UsuarioBL;
import pe.edu.pucp.reuse.bl.impl.AdministradorBLImpl;
import pe.edu.pucp.reuse.bl.impl.UsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.dto.CredencialesDTO;
import pe.edu.pucp.reuse.rest.dto.UsuarioDTO;
import pe.edu.pucp.reuse.rest.mapper.UsuarioMapper;

/** RF-01: inicio de sesion con correo institucional (BCrypt en la capa de negocio). */
@Path("/auth")
@Produces("application/json")
@Consumes("application/json")
public class AuthResource {
    private final UsuarioBL usuarioBL;
    private final AdministradorBL administradorBL;

    public AuthResource() {
        usuarioBL = new UsuarioBLImpl();
        administradorBL = new AdministradorBLImpl();
    }

    /** Devuelve el usuario autenticado; 'administrador' indica el rol. Credenciales invalidas: 400. */
    @POST
    @Path("/login")
    public Response login(CredencialesDTO credenciales) throws BLException {
        if (credenciales == null) {
            return Respuestas.solicitudInvalida("Debe enviar el correo y la contrasena");
        }
        UsuarioPUCP usuario = usuarioBL.iniciarSesion(credenciales.getCorreo(), credenciales.getContrasena());
        UsuarioDTO dto = UsuarioMapper.toDTO(usuario);
        AdministradorPUCP administrador = administradorBL.findById(usuario.getIdUsuario());
        dto.setAdministrador(administrador != null && administrador.isActivo());
        return Response.ok(dto).build();
    }
}
