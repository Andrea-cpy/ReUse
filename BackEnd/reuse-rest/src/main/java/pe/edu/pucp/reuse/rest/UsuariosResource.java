package pe.edu.pucp.reuse.rest;

import java.net.URI;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.UsuarioBL;
import pe.edu.pucp.reuse.bl.impl.UsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.dto.CambioContrasenaDTO;
import pe.edu.pucp.reuse.rest.dto.UsuarioDTO;
import pe.edu.pucp.reuse.rest.mapper.UsuarioMapper;

/** RF-01: registro, perfil, contrasena y estado de la cuenta (verificar, suspender, reactivar). */
@Path("/usuarios")
@Produces("application/json")
@Consumes("application/json")
public class UsuariosResource {
    private final UsuarioBL usuarioBL;

    @Context
    private UriInfo uriInfo;

    public UsuariosResource() {
        usuarioBL = new UsuarioBLImpl();
    }

    @GET
    public Response findAll() throws BLException {
        return Response.ok(UsuarioMapper.toDTOs(usuarioBL.findAll())).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        UsuarioPUCP usuario = usuarioBL.findById(id);
        if (usuario == null) {
            return Respuestas.noEncontrado("No existe el usuario con id " + id);
        }
        return Response.ok(UsuarioMapper.toDTO(usuario)).build();
    }

    /** Registro de un estudiante. La cuenta queda PENDIENTE_VERIFICACION hasta que la verifique un administrador. */
    @POST
    public Response insert(UsuarioDTO usuarioDTO) throws BLException {
        UsuarioPUCP usuario = UsuarioMapper.toModelo(usuarioDTO);
        int id = usuarioBL.insert(usuario);
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(UsuarioMapper.toDTO(usuarioBL.findById(id))).build();
    }

    /** Solo el perfil: nombres, apellidos, carrera y foto. */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, UsuarioDTO usuarioDTO) throws BLException {
        if (usuarioDTO == null || id != usuarioDTO.getIdUsuario()) {
            return Respuestas.solicitudInvalida("El id y el payload no coinciden");
        }
        usuarioBL.update(UsuarioMapper.toModelo(usuarioDTO));
        return Response.ok(UsuarioMapper.toDTO(usuarioBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}/contrasena")
    public Response cambiarContrasena(@PathParam("id") int id, CambioContrasenaDTO cambio) throws BLException {
        if (cambio == null) {
            return Respuestas.solicitudInvalida("Debe enviar la contrasena actual y la nueva");
        }
        usuarioBL.cambiarContrasena(id, cambio.getContrasenaActual(), cambio.getContrasenaNueva());
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/verificar")
    public Response verificar(@PathParam("id") int id) throws BLException {
        usuarioBL.verificarCuenta(id);
        return Response.ok(UsuarioMapper.toDTO(usuarioBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}/suspender")
    public Response suspender(@PathParam("id") int id) throws BLException {
        usuarioBL.suspenderCuenta(id);
        return Response.ok(UsuarioMapper.toDTO(usuarioBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}/reactivar")
    public Response reactivar(@PathParam("id") int id) throws BLException {
        usuarioBL.reactivarCuenta(id);
        return Response.ok(UsuarioMapper.toDTO(usuarioBL.findById(id))).build();
    }
}
