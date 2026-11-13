package pe.edu.pucp.reuse.integraciones;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Conexiones TLS hacia servicios externos con las autoridades raiz del JDK.
 * <p>
 * Dentro de GlassFish, javax.net.ssl.trustStore apunta al almacen del dominio (cacerts.p12),
 * que no trae las autoridades de Google (googleapis.com, smtp.gmail.com) y la conexion falla
 * con "PKIX path building failed". Por eso se usa java.home/lib/security/cacerts.
 */
public final class CertificadosJdk {
    private static volatile SSLSocketFactory fabrica;

    private CertificadosJdk() {
    }

    /** SSLSocketFactory que confia en las autoridades del JDK. Se crea una sola vez. */
    public static SSLSocketFactory fabricaSsl() {
        if (fabrica == null) {
            synchronized (CertificadosJdk.class) {
                if (fabrica == null) {
                    fabrica = crearFabrica();
                }
            }
        }
        return fabrica;
    }

    // El almacen se lee sin contrasena: solo contiene certificados publicos.
    private static SSLSocketFactory crearFabrica() {
        Path cacerts = Path.of(System.getProperty("java.home"), "lib", "security", "cacerts");
        try {
            KeyStore almacen = KeyStore.getInstance(cacerts.toFile(), (char[]) null);
            TrustManagerFactory confianza = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            confianza.init(almacen);
            SSLContext contexto = SSLContext.getInstance("TLS");
            contexto.init(null, confianza.getTrustManagers(), null);
            return contexto.getSocketFactory();
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cargar los certificados del JDK desde " + cacerts, e);
        }
    }
}
