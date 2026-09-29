package pe.edu.pucp.reuse.modelo.catalogo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.reuse.modelo.Registro;
import pe.edu.pucp.reuse.modelo.enums.CondicionMaterial;
import pe.edu.pucp.reuse.modelo.enums.EstadoAnuncio;
import pe.edu.pucp.reuse.modelo.gamificacion.Favorito;
import pe.edu.pucp.reuse.modelo.mensajeria.CanalChat;
import pe.edu.pucp.reuse.modelo.moderacion.ReporteAnuncio;
import pe.edu.pucp.reuse.modelo.transacciones.Oferta;
import pe.edu.pucp.reuse.modelo.transacciones.Transaccion;
import pe.edu.pucp.reuse.modelo.usuarios.UsuarioPUCP;

public class Anuncio extends Registro {

    private int idAnuncio;
    private String titulo;
    private double precio;
    private String descripcion;
    private CondicionMaterial condicion;
    private EstadoAnuncio estado;
    private LocalDateTime fechaPublicacion;

    private UsuarioPUCP vendedor;
    private MaterialAcademico materialAcademico;
    private final List<Transaccion> transacciones;
    private final List<ImagenProducto> imagenes;
    private final List<Favorito> favoritos;
    private final List<Oferta> ofertas;
    private final List<CanalChat> canalesChat;
    private final List<ReporteAnuncio> reportes;

    public Anuncio() {
        this.estado = EstadoAnuncio.DISPONIBLE;
        this.transacciones = new ArrayList<>();
        this.imagenes = new ArrayList<>();
        this.favoritos = new ArrayList<>();
        this.ofertas = new ArrayList<>();
        this.canalesChat = new ArrayList<>();
        this.reportes = new ArrayList<>();
    }

    // El id y la fecha de publicacion los genera la base de datos.
    public Anuncio(String titulo, double precio, String descripcion, CondicionMaterial condicion,
                   UsuarioPUCP vendedor, MaterialAcademico materialAcademico) {
        this();
        this.titulo = titulo;
        this.precio = precio;
        this.descripcion = descripcion;
        this.condicion = condicion;
        setVendedor(vendedor);
        setMaterialAcademico(materialAcademico);
    }

    public int getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(int idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public CondicionMaterial getCondicion() {
        return condicion;
    }

    public void setCondicion(CondicionMaterial condicion) {
        this.condicion = condicion;
    }

    public EstadoAnuncio getEstado() {
        return estado;
    }

    public void setEstado(EstadoAnuncio estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public UsuarioPUCP getVendedor() {
        return vendedor;
    }

    public final void setVendedor(UsuarioPUCP vendedor) {
        if (this.vendedor == vendedor) {
            return;
        }
        UsuarioPUCP anterior = this.vendedor;
        this.vendedor = vendedor;
        if (anterior != null) {
            anterior.quitarAnuncioPublicado(this);
        }
        if (vendedor != null) {
            vendedor.agregarAnuncioPublicado(this);
        }
    }

    public MaterialAcademico getMaterialAcademico() {
        return materialAcademico;
    }

    public final void setMaterialAcademico(MaterialAcademico materialAcademico) {
        if (this.materialAcademico == materialAcademico) {
            return;
        }
        MaterialAcademico anterior = this.materialAcademico;
        this.materialAcademico = materialAcademico;
        if (anterior != null) {
            anterior.quitarAnuncio(this);
        }
        if (materialAcademico != null) {
            materialAcademico.agregarAnuncio(this);
        }
    }

    public List<Transaccion> getTransacciones() {
        return Collections.unmodifiableList(transacciones);
    }

    public List<ImagenProducto> getImagenes() {
        return Collections.unmodifiableList(imagenes);
    }

    public List<Favorito> getFavoritos() {
        return Collections.unmodifiableList(favoritos);
    }

    public List<Oferta> getOfertas() {
        return Collections.unmodifiableList(ofertas);
    }

    public List<CanalChat> getCanalesChat() {
        return Collections.unmodifiableList(canalesChat);
    }

    public List<ReporteAnuncio> getReportes() {
        return Collections.unmodifiableList(reportes);
    }

    public void agregarTransaccion(Transaccion transaccion) {
        if (transaccion == null || transacciones.contains(transaccion)) {
            return;
        }
        transacciones.add(transaccion);
        transaccion.setAnuncio(this);
    }

    public void quitarTransaccion(Transaccion transaccion) {
        if (transacciones.remove(transaccion) && transaccion.getAnuncio() == this) {
            transaccion.setAnuncio(null);
        }
    }

    public void agregarImagen(ImagenProducto imagen) {
        if (imagen == null || imagenes.contains(imagen)) {
            return;
        }
        imagenes.add(imagen);
        imagen.setAnuncio(this);
    }

    public void quitarImagen(ImagenProducto imagen) {
        if (imagenes.remove(imagen) && imagen.getAnuncio() == this) {
            imagen.setAnuncio(null);
        }
    }

    public void agregarFavorito(Favorito favorito) {
        if (favorito == null || favoritos.contains(favorito)) {
            return;
        }
        favoritos.add(favorito);
        favorito.setAnuncio(this);
    }

    public void quitarFavorito(Favorito favorito) {
        if (favoritos.remove(favorito) && favorito.getAnuncio() == this) {
            favorito.setAnuncio(null);
        }
    }

    public void agregarOferta(Oferta oferta) {
        if (oferta == null || ofertas.contains(oferta)) {
            return;
        }
        ofertas.add(oferta);
        oferta.setAnuncio(this);
    }

    public void quitarOferta(Oferta oferta) {
        if (ofertas.remove(oferta) && oferta.getAnuncio() == this) {
            oferta.setAnuncio(null);
        }
    }

    public void agregarCanalChat(CanalChat canalChat) {
        if (canalChat == null || canalesChat.contains(canalChat)) {
            return;
        }
        canalesChat.add(canalChat);
        canalChat.setAnuncio(this);
    }

    public void quitarCanalChat(CanalChat canalChat) {
        if (canalesChat.remove(canalChat) && canalChat.getAnuncio() == this) {
            canalChat.setAnuncio(null);
        }
    }

    public void agregarReporte(ReporteAnuncio reporte) {
        if (reporte == null || reportes.contains(reporte)) {
            return;
        }
        reportes.add(reporte);
        reporte.setAnuncio(this);
    }

    public void quitarReporte(ReporteAnuncio reporte) {
        if (reportes.remove(reporte) && reporte.getAnuncio() == this) {
            reporte.setAnuncio(null);
        }
    }
}
