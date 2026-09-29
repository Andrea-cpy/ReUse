package pe.edu.pucp.reuse.modelo.moderacion;

import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.enums.MotivoReporteAnuncio;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class ReporteAnuncio extends Reporte {

    private MotivoReporteAnuncio motivo;

    private Anuncio anuncio;

    public ReporteAnuncio() {
    }

    public ReporteAnuncio(String descripcion, MotivoReporteAnuncio motivo, UsuarioPUCP denunciante,
                          Anuncio anuncio) {
        super(descripcion, denunciante);
        this.motivo = motivo;
        setAnuncio(anuncio);
    }

    @Override
    public UsuarioPUCP getUsuarioResponsable() {
        return anuncio == null ? null : anuncio.getVendedor();
    }

    public MotivoReporteAnuncio getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoReporteAnuncio motivo) {
        this.motivo = motivo;
    }

    public Anuncio getAnuncio() {
        return anuncio;
    }

    public final void setAnuncio(Anuncio anuncio) {
        if (this.anuncio == anuncio) {
            return;
        }
        Anuncio anterior = this.anuncio;
        this.anuncio = anuncio;
        if (anterior != null) {
            anterior.quitarReporte(this);
        }
        if (anuncio != null) {
            anuncio.agregarReporte(this);
        }
    }
}
