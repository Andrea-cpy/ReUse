package pe.edu.pucp.reuse.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import pe.edu.pucp.reuse.bl.BLException;
import pe.edu.pucp.reuse.modelo.catalogo.Anuncio;
import pe.edu.pucp.reuse.modelo.catalogo.ImagenProducto;

/**
 * RF-02: el vendedor indica las fotos de su anuncio como URLs. La tabla imagen_producto
 * guarda tambien el peso y el formato reales, asi que aqui se descarga cada imagen para
 * conocerlos. Las reglas (maximo 5, 5 MB, jpg/png/webp) las valida la capa de negocio.
 */
final class ImagenesRemotas {
    // Se lee un byte mas del limite de la capa de negocio para que ella rechace las imagenes muy pesadas.
    private static final long LIMITE_LECTURA = 5L * 1024 * 1024 + 1;
    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private ImagenesRemotas() {
    }

    /** Agrega al anuncio una ImagenProducto por cada URL, con su peso y formato reales. */
    static void agregar(Anuncio anuncio, List<String> urls) throws BLException {
        if (urls == null) {
            return;
        }
        for (String texto : urls) {
            if (texto == null || texto.isBlank()) {
                continue;
            }
            String url = texto.trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new BLException("La URL de la imagen debe empezar con http:// o https://");
            }
            anuncio.agregarImagen(descargar(url));
        }
    }

    private static ImagenProducto descargar(String url) throws BLException {
        HttpResponse<InputStream> respuesta;
        try {
            HttpRequest solicitud = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            respuesta = CLIENTE.send(solicitud, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IllegalArgumentException | IOException e) {
            throw new BLException("No se pudo acceder a la imagen " + url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BLException("Se interrumpio la descarga de la imagen " + url);
        }
        if (respuesta.statusCode() != 200) {
            throw new BLException("La imagen " + url + " no esta disponible (codigo " + respuesta.statusCode() + ")");
        }
        String tipo = respuesta.headers().firstValue("Content-Type").orElse("");
        if (!tipo.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BLException("La URL " + url + " no corresponde a una imagen");
        }
        long peso;
        try (InputStream cuerpo = respuesta.body()) {
            peso = cuerpo.readNBytes((int) LIMITE_LECTURA).length;
        } catch (IOException e) {
            throw new BLException("No se pudo leer la imagen " + url);
        }
        ImagenProducto imagen = new ImagenProducto();
        imagen.setUrl(url);
        imagen.setPesoBytes(peso);
        imagen.setFormato(formato(tipo, url));
        return imagen;
    }

    /** jpg, png, webp... segun el Content-Type; si no es claro, segun la extension de la URL. */
    private static String formato(String tipo, String url) {
        String subtipo = tipo.toLowerCase(Locale.ROOT).replace("image/", "").split(";")[0].trim();
        if (subtipo.equals("jpeg") || subtipo.equals("pjpeg")) {
            return "jpg";
        }
        if (!subtipo.isEmpty()) {
            return subtipo;
        }
        String ruta = URI.create(url).getPath();
        int punto = ruta == null ? -1 : ruta.lastIndexOf('.');
        return punto < 0 ? "" : ruta.substring(punto + 1).toLowerCase(Locale.ROOT);
    }
}
