package pe.edu.pucp.reuse.rest.reportes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.pdf.SimplePdfExporterConfiguration;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.bl.impl.ReporteUsuarioBLImpl;
import pe.edu.pucp.reuse.bl.impl.TransaccionBLImpl;
import pe.edu.pucp.reuse.bl.impl.UsuarioBLImpl;
import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.enums.EstadoTransaccion;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;
import pe.edu.pucp.reuse.rest.dto.DenunciaDTO;
import pe.edu.pucp.reuse.rest.mapper.TransaccionMapper;
import pe.edu.pucp.reuse.rest.mapper.UsuarioMapper;

/**
 * RF-04: generacion de reportes PDF con JasperReports (Sesion 25).
 * <ol>
 *   <li>Diseno: plantillas .jrxml en src/main/resources/reports.</li>
 *   <li>Compilacion: precompiladas a .jasper (CompilarReportes); si faltara el .jasper, se compila
 *       el .jrxml una sola vez.</li>
 *   <li>Llenado: JasperFillManager con un JRBeanCollectionDataSource (lista de objetos Java).</li>
 *   <li>Exportacion: JRPdfExporter con metadatos (titulo, autor, asunto).</li>
 * </ol>
 * Singleton: el JasperReport compilado se carga una vez y queda en cache.
 */
public final class ReporteService {
    public static final String INCIDENTES = "ReporteIncidentes";
    public static final String TRANSACCIONES = "ReporteTransacciones";
    public static final String USUARIOS = "ReporteUsuarios";

    private static final Logger LOG = Logger.getLogger(ReporteService.class.getName());
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ReporteService INSTANCIA = new ReporteService();

    private final Map<String, JasperReport> cache = new ConcurrentHashMap<>();

