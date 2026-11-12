package pe.edu.pucp.reuse.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
import pe.edu.pucp.reuse.rest.dto.RecuperacionDTO;
import pe.edu.pucp.reuse.rest.dto.UsuarioDTO;
import pe.edu.pucp.reuse.rest.mapper.UsuarioMapper;

/** RF-01: inicio de sesion con correo institucional (BCrypt en la capa de negocio). */
@Path("/auth")
@Produces("application/json")
@Consumes("application/json")
public class AuthResource {
    private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());
    // Modo demostracion (sin servidor de correo): el codigo de recuperacion viaja en la respuesta.
    private static final boolean ENTREGAR_CODIGO_EN_RESPUESTA = true;

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

    /**
     * RF-01 (recuperacion, paso 1): genera un codigo de 6 digitos valido por 15 minutos.
     * El proyecto no tiene servidor de correo: el codigo se registra en el log del servidor y,
     * mientras ENTREGAR_CODIGO_EN_RESPUESTA sea true (modo demostracion), tambien se devuelve
     * en la respuesta para que el frontend lo muestre. En produccion se enviaria al correo PUCP.
     */
    @POST
    @Path("/recuperar")
    public Response recuperar(RecuperacionDTO solicitud) throws BLException {
        if (solicitud == null || solicitud.getCorreo() == null) {
            return Respuestas.solicitudInvalida("Debe indicar el correo institucional");
        }
        String codigo = usuarioBL.solicitarRecuperacion(solicitud.getCorreo());
        LOG.info("Codigo de recuperacion para " + solicitud.getCorreo().trim().toLowerCase() + ": " + codigo);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Se genero un codigo de recuperacion valido por 15 minutos.");
        if (ENTREGAR_CODIGO_EN_RESPUESTA) {
            respuesta.put("codigoDemo", codigo);
        }
        return Response.ok(respuesta).build();
    }

    /** RF-01 (recuperacion, paso 2): valida el codigo y guarda la nueva contrasena. */
    @POST
    @Path("/restablecer")
    public Response restablecer(RecuperacionDTO solicitud) throws BLException {
        if (solicitud == null) {
            return Respuestas.solicitudInvalida("Debe enviar el correo, el codigo y la nueva contrasena");
        }
        usuarioBL.restablecerContrasena(solicitud.getCorreo(), solicitud.getCodigo(), solicitud.getContrasenaNueva());
        return Response.ok(Map.of("mensaje", "Tu contrasena fue actualizada. Ya puedes iniciar sesion.")).build();
    }
}
