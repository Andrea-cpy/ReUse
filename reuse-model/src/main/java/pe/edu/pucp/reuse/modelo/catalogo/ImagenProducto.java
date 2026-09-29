package pe.edu.pucp.reuse.modelo.catalogo;

import pe.edu.pucp.reuse.modelo.Registro;

public class ImagenProducto extends Registro {

    private int idImagen;
    private String url;
    private long pesoBytes;
    private String formato;

    private Anuncio anuncio;

    public ImagenProducto() {
    }

    public ImagenProducto(String url, long pesoBytes, String formato, Anuncio anuncio) {
        this();
        this.url = url;
        this.pesoBytes = pesoBytes;
        this.formato = formato;
        setAnuncio(anuncio);
    }

    public int getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(int idImagen) {
        this.idImagen = idImagen;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getPesoBytes() {
        return pesoBytes;
    }

    public void setPesoBytes(long pesoBytes) {
        this.pesoBytes = pesoBytes;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
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
            anterior.quitarImagen(this);
        }
        if (anuncio != null) {
            anuncio.agregarImagen(this);
        }
    }
}
