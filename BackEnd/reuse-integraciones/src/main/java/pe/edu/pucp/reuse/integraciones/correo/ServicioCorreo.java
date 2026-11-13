package pe.edu.pucp.reuse.integraciones.correo;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import pe.edu.pucp.reuse.integraciones.CertificadosJdk;

/**
 * Envio de correos por SMTP con Jakarta Mail (la implementacion, Angus Mail, la pone GlassFish).
 * <p>
 * La conexion usa STARTTLS obligatorio (nunca envia la clave sin cifrar) y timeouts de 10 s.
 * Los envios se hacen en segundo plano, en un unico hilo daemon: quien llama no espera al
 * servidor SMTP y un fallo solo se registra en el log, sin propagarse.
 * <p>
 * La configuracion se lee de correo.properties (claves smtp.host, smtp.puerto, smtp.usuario,
 * smtp.clave y remitente.nombre). Las variables de entorno REUSE_SMTP_USUARIO y
 * REUSE_SMTP_CLAVE tienen prioridad sobre el archivo.
 */
public final class ServicioCorreo {
    private static final String HOST_POR_DEFECTO = "smtp.gmail.com";
    private static final int PUERTO_POR_DEFECTO = 587;
    private static final String REMITENTE_POR_DEFECTO = "ReUse PUCP";
    private static final String MILISEGUNDOS_ESPERA = "10000";

    private static final Logger LOG = Logger.getLogger(ServicioCorreo.class.getName());

    private static volatile ServicioCorreo instancia;

    private final String host;
    private final int puerto;
    private final String usuario;
    private final String clave;
    private final String remitenteNombre;
    private final ExecutorService envios;

    private ServicioCorreo(ResourceBundle configuracion) {
        host = valor(null, configuracion, "smtp.host", HOST_POR_DEFECTO);
        puerto = leerPuerto(configuracion);
        usuario = valor("REUSE_SMTP_USUARIO", configuracion, "smtp.usuario", null);
        clave = valor("REUSE_SMTP_CLAVE", configuracion, "smtp.clave", null);
        remitenteNombre = valor(null, configuracion, "remitente.nombre", REMITENTE_POR_DEFECTO);
        envios = Executors.newSingleThreadExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "reuse-envio-correo");
            hilo.setDaemon(true); // no impide que el servidor se detenga
            return hilo;
        });
        if (!estaConfigurado()) {
            LOG.warning("Correo sin configurar: falta smtp.usuario/smtp.clave en correo.properties"
                    + " o las variables de entorno REUSE_SMTP_USUARIO/REUSE_SMTP_CLAVE");
        }
    }

    public static ServicioCorreo getInstance() {
        if (instancia == null) {
            synchronized (ServicioCorreo.class) {
                if (instancia == null) {
                    instancia = new ServicioCorreo(leerConfiguracion());
                }
            }
        }
        return instancia;
    }

    /** true si hay usuario y clave SMTP; sin ellos no se puede enviar nada. */
    public boolean estaConfigurado() {
        return usuario != null && clave != null;
    }

    /**
     * Envia en segundo plano el codigo de recuperacion de contrasena. Retorna de inmediato;
     * si el envio falla, solo se registra en el log del servidor.
     */
    public void enviarCodigoRecuperacion(String destinatario, String codigo, int minutosValidez) {
        String asunto = "Tu código para recuperar tu contraseña de ReUse";
        String html = htmlCodigoRecuperacion(codigo, minutosValidez);
        envios.submit(() -> enviar(destinatario, asunto, html));
    }

    private void enviar(String destinatario, String asunto, String html) {
        try {
            MimeMessage mensaje = new MimeMessage(crearSesion());
            mensaje.setFrom(new InternetAddress(usuario, remitenteNombre, "UTF-8"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario, true));
            mensaje.setSubject(asunto, "UTF-8");
            mensaje.setContent(html, "text/html; charset=UTF-8");
            mensaje.setSentDate(new Date());
            Transport.send(mensaje, usuario, clave);
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo enviar el correo a " + destinatario + " por " + host + ":" + puerto, e);
        }
    }

    private Session crearSesion() {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", host);
        propiedades.put("mail.smtp.port", String.valueOf(puerto));
        propiedades.put("mail.smtp.auth", "true");
        // STARTTLS obligatorio: si el servidor no lo ofrece, no se envia nada.
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.starttls.required", "true");
        propiedades.put("mail.smtp.ssl.checkserveridentity", "true");
        propiedades.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        // Autoridades del JDK: el almacen de GlassFish no trae la de smtp.gmail.com (ver CertificadosJdk).
        propiedades.put("mail.smtp.ssl.socketFactory", CertificadosJdk.fabricaSsl());
        propiedades.put("mail.smtp.connectiontimeout", MILISEGUNDOS_ESPERA);
        propiedades.put("mail.smtp.timeout", MILISEGUNDOS_ESPERA);
        propiedades.put("mail.smtp.writetimeout", MILISEGUNDOS_ESPERA);
        return Session.getInstance(propiedades);
    }

    // El texto usa entidades HTML para las tildes: el codigo fuente queda sin tildes.
    private static String htmlCodigoRecuperacion(String codigo, int minutosValidez) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"margin:0;padding:24px;background:#f4f4f5;font-family:Arial,Helvetica,sans-serif;color:#1f2937\">"
                + "<div style=\"max-width:480px;margin:0 auto;background:#ffffff;border-radius:8px;padding:32px\">"
                + "<h1 style=\"margin:0 0 16px;font-size:20px\">Recupera tu contrase&ntilde;a de ReUse</h1>"
                + "<p style=\"margin:0 0 16px\">Usa este c&oacute;digo para crear una nueva contrase&ntilde;a:</p>"
                + "<p style=\"margin:0 0 16px;font-size:36px;font-weight:bold;letter-spacing:8px;text-align:center\">"
                + codigo + "</p>"
                + "<p style=\"margin:0 0 16px\">El c&oacute;digo vence en " + minutosValidez + " minutos.</p>"
                + "<p style=\"margin:0;color:#6b7280;font-size:13px\">Si no fuiste t&uacute;, ignora este correo:"
                + " tu contrase&ntilde;a no cambiar&aacute;.</p>"
                + "</div></body></html>";
    }

    private static ResourceBundle leerConfiguracion() {
        try {
            return ResourceBundle.getBundle("correo");
        } catch (MissingResourceException e) {
            return null; // sin archivo: solo cuentan las variables de entorno y los valores por defecto
        }
    }

    // Variable de entorno (si se indica y tiene valor), luego correo.properties y al final el valor por defecto.
    private static String valor(String variableEntorno, ResourceBundle configuracion, String clave, String porDefecto) {
        String valor = variableEntorno == null ? null : System.getenv(variableEntorno);
        if ((valor == null || valor.isBlank()) && configuracion != null && configuracion.containsKey(clave)) {
            valor = configuracion.getString(clave);
        }
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }

    private static int leerPuerto(ResourceBundle configuracion) {
        String texto = valor(null, configuracion, "smtp.puerto", String.valueOf(PUERTO_POR_DEFECTO));
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            LOG.severe("smtp.puerto invalido en correo.properties (" + texto + "); se usa " + PUERTO_POR_DEFECTO);
            return PUERTO_POR_DEFECTO;
        }
    }
}