    private ReporteService() {
        // Los reportes se generan en el servidor, sin pantalla.
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true");
        }
    }

    public static ReporteService getInstance() {
        return INSTANCIA;
    }

    // ---------------------------------------------------------------- reportes del sistema

    /** Denuncias (de usuarios y de anuncios) con su estado de revision, filtradas como la bandeja. */
    public byte[] incidentes(List<DenunciaDTO> denuncias, String filtros, String generadoPor) throws ReporteException {
        List<FilaIncidente> filas = new ArrayList<>();
        int pendientes = 0;
        int sancionados = 0;
        int desestimados = 0;
        for (DenunciaDTO d : denuncias) {
            boolean deUsuario = "USUARIO".equals(d.getTipo());
            String reportado = deUsuario
                    ? d.getNombreDenunciado() + (d.getCodigoDenunciado() == null ? "" : " (" + d.getCodigoDenunciado() + ")")
                    : d.getTituloAnuncio();
            filas.add(new FilaIncidente(d.getIdReporte(), deUsuario ? "Usuario" : "Anuncio", reportado,
                    texto(d.getMotivo()), d.getNombreDenunciante(), fecha(d.getFechaRegistro()),
                    texto(d.getEstadoRevision()), d.getDescripcion(), d.getNombreRevisor()));
            switch (d.getEstadoRevision() == null ? "" : d.getEstadoRevision()) {
                case "PENDIENTE" -> pendientes++;
                case "SANCIONADO" -> sancionados++;
                case "DESESTIMADO" -> desestimados++;
                default -> { }
            }
        }
        Map<String, Object> parametros = parametrosBase("Reporte de incidentes y denuncias", filtros, generadoPor);
        parametros.put("TOTAL_PENDIENTES", pendientes);
        parametros.put("TOTAL_SANCIONADOS", sancionados);
        parametros.put("TOTAL_DESESTIMADOS", desestimados);
        return exportarPdf(INCIDENTES, "Reporte de incidentes - ReUse", parametros, filas);
    }

    /** Historial de transacciones con los montos acordados y el total de lo vendido. */
    public byte[] transacciones(String estado, String generadoPor) throws ReporteException, BLException {
        Map<Integer, String> nombres = new HashMap<>();
        for (UsuarioPUCP usuario : new UsuarioBLImpl().findAll()) {
            nombres.put(usuario.getIdUsuario(), UsuarioMapper.nombre(usuario));
        }
        List<Transaccion> transacciones = new TransaccionBLImpl().findAll();
        transacciones.sort(Comparator.comparing(Transaccion::getFechaInicio,
                Comparator.nullsLast(Comparator.reverseOrder())));
        List<FilaTransaccion> filas = new ArrayList<>();
        int completadas = 0;
        int enCurso = 0;
        int canceladas = 0;
        double montoCompletado = 0;
        for (Transaccion t : transacciones) {
            if (!t.isActivo() || (estado != null && !estado.isBlank()
                    && (t.getEstado() == null || !t.getEstado().name().equalsIgnoreCase(estado)))) {
                continue;
            }
            // El monto acordado es el de la oferta aceptada o, si no hubo oferta, el precio publicado.
            double monto = TransaccionMapper.toDTO(t).getMontoAcordado();
            int idVendedor = t.getAnuncio() == null || t.getAnuncio().getVendedor() == null
                    ? 0 : t.getAnuncio().getVendedor().getIdUsuario();
            filas.add(new FilaTransaccion(t.getIdTransaccion(),
                    t.getAnuncio() == null ? "" : t.getAnuncio().getTitulo(),
                    nombres.getOrDefault(idVendedor, "-"), UsuarioMapper.nombre(t.getComprador()),
                    texto(t.getEstado() == null ? null : t.getEstado().name()),
                    fecha(t.getFechaInicio()), fecha(t.getFechaFin()), monto));
            if (t.getEstado() == EstadoTransaccion.COMPLETADA) {
                completadas++;
                montoCompletado += monto;
            } else if (t.getEstado() == EstadoTransaccion.CANCELADA) {
                canceladas++;
            } else {
                enCurso++;
            }
        }
        String filtros = estado == null || estado.isBlank() ? "Todas las transacciones" : "Estado: " + texto(estado);
        Map<String, Object> parametros = parametrosBase("Reporte de transacciones", filtros, generadoPor);
        parametros.put("TOTAL_COMPLETADAS", completadas);
        parametros.put("TOTAL_EN_CURSO", enCurso);
        parametros.put("TOTAL_CANCELADAS", canceladas);
        parametros.put("MONTO_COMPLETADO", montoCompletado);
        return exportarPdf(TRANSACCIONES, "Reporte de transacciones - ReUse", parametros, filas);
    }

    /** RF-13 / RF-18: reputacion separada, reportes recibidos y sanciones de cada estudiante. */
    public byte[] usuarios(String generadoPor) throws ReporteException, BLException {
        ReporteUsuarioBLImpl reportes = new ReporteUsuarioBLImpl();
        List<UsuarioPUCP> usuarios = new UsuarioBLImpl().findAll();
        usuarios.sort(Comparator.comparing(UsuarioPUCP::getApellidos, Comparator.nullsLast(String::compareTo)));
        List<FilaUsuario> filas = new ArrayList<>();
        int activas = 0;
        int suspendidas = 0;
        int pendientes = 0;
        int enRiesgo = 0;
        for (UsuarioPUCP u : usuarios) {
            if (!u.isActivo()) {
                continue;
            }
            int sanciones = reportes.contarSancionesRecientes(u.getIdUsuario());
            String nombre = (u.getNombres() + " " + (u.getApellidos() == null ? "" : u.getApellidos())).trim();
            filas.add(new FilaUsuario(u.getCodigoPUCP(), nombre,
                    u.getCarrera() == null ? "-" : u.getCarrera().getNombre(),
                    texto(u.getEstadoCuenta() == null ? null : u.getEstadoCuenta().name()),
                    u.getReputacionVendedor(), u.getReputacionComprador(), u.getContadorReportes(), sanciones));
            if (u.getEstadoCuenta() == EstadoCuenta.ACTIVA) {
                activas++;
            } else if (u.getEstadoCuenta() == EstadoCuenta.SUSPENDIDA) {
                suspendidas++;
            } else {
                pendientes++;
            }
            if (sanciones >= 2 && u.getEstadoCuenta() != EstadoCuenta.SUSPENDIDA) {
                enRiesgo++;
            }
        }
        Map<String, Object> parametros = parametrosBase("Reputación y conducta de usuarios",
                "Sanciones contadas en los últimos 90 días (con 4 o más la cuenta se suspende)", generadoPor);
        parametros.put("TOTAL_ACTIVAS", activas);
        parametros.put("TOTAL_SUSPENDIDAS", suspendidas);
        parametros.put("TOTAL_PENDIENTES", pendientes);
        parametros.put("TOTAL_EN_RIESGO", enRiesgo);
        return exportarPdf(USUARIOS, "Reputación y conducta de usuarios - ReUse", parametros, filas);
    }

    // ---------------------------------------------------------------- motor JasperReports

    /** Llena la plantilla con la lista de objetos y la exporta a PDF en memoria. */
    public byte[] exportarPdf(String plantilla, String tituloPdf, Map<String, Object> parametros,
                              Collection<?> filas) throws ReporteException {
        try {
            JasperPrint impresion;
            try {
                impresion = JasperFillManager.fillReport(cargar(plantilla, false), parametros,
                        new JRBeanCollectionDataSource(filas));
            } catch (LinkageError e) {
                // El .jasper trae clases compiladas con un JDK mas nuevo que el del servidor
                // (UnsupportedClassVersionError): se recompila el .jrxml con el JDK del servidor.
                LOG.warning("El " + plantilla + ".jasper no es compatible con esta JVM (" + e.getMessage()
                        + "); se recompila el .jrxml");
                impresion = JasperFillManager.fillReport(cargar(plantilla, true), parametros,
                        new JRBeanCollectionDataSource(filas));
            }

            SimplePdfExporterConfiguration configuracion = new SimplePdfExporterConfiguration();
            configuracion.setMetadataTitle(tituloPdf);
            configuracion.setMetadataAuthor("ReUse - Marketplace academico PUCP");
            configuracion.setMetadataSubject("Programacion 3 (1INF30) - Erasmolovers");
            configuracion.setMetadataCreator("JasperReports 7");

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            JRPdfExporter exportador = new JRPdfExporter();
            exportador.setExporterInput(new SimpleExporterInput(impresion));
            exportador.setExporterOutput(new SimpleOutputStreamExporterOutput(salida));
            exportador.setConfiguration(configuracion);
            exportador.exportReport();
            return salida.toByteArray();
        } catch (JRException e) {
            throw new ReporteException("No se pudo generar el reporte " + plantilla + ": " + e.getMessage(), e);
        }
    }

    // Carga el .jasper precompilado del classpath (o compila el .jrxml la primera vez) y lo guarda en cache.
    private JasperReport cargar(String plantilla, boolean compilarJrxml) throws JRException {
        JasperReport enCache = cache.get(plantilla);
        if (enCache != null && !compilarJrxml) {
            return enCache;
        }
        JasperReport reporte;
        String recursoJasper = compilarJrxml ? "/reports/" + plantilla + ".no-usar" : "/reports/" + plantilla + ".jasper";
        try (InputStream jasper = ReporteService.class.getResourceAsStream(recursoJasper)) {
            if (jasper != null) {
                reporte = (JasperReport) JRLoader.loadObject(jasper);
            } else {
                try (InputStream jrxml = ReporteService.class.getResourceAsStream("/reports/" + plantilla + ".jrxml")) {
                    if (jrxml == null) {
                        throw new JRException("No existe la plantilla /reports/" + plantilla + ".jrxml");
                    }
                    LOG.info("No se encontro " + plantilla + ".jasper; se compila el .jrxml una sola vez");
                    reporte = JasperCompileManager.compileReport(jrxml);
                }
            }
        } catch (java.io.IOException e) {
            throw new JRException("No se pudo leer la plantilla " + plantilla, e);
        }
        cache.put(plantilla, reporte);
        return reporte;
    }

    private static Map<String, Object> parametrosBase(String titulo, String filtros, String generadoPor) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO", titulo);
        parametros.put("FILTROS", filtros);
        parametros.put("GENERADO_POR", generadoPor == null || generadoPor.isBlank() ? "Administrador ReUse" : generadoPor);
        parametros.put("FECHA_GENERACION", LocalDateTime.now().format(FECHA_HORA));
        return parametros;
    }

    // Etiquetas que llevan tilde; el resto se arma a partir del nombre del enum.
    private static final Map<String, String> ETIQUETAS = Map.of(
            "EN_NEGOCIACION", "En negociación",
            "NO_SE_PRESENTO_A_LA_CITA", "No se presentó a la cita",
            "INFORMACION_FALSA", "Información falsa",
            "PENDIENTE_VERIFICACION", "Pendiente de verificación");

    // "COMPORTAMIENTO_INAPROPIADO" -> "Comportamiento inapropiado"
    static String texto(String valorEnum) {
        if (valorEnum == null || valorEnum.isBlank()) {
            return "-";
        }
        if (ETIQUETAS.containsKey(valorEnum)) {
            return ETIQUETAS.get(valorEnum);
        }
        String t = valorEnum.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static String fecha(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    private static String fecha(String iso) {
        if (iso == null || iso.isBlank()) {
            return "-";
        }
        try {
            return LocalDateTime.parse(iso).format(FECHA);
        } catch (RuntimeException e) {
            return iso;
        }
    }
}
