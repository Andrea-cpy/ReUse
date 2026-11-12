package pe.edu.pucp.reuse.rest.reportes;

/** Falla al cargar, llenar o exportar una plantilla JasperReports (se responde 500). */
public class ReporteException extends Exception {
    public ReporteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
