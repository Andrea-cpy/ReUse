package pe.edu.pucp.reuse.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import pe.edu.pucp.reuse.bl.ReporteAnuncioBL;
import pe.edu.pucp.reuse.bl.ReporteUsuarioBL;
import pe.edu.pucp.reuse.bl.impl.ReporteAnuncioBLImpl;
import pe.edu.pucp.reuse.bl.impl.ReporteUsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteUsuario;
import pe.edu.pucp.reuse.rest.dto.DenunciaDTO;
import pe.edu.pucp.reuse.rest.mapper.ModeracionMapper;

/**
 * RF-04: denuncias de usuarios (fraude, inasistencia...) y de anuncios, y su resolucion por un administrador.
 * RF-13: al sancionar a un usuario con 4 o mas sanciones en 90 dias su cuenta queda SUSPENDIDA.
 * RF-14: denunciar un anuncio lo deja OBSERVADO; sancionarlo lo ARCHIVA y desestimarlo lo vuelve DISPONIBLE.
 * (Los reportes PDF estan en /reportes.)
 */
@Path("/denuncias")
@Produces("application/json")
@Consumes("application/json")
public class DenunciasResource {
    private final ReporteUsuarioBL reporteUsuarioBL;
    private final ReporteAnuncioBL reporteAnuncioBL;

    public DenunciasResource() {
        reporteUsuarioBL = new ReporteUsuarioBLImpl();
        reporteAnuncioBL = new ReporteAnuncioBLImpl();
    }

    /** Bandeja del administrador (filtros opcionales) o "Mis reportes" con ?denunciante=ID. */
    @GET
    public Response listar(@QueryParam("tipo") String tipo,
                           @QueryParam("estado") String estado,
                           @QueryParam("denunciante") int idDenunciante) throws BLException {
        return Response.ok(listarDenuncias(tipo, estado, idDenunciante)).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") int id) throws BLException {
        DenunciaDTO dto = buscar(id);
        if (dto == null) {
            return Respuestas.noEncontrado("No existe la denuncia con id " + id);
        }
        return Response.ok(dto).build();
    }

    @POST
    @Path("/usuarios")
    public Response denunciarUsuario(DenunciaDTO dto) throws BLException {
        if (dto == null) {
            return Respuestas.solicitudInvalida("Debe enviar los datos de la denuncia");
        }
        int id = reporteUsuarioBL.insert(ModeracionMapper.toReporteUsuario(dto));
        return Response.created(URI.create("denuncias/" + id)).entity(buscar(id)).build();
    }

    @POST
    @Path("/anuncios")
    public Response denunciarAnuncio(DenunciaDTO dto) throws BLException {
        if (dto == null) {
            return Respuestas.solicitudInvalida("Debe enviar los datos de la denuncia");
        }
        int id = reporteAnuncioBL.insert(ModeracionMapper.toReporteAnuncio(dto));
        return Response.created(URI.create("denuncias/" + id)).entity(buscar(id)).build();
    }

    /** Devuelve la denuncia resuelta; en las de usuario, cuentaSuspendida indica si se aplico RF-13. */
    @PUT
    @Path("/{id}/sancionar")
    public Response sancionar(@PathParam("id") int id, @QueryParam("administrador") int idAdministrador)
            throws BLException {
        DenunciaDTO actual = buscar(id);
        if (actual == null) {
            return Respuestas.noEncontrado("No existe la denuncia con id " + id);
        }
        boolean suspendida = false;
        if (ModeracionMapper.TIPO_USUARIO.equals(actual.getTipo())) {
            suspendida = reporteUsuarioBL.sancionar(id, idAdministrador);
        } else {
            reporteAnuncioBL.sancionar(id, idAdministrador);
        }
        DenunciaDTO dto = buscar(id);
        dto.setCuentaSuspendida(suspendida);
        return Response.ok(dto).build();
    }

    @PUT
    @Path("/{id}/desestimar")
    public Response desestimar(@PathParam("id") int id, @QueryParam("administrador") int idAdministrador)
            throws BLException {
        DenunciaDTO actual = buscar(id);
        if (actual == null) {
            return Respuestas.noEncontrado("No existe la denuncia con id " + id);
        }
        if (ModeracionMapper.TIPO_USUARIO.equals(actual.getTipo())) {
            reporteUsuarioBL.desestimar(id, idAdministrador);
        } else {
            reporteAnuncioBL.desestimar(id, idAdministrador);
        }
        return Response.ok(buscar(id)).build();
    }

    /** Tambien lo usa el reporte PDF de incidentes. Mas recientes primero. */
    public static List<DenunciaDTO> listarDenuncias(String tipo, String estado, int idDenunciante) throws BLException {
        boolean usuarios = tipo == null || tipo.isBlank() || ModeracionMapper.TIPO_USUARIO.equalsIgnoreCase(tipo);
        boolean anuncios = tipo == null || tipo.isBlank() || ModeracionMapper.TIPO_ANUNCIO.equalsIgnoreCase(tipo);
        List<DenunciaDTO> dtos = new ArrayList<>();
        if (usuarios) {
            for (ReporteUsuario reporte : new ReporteUsuarioBLImpl().findAll()) {
                if (reporte.isActivo()) {
                    dtos.add(ModeracionMapper.toDTO(reporte));
                }
            }
        }
        if (anuncios) {
            for (ReporteAnuncio reporte : new ReporteAnuncioBLImpl().findAll()) {
                if (reporte.isActivo()) {
                    dtos.add(ModeracionMapper.toDTO(reporte));
                }
            }
        }
        dtos.removeIf(d -> (estado != null && !estado.isBlank() && !estado.equalsIgnoreCase(d.getEstadoRevision()))
                || (idDenunciante > 0 && d.getIdDenunciante() != idDenunciante));
        dtos.sort(Comparator.comparing(DenunciaDTO::getFechaRegistro,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return dtos;
    }

    // Los ids vienen de la tabla padre "reporte": una denuncia es de usuario o de anuncio, no de ambos.
    private DenunciaDTO buscar(int id) throws BLException {
        ReporteUsuario reporteUsuario = reporteUsuarioBL.findById(id);
        if (reporteUsuario != null && reporteUsuario.isActivo()) {
            return ModeracionMapper.toDTO(reporteUsuario);
        }
        ReporteAnuncio reporteAnuncio = reporteAnuncioBL.findById(id);
        if (reporteAnuncio != null && reporteAnuncio.isActivo()) {
            return ModeracionMapper.toDTO(reporteAnuncio);
        }
        return null;
    }
}
