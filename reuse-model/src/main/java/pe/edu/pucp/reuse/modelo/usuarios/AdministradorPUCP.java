package pe.edu.pucp.reuse.modelo.usuarios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.enums.EstadoCuenta;
import pe.edu.pucp.reuse.modelo.moderacion.Reporte;

public class AdministradorPUCP extends UsuarioPUCP {

    private final List<Reporte> reportesRevisados;

    public AdministradorPUCP() {
        this.reportesRevisados = new ArrayList<>();
        setVerificado(true);
        setEstadoCuenta(EstadoCuenta.ACTIVA);
    }

    public AdministradorPUCP(String codigoPUCP, String nombres, String apellidoPaterno, String apellidoMaterno,
                             String correoInstitucional, String contrasena) {
        super(codigoPUCP, nombres, apellidoPaterno, apellidoMaterno, correoInstitucional, contrasena, null);
        this.reportesRevisados = new ArrayList<>();
        setVerificado(true);
        setEstadoCuenta(EstadoCuenta.ACTIVA);
    }

    public List<Reporte> getReportesRevisados() {
        return Collections.unmodifiableList(reportesRevisados);
    }

    public void agregarReporteRevisado(Reporte reporte) {
        if (reporte == null || reportesRevisados.contains(reporte)) {
            return;
        }
        reportesRevisados.add(reporte);
        reporte.setRevisor(this);
    }

    public void quitarReporteRevisado(Reporte reporte) {
        if (reportesRevisados.remove(reporte) && reporte.getRevisor() == this) {
            reporte.setRevisor(null);
        }
    }
}
