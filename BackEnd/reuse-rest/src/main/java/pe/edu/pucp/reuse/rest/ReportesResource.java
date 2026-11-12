package pe.edu.pucp.reuse.rest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.impl.UsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.dto.DenunciaDTO;
import pe.edu.pucp.reuse.rest.mapper.UsuarioMapper;
import pe.edu.pucp.reuse.rest.reportes.ReporteException;
import pe.edu.pucp.reuse.rest.reportes.ReporteService;

/**
 * RF-04: reportes formales en PDF generados con JasperReports. Cada uno se descarga como
 * archivo adjunto (cabecera Content-Disposition), como se recomienda en la Sesion 25.
 * <ul>
 *   <li>GET /reportes/incidentes?estado=&amp;tipo=&amp;administrador=ID: denuncias y su resolucion.</li>
 *   <li>GET /reportes/transacciones?estado=&amp;administrador=ID: historial y monto vendido.</li>
 *   <li>GET /reportes/usuarios?administrador=ID: reputacion, reportes y sanciones (RF-13, RF-18).</li>
 * </ul>
 */
@Path("/reportes")
public class ReportesResource {
    private static final Logger LOG = Logger.getLogger(ReportesResource.class.getName());
    private static final String PDF = "application/pdf";

    @GET
    @Path("/incidentes")
    @Produces({PDF, MediaType.APPLICATION_JSON})
    public Response incidentes(@QueryParam("estado") String estado, @QueryParam("tipo") String tipo,
                               @QueryParam("administrador") int idAdministrador) throws BLException {
        List<DenunciaDTO> denuncias = DenunciasResource.listarDenuncias(tipo, estado, 0);
        String filtros = (tipo == null || tipo.isBlank() ? "Usuarios y anuncios" : "Solo " + tipo.toLowerCase() + "s")
                + " - " + (estado == null || estado.isBlank() ? "todos los estados" : "estado " + estado.toLowerCase());
        String autor = nombre(idAdministrador);
        return pdf("incidentes", () -> ReporteService.getInstance().incidentes(denuncias, filtros, autor));
    }

    @GET
    @Path("/transacciones")
    @Produces({PDF, MediaType.APPLICATION_JSON})
    public Response transacciones(@QueryParam("estado") String estado,
                                  @QueryParam("administrador") int idAdministrador) throws BLException {
        String autor = nombre(idAdministrador);
        return pdf("transacciones", () -> ReporteService.getInstance().transacciones(estado, autor));
    }

    @GET
    @Path("/usuarios")
    @Produces({PDF, MediaType.APPLICATION_JSON})
    public Response usuarios(@QueryParam("administrador") int idAdministrador) throws BLException {
        String autor = nombre(idAdministrador);
        return pdf("usuarios", () -> ReporteService.getInstance().usuarios(autor));
    }

    @FunctionalInterface
    private interface Generador {
        byte[] generar() throws ReporteException, BLException;
    }

    private Response pdf(String nombre, Generador generador) throws BLException {
        try {
            byte[] contenido = generador.generar();
            String archivo = "reuse-" + nombre + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
            return Response.ok(contenido, PDF)
                    .header("Content-Disposition", "attachment; filename=\"" + archivo + "\"")
                    .header("Content-Length", contenido.length)
                    .build();
        } catch (ReporteException e) {
            String idSolicitud = UUID.randomUUID().toString();
            LOG.log(Level.SEVERE, "[" + idSolicitud + "] " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "No se pudo generar el reporte PDF", "idSolicitud", idSolicitud))
                    .build();
        }
    }

    private static String nombre(int idUsuario) throws BLException {
        if (idUsuario <= 0) {
            return null;
        }
        UsuarioPUCP usuario = new UsuarioBLImpl().findById(idUsuario);
        return usuario == null ? null : UsuarioMapper.nombre(usuario);
    }
}
