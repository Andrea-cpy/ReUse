package pe.edu.pucp.reuse.rest.reportes;

import java.io.File;

import net.sf.jasperreports.engine.JasperCompileManager;

/**
 * Precompila las plantillas (buena practica de la Sesion 25: no recompilar el .jrxml en cada
 * solicitud). Convierte cada src/main/resources/reports/*.jrxml en su .jasper al lado.
 * Ejecutar desde el IDE (Run 'CompilarReportes.main()') cada vez que se modifique un .jrxml;
 * acepta como argumento otra carpeta de plantillas.
 */
public final class CompilarReportes {
    private CompilarReportes() {
    }

    public static void main(String[] args) throws Exception {
        File carpeta = new File(args.length > 0 ? args[0] : "src/main/resources/reports");
        File[] plantillas = carpeta.listFiles((dir, nombre) -> nombre.endsWith(".jrxml"));
        if (plantillas == null || plantillas.length == 0) {
            System.err.println("No hay plantillas .jrxml en " + carpeta.getAbsolutePath());
            return;
        }
        for (File jrxml : plantillas) {
            String destino = jrxml.getPath().replaceFirst("\\.jrxml$", ".jasper");
            JasperCompileManager.compileReportToFile(jrxml.getPath(), destino);
            System.out.println("Compilado: " + destino);
        }
    }
}
