package pe.edu.pucp.reuse.rest;

import java.util.Map;

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
import pe.edu.pucp.reuse.modelo.usuarios.CuentaGoogle;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.dto.CredencialesDTO;
import pe.edu.pucp.reuse.rest.dto.GoogleTokenDTO;
import pe.edu.pucp.reuse.rest.dto.IngresoGoogleDTO;
import pe.edu.pucp.reuse.rest.dto.RegistroGoogleDTO;
import pe.edu.pucp.reuse.rest.dto.RecuperacionDTO;
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
        return Response.ok(conRol(usuario)).build();
    }

    /**
     * RF-01 con Google: recibe el ID token que Google entrego al frontend. Si el correo ya tiene
     * cuenta, la devuelve (registrado = true) y el frontend inicia sesion; si no, devuelve los
     * datos de Google para que el estudiante complete su registro. Token invalido o de otro
     * dominio: 400 con el motivo.
     */
    @POST
    @Path("/google")
    public Response ingresarConGoogle(GoogleTokenDTO token) throws BLException {
        if (token == null) {
            return Respuestas.solicitudInvalida("Falta el token de Google");
        }
        CuentaGoogle cuenta = usuarioBL.validarCuentaGoogle(token.getIdToken());
        UsuarioPUCP usuario = usuarioBL.iniciarSesionConGoogle(cuenta);
        if (usuario == null) {
            return Response.ok(new IngresoGoogleDTO(false, perfilDeGoogle(cuenta))).build();
        }
        return Response.ok(new IngresoGoogleDTO(true, conRol(usuario))).build();
    }

    /** Registro con Google: la cuenta queda ACTIVA y verificada (201 con el usuario creado). */
    @POST
    @Path("/google/registro")
    public Response registrarConGoogle(RegistroGoogleDTO registro) throws BLException {
        if (registro == null || registro.getUsuario() == null) {
            return Respuestas.solicitudInvalida("Debe enviar el token de Google y los datos del estudiante");
        }
        CuentaGoogle cuenta = usuarioBL.validarCuentaGoogle(registro.getIdToken());
        int id = usuarioBL.registrarConGoogle(UsuarioMapper.toModelo(registro.getUsuario()), cuenta);
        return Response.status(Response.Status.CREATED).entity(conRol(usuarioBL.findById(id))).build();
    }

    private UsuarioDTO conRol(UsuarioPUCP usuario) throws BLException {
        UsuarioDTO dto = UsuarioMapper.toDTO(usuario);
        AdministradorPUCP administrador = administradorBL.findById(usuario.getIdUsuario());
        dto.setAdministrador(administrador != null && administrador.isActivo());
        return dto;
    }

    // Google entrega los apellidos juntos ("Quispe Mamani") y asi se guardan.
    private static UsuarioDTO perfilDeGoogle(CuentaGoogle cuenta) {
        UsuarioDTO perfil = new UsuarioDTO();
        perfil.setCorreoInstitucional(cuenta.getCorreo());
        perfil.setCodigoPUCP(cuenta.getCodigoPUCP());
        perfil.setNombres(cuenta.getNombres());
        perfil.setApellidos(cuenta.getApellidos() == null ? null : cuenta.getApellidos().trim());
        return perfil;
    }

    /**
     * RF-01 (recuperacion, paso 1): la capa de negocio genera un codigo de 6 digitos valido por
     * 15 minutos y lo envia al correo PUCP (si el correo no esta configurado, lo deja en el log
     * del servidor como modo desarrollo). El codigo nunca viaja en la respuesta, porque cualquiera
     * podria cambiar la contrasena de otra cuenta sabiendo solo su correo. La respuesta es la misma
     * exista o no la cuenta.
     */
    @POST
    @Path("/recuperar")
    public Response recuperar(RecuperacionDTO solicitud) throws BLException {
        if (solicitud == null || solicitud.getCorreo() == null) {
            return Respuestas.solicitudInvalida("Debe indicar el correo institucional");
        }
        usuarioBL.solicitarRecuperacion(solicitud.getCorreo());
        return Response.ok(Map.of("mensaje",
                "Si el correo esta registrado, te enviaremos un codigo valido por 15 minutos.")).build();
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
