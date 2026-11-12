package pe.edu.pucp.reuse.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
import pe.edu.pucp.reuse.bl.impl.InsigniaBLImpl;
import pe.edu.pucp.reuse.bl.impl.InsigniaUsuarioBLImpl;
import pe.edu.pucp.reuse.bl.impl.ReporteUsuarioBLImpl;
import pe.edu.pucp.reuse.bl.impl.UsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.enums.TipoMetricaInsignia;
import pe.edu.pucp.reuse.modelo.gamificacion.Insignia;
import pe.edu.pucp.reuse.modelo.gamificacion.InsigniaUsuario;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.dto.CambioContrasenaDTO;
import pe.edu.pucp.reuse.rest.dto.InsigniaUsuarioDTO;
import pe.edu.pucp.reuse.rest.dto.ReputacionDTO;
import pe.edu.pucp.reuse.rest.dto.UsuarioDTO;
import pe.edu.pucp.reuse.rest.mapper.GamificacionMapper;
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

    /** RF-18: nivel de reputacion, reputacion separada y metricas (ventas, compras, promedio). */
    @GET
    @Path("/{id}/reputacion")
    public Response reputacion(@PathParam("id") int id) throws BLException {
        UsuarioPUCP usuario = usuarioBL.findById(id);
        if (usuario == null) {
            return Respuestas.noEncontrado("No existe el usuario con id " + id);
        }
        // Las sanciones se cuentan en paralelo con las metricas (cada consulta abre su conexion a la BD).
        CompletableFuture<Integer> sanciones = CompletableFuture.supplyAsync(() -> {
            try {
                return new ReporteUsuarioBLImpl().contarSancionesRecientes(id);
            } catch (BLException e) {
                throw new CompletionException(e);
            }
        });
        Map<TipoMetricaInsignia, Double> metricas = usuarioBL.obtenerMetricas(id, List.of(
                TipoMetricaInsignia.VENTAS_COMPLETADAS, TipoMetricaInsignia.COMPRAS_COMPLETADAS,
                TipoMetricaInsignia.TRANSACCIONES_COMPLETADAS, TipoMetricaInsignia.CALIFICACION_PROMEDIO));
        double completadas = metricas.get(TipoMetricaInsignia.TRANSACCIONES_COMPLETADAS);
        double promedio = metricas.get(TipoMetricaInsignia.CALIFICACION_PROMEDIO);

        ReputacionDTO dto = new ReputacionDTO();
        dto.setIdUsuario(id);
        dto.setNivel(UsuarioBLImpl.nivelSegun(completadas, promedio).name());
        dto.setReputacionVendedor(usuario.getReputacionVendedor());
        dto.setReputacionComprador(usuario.getReputacionComprador());
        dto.setVentasCompletadas(metricas.get(TipoMetricaInsignia.VENTAS_COMPLETADAS).intValue());
        dto.setComprasCompletadas(metricas.get(TipoMetricaInsignia.COMPRAS_COMPLETADAS).intValue());
        dto.setTransaccionesCompletadas((int) completadas);
        dto.setCalificacionPromedio(promedio);
        try {
            dto.setSancionesRecientes(sanciones.join());
        } catch (CompletionException e) {
            if (e.getCause() instanceof BLException causa) {
                throw causa;
            }
            throw e;
        }
        return Response.ok(dto).build();
    }

    /** RF-13: sanciones en la ventana de 90 dias (con 4 o mas la cuenta se suspende). */
    @GET
    @Path("/{id}/sanciones")
    public Response sanciones(@PathParam("id") int id) throws BLException {
        return Response.ok(Map.of("idUsuario", id,
                "sancionesRecientes", new ReporteUsuarioBLImpl().contarSancionesRecientes(id),
                "sancionesParaSuspender", 4, "diasVentana", 90)).build();
    }

    /** RF-18: insignias obtenidas por el usuario (con descripcion e icono del catalogo). */
    @GET
    @Path("/{id}/insignias")
    public Response insignias(@PathParam("id") int id) throws BLException {
        return Response.ok(insigniasDe(id)).build();
    }

    /** RF-18: evalua las reglas de todas las insignias activas y otorga las que ya cumple. */
    @POST
    @Path("/{id}/insignias/evaluar")
    public Response evaluarInsignias(@PathParam("id") int id) throws BLException {
        List<Insignia> nuevas = new InsigniaUsuarioBLImpl().evaluarInsignias(id);
        List<String> nombres = new ArrayList<>();
        for (Insignia insignia : nuevas) {
            nombres.add(insignia.getNombre());
        }
        return Response.ok(Map.of("nuevas", nombres, "insignias", insigniasDe(id))).build();
    }

    private List<InsigniaUsuarioDTO> insigniasDe(int idUsuario) throws BLException {
        Map<Integer, Insignia> catalogo = new HashMap<>();
        for (Insignia insignia : new InsigniaBLImpl().findAll()) {
            catalogo.put(insignia.getIdInsignia(), insignia);
        }
        List<InsigniaUsuarioDTO> dtos = new ArrayList<>();
        for (InsigniaUsuario otorgamiento : new InsigniaUsuarioBLImpl().listarPorUsuario(idUsuario)) {
            if (otorgamiento.isActivo() && otorgamiento.getInsignia() != null) {
                dtos.add(GamificacionMapper.toDTO(otorgamiento, catalogo.get(otorgamiento.getInsignia().getIdInsignia())));
            }
        }
        return dtos;
    }
}
