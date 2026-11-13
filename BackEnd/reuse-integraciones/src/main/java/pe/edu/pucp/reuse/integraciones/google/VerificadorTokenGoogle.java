package pe.edu.pucp.reuse.integraciones.google;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import pe.edu.pucp.reuse.integraciones.CertificadosJdk;
import pe.edu.pucp.reuse.integraciones.IntegracionException;
import pe.edu.pucp.reuse.integraciones.IntegracionException.Motivo;

/**
 * Valida el ID token (JWT) que Google entrega al frontend tras el inicio de sesion
 * (OpenID Connect) y devuelve sus datos.
 * <p>
 * Solo comprueba lo tecnico: firma RS256 con las claves publicas de Google, emisor, audiencia
 * (el Client ID de ReUse: un token emitido para otra aplicacion se rechaza) y vencimiento.
 * Las reglas de ReUse (dominio pucp.edu.pe, correo verificado) las aplica la capa de negocio.
 * <p>
 * El Client ID se lee de google.properties (clave clientId) o de la variable de entorno
 * REUSE_GOOGLE_CLIENT_ID, que tiene prioridad.
 */
public final class VerificadorTokenGoogle {
    private static final String URL_CLAVES = "https://www.googleapis.com/oauth2/v3/certs";
    private static final int MILISEGUNDOS_CONEXION = 5_000;
    private static final int MILISEGUNDOS_LECTURA = 5_000;
    private static final int BYTES_MAXIMOS = 512 * 1024;
    private static final Set<String> EMISORES = Set.of("https://accounts.google.com", "accounts.google.com");

    private static final Logger LOG = Logger.getLogger(VerificadorTokenGoogle.class.getName());

    private static volatile VerificadorTokenGoogle instancia;

    private final DefaultJWTProcessor<SecurityContext> procesador;

    private VerificadorTokenGoogle(String clientId) {
        URL urlClaves;
        try {
            urlClaves = URI.create(URL_CLAVES).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        // Las claves publicas de Google rotan: el builder las guarda en cache y las vuelve a pedir si cambian.
        // La descarga usa los certificados raiz del JDK (ver CertificadosJdk).
        DefaultResourceRetriever descargador = new DefaultResourceRetriever(
                MILISEGUNDOS_CONEXION, MILISEGUNDOS_LECTURA, BYTES_MAXIMOS, true, CertificadosJdk.fabricaSsl());
        JWKSource<SecurityContext> claves = JWKSourceBuilder.create(urlClaves, descargador).retrying(true).build();
        procesador = new DefaultJWTProcessor<>();
        procesador.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, claves));
        procesador.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                clientId, null, Set.of("iss", "sub", "email", "exp", "iat")));
    }

    public static VerificadorTokenGoogle getInstance() throws IntegracionException {
        if (instancia == null) {
            synchronized (VerificadorTokenGoogle.class) {
                if (instancia == null) {
                    instancia = new VerificadorTokenGoogle(leerClientId());
                }
            }
        }
        return instancia;
    }

    /** Valida el token y devuelve sus datos sin interpretar; cualquier fallo es una IntegracionException. */
    public DatosTokenGoogle verificar(String idToken) throws IntegracionException {
        if (idToken == null || idToken.isBlank()) {
            throw new IntegracionException(Motivo.TOKEN_RECHAZADO, "Token de Google vacio");
        }
        JWTClaimsSet claims;
        try {
            claims = procesador.process(idToken.trim(), null);
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new IntegracionException(Motivo.TOKEN_RECHAZADO,
                    "Google rechazo el token: " + e.getMessage(), e);
        }
        if (!EMISORES.contains(claims.getIssuer())) {
            throw new IntegracionException(Motivo.EMISOR_DESCONOCIDO,
                    "Emisor del token desconocido: " + claims.getIssuer());
        }
        try {
            return new DatosTokenGoogle(
                    claims.getStringClaim("email"),
                    claims.getBooleanClaim("email_verified"),
                    claims.getStringClaim("hd"),
                    claims.getStringClaim("given_name"),
                    claims.getStringClaim("family_name"));
        } catch (ParseException e) {
            throw new IntegracionException(Motivo.FORMATO_INESPERADO,
                    "Claims del token de Google con formato inesperado: " + e.getMessage(), e);
        }
    }

    private static String leerClientId() throws IntegracionException {
        String clientId = System.getenv("REUSE_GOOGLE_CLIENT_ID");
        if (clientId == null || clientId.isBlank()) {
            try {
                clientId = ResourceBundle.getBundle("google").getString("clientId");
            } catch (MissingResourceException e) {
                clientId = null;
            }
        }
        if (clientId == null || clientId.isBlank()) {
            String detalle = "Ingreso con Google sin configurar: falta clientId en google.properties"
                    + " o la variable de entorno REUSE_GOOGLE_CLIENT_ID";
            LOG.severe(detalle);
            throw new IntegracionException(Motivo.SIN_CONFIGURAR, detalle);
        }
        return clientId.trim();
    }
}
