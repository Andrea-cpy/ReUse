package pe.edu.pucp.reuse.rest;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.BloqueoBL;
import pe.edu.pucp.reuse.bl.impl.BloqueoBLImpl;
import pe.edu.pucp.reuse.modelo.moderacion.Bloqueo;
import pe.edu.pucp.reuse.rest.dto.BloqueoDTO;
import pe.edu.pucp.reuse.rest.mapper.ModeracionMapper;
import pe.edu.pucp.reuse.rest.mapper.UsuarioMapper;

/**
 * RF-10: bloquear contacto. Al bloquear se cierran (BLOQUEADO) los chats abiertos entre ambos
 * (procedimiento bloquear_chats_entre, en la misma transaccion) y, mientras dure, ninguno ve los
 * anuncios del otro ni puede solicitarle chat u ofertarle.
 */
@Path("/bloqueos")
@Produces("application/json")
@Consumes("application/json")
public class BloqueosResource {
    private final BloqueoBL bloqueoBL;

    public BloqueosResource() {
        bloqueoBL = new BloqueoBLImpl();
    }

    /** Bloqueos activos que registro el usuario (?usuario=ID). */
    @GET
    public Response listar(@QueryParam("usuario") int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el usuario (?usuario=ID)");
        }
        return Response.ok(ModeracionMapper.bloqueosToDTOs(bloqueoBL.listarPorBloqueador(idUsuario))).build();
    }

    /** Estado del bloqueo entre dos usuarios: {"bloqueado": true|false, "bloqueo": {...}}. */
    @GET
    @Path("/entre")
    public Response entre(@QueryParam("usuarioA") int idUsuarioA, @QueryParam("usuarioB") int idUsuarioB)
            throws BLException {
        Bloqueo bloqueo = bloqueoBL.obtenerActivoEntre(idUsuarioA, idUsuarioB);
        if (bloqueo == null) {
            return Response.ok(Map.of("bloqueado", false)).build();
        }
        return Response.ok(Map.of("bloqueado", true, "bloqueo", ModeracionMapper.toDTO(bloqueo))).build();
    }

    @POST
    public Response bloquear(BloqueoDTO dto) throws BLException {
        if (dto == null) {
            return Respuestas.solicitudInvalida("Debe indicar quien bloquea y a quien");
        }
        Bloqueo bloqueo = new Bloqueo(UsuarioMapper.referencia(dto.getIdBloqueador()),
                UsuarioMapper.referencia(dto.getIdBloqueado()));
        int id = bloqueoBL.insert(bloqueo);
        return Response.created(URI.create("bloqueos/" + id))
                .entity(ModeracionMapper.toDTO(bloqueoBL.findById(id)))
                .build();
    }

    @PUT
    @Path("/{id}/desbloquear")
    public Response desbloquear(@PathParam("id") int id) throws BLException {
        bloqueoBL.desbloquear(id);
        return Response.ok(ModeracionMapper.toDTO(bloqueoBL.findById(id))).build();
    }
}
