package pe.edu.pucp.reuse.modelo.moderacion;

import java.time.LocalDateTime;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.EstadoRevisionReporte;
import pe.edu.pucp.reuse.modelo.usuarios.AdministradorPUCP;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public abstract class Reporte extends Registro {

    private int idReporte;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private EstadoRevisionReporte estadoRevision;
    private LocalDateTime fechaRevision;

    private UsuarioPUCP denunciante;
    private AdministradorPUCP revisor;

    protected Reporte() {
        this.estadoRevision = EstadoRevisionReporte.PENDIENTE;
    }

    protected Reporte(String descripcion, UsuarioPUCP denunciante) {
        this();
        this.descripcion = descripcion;
        setDenunciante(denunciante);
    }

    public abstract UsuarioPUCP getUsuarioResponsable();

    public int getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public EstadoRevisionReporte getEstadoRevision() {
        return estadoRevision;
    }

    public void setEstadoRevision(EstadoRevisionReporte estadoRevision) {
        this.estadoRevision = estadoRevision;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public UsuarioPUCP getDenunciante() {
        return denunciante;
    }

    public final void setDenunciante(UsuarioPUCP denunciante) {
        if (this.denunciante == denunciante) {
            return;
        }
        UsuarioPUCP anterior = this.denunciante;
        this.denunciante = denunciante;
        if (anterior != null) {
            anterior.quitarReporteRealizado(this);
        }
        if (denunciante != null) {
            denunciante.agregarReporteRealizado(this);
        }
    }

    public AdministradorPUCP getRevisor() {
        return revisor;
    }

    public void setRevisor(AdministradorPUCP revisor) {
        if (this.revisor == revisor) {
            return;
        }
        AdministradorPUCP anterior = this.revisor;
        this.revisor = revisor;
        if (anterior != null) {
            anterior.quitarReporteRevisado(this);
        }
        if (revisor != null) {
            revisor.agregarReporteRevisado(this);
        }
    }
}
