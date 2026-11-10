package pe.edu.pucp.reuse.rest;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.CanalChatBL;
import pe.edu.pucp.reuse.bl.MensajeBL;
import pe.edu.pucp.reuse.bl.impl.CanalChatBLImpl;
import pe.edu.pucp.reuse.bl.impl.MensajeBLImpl;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.rest.dto.CanalChatDTO;
import pe.edu.pucp.reuse.rest.dto.MensajeDTO;
import pe.edu.pucp.reuse.rest.mapper.ChatMapper;

/**
 * RF-09: el comprador solicita un chat por un anuncio; el vendedor lo acepta o rechaza.
 * RF-08: una vez aceptado, ambos intercambian mensajes. RF-16: respuestas rapidas.
 */
@Path("/chats")
@Produces("application/json")
@Consumes("application/json")
public class ChatsResource {
    private final CanalChatBL chatBL;
    private final MensajeBL mensajeBL;

    @Context
    private UriInfo uriInfo;

    public ChatsResource() {
        chatBL = new CanalChatBLImpl();
        mensajeBL = new MensajeBLImpl();
    }

    @GET
    public Response listarPorUsuario(@QueryParam("usuario") int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return Respuestas.solicitudInvalida("Debe indicar el usuario (?usuario=ID)");
        }
        List<CanalChatDTO> dtos = ChatMapper.chatsToDTOs(chatBL.listarPorUsuario(idUsuario));
        NombresUsuario nombres = new NombresUsuario();
        for (CanalChatDTO dto : dtos) {
            completarNombres(dto, nombres);
        }
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        CanalChat chat = chatBL.findById(id);
        if (chat == null) {
            return Respuestas.noEncontrado("No existe el chat con id " + id);
        }
        CanalChatDTO dto = ChatMapper.toDTO(chat);
        completarNombres(dto, new NombresUsuario());
        return Response.ok(dto).build();
    }

    /** Solicitud de chat: {idAnuncio, idComprador}. Queda PENDIENTE hasta que el vendedor responda. */
    @POST
    public Response solicitar(CanalChatDTO chatDTO) throws BLException {
        if (chatDTO == null) {
            return Respuestas.solicitudInvalida("Debe enviar el anuncio y el comprador");
        }
        int id = chatBL.insert(ChatMapper.toModelo(chatDTO));
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(uri).entity(ChatMapper.toDTO(chatBL.findById(id))).build();
    }

    @PUT
    @Path("/{id}/aceptar")
    public Response aceptar(@PathParam("id") int id) throws BLException {
        chatBL.aceptarSolicitud(id);
        return findById(id);
    }

    @PUT
    @Path("/{id}/rechazar")
    public Response rechazar(@PathParam("id") int id) throws BLException {
        chatBL.rechazarSolicitud(id);
        return findById(id);
    }

    @PUT
    @Path("/{id}/cerrar")
    public Response cerrar(@PathParam("id") int id) throws BLException {
        chatBL.cerrar(id);
        return findById(id);
    }

    @GET
    @Path("/{id}/mensajes")
    public Response mensajes(@PathParam("id") int id) throws BLException {
        List<MensajeDTO> dtos = ChatMapper.mensajesToDTOs(mensajeBL.listarPorChat(id));
        NombresUsuario nombres = new NombresUsuario();
        for (MensajeDTO dto : dtos) {
            if (dto.getNombreEmisor() == null) {
                dto.setNombreEmisor(nombres.de(dto.getIdEmisor()));
            }
        }
        return Response.ok(dtos).build();
    }

    /** Mensaje libre: {contenido, idEmisor}. La BL valida que el chat este ACTIVO y que el emisor participe. */
    @POST
    @Path("/{id}/mensajes")
    public Response enviar(@PathParam("id") int id, MensajeDTO mensajeDTO) throws BLException {
        if (mensajeDTO == null) {
            return Respuestas.solicitudInvalida("Debe enviar el contenido y el emisor");
        }
        int idMensaje = mensajeBL.insert(ChatMapper.toModelo(mensajeDTO, id));
        return Response.status(Response.Status.CREATED).entity(ChatMapper.toDTO(mensajeBL.findById(idMensaje))).build();
    }

    /** Envia una respuesta rapida guardada del emisor (?emisor=ID). */
    @POST
    @Path("/{id}/respuestas-rapidas/{idRespuesta}")
    public Response enviarRespuestaRapida(@PathParam("id") int id, @PathParam("idRespuesta") int idRespuesta,
            @QueryParam("emisor") int idEmisor) throws BLException {
        int idMensaje = mensajeBL.enviarRespuestaRapida(id, idEmisor, idRespuesta);
        return Response.status(Response.Status.CREATED).entity(ChatMapper.toDTO(mensajeBL.findById(idMensaje))).build();
    }

    private static void completarNombres(CanalChatDTO dto, NombresUsuario nombres) throws BLException {
        if (dto.getNombreComprador() == null) {
            dto.setNombreComprador(nombres.de(dto.getIdComprador()));
        }
        dto.setNombreVendedor(nombres.de(dto.getIdVendedor()));
    }
}
