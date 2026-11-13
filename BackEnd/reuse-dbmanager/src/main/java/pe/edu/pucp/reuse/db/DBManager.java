package pe.edu.pucp.reuse.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Punto unico de acceso a la base de datos. Administra un pool de conexiones
 * (HikariCP): abrir una conexion SSL nueva contra AWS en cada consulta toma
 * alrededor de un segundo, mientras que tomar una del pool es inmediato.
 * <p>
 * getConnection() entrega una conexion del pool. Al llamar a close() (en los DAO
 * o en TransactionsManager) la conexion vuelve al pool en lugar de cerrarse, asi
 * que el codigo que ya cerraba sus conexiones no cambia.
 * <p>
 * Ademas de servidor, puerto, esquema, usuario y password, db.properties admite
 * claves opcionales para el pool (ver db.properties.example); si faltan se usan
 * los valores por defecto definidos aqui.
 */
public class DBManager {
    private static final int POOL_MAXIMO = 10;
    private static final int POOL_MINIMO_INACTIVAS = 3;
    private static final long POOL_TIEMPO_INACTIVA_MS = 600_000;   // 10 min
    private static final long POOL_TIEMPO_ESPERA_MS = 30_000;      // 30 s

    protected String servidor;
    protected int puerto;
    protected String esquema;
    protected String usuario;
    protected String password;

    private HikariDataSource dataSource;

    private static DBManager instance;

    protected DBManager(){
    }

    protected DBManager(String servidor, int puerto, String esquema, String usuario, String password){
        this.servidor = servidor;
        this.puerto = puerto;
        this.esquema = esquema;
        this.usuario = usuario;
        this.password = password;
    }

    public synchronized static DBManager getInstance (){
        if(instance == null){
            ResourceBundle rb = ResourceBundle.getBundle("db");
            DBManager nuevo = new DBManager(
                    rb.getString("servidor"),
                    Integer.parseInt(rb.getString("puerto")),
                    rb.getString("esquema"),
                    rb.getString("usuario"),
                    rb.getString("password")
            );
            nuevo.iniciarPool(rb);
            instance = nuevo;
        }
        return instance;
    }

    private void iniciarPool(ResourceBundle rb) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("ReUsePool");
        // En GlassFish el driver vive en WEB-INF/lib y no siempre se autodescubre: se indica explicitamente.
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // sslMode=REQUIRED: la conexion siempre va cifrada, sin validar el certificado del servidor.
        // GlassFish define javax.net.ssl.keyStore/trustStore con su propio almacen (keystore.p12) sin
        // exponer la contrasena; si el driver los usa, la conexion SSL con la BD falla
        // ("keystore password was incorrect"). Por eso se desactiva ese uso.
        config.setJdbcUrl("jdbc:mysql://" + servidor + ":" + puerto + "/" + esquema
                + "?sslMode=REQUIRED&fallbackToSystemKeyStore=false&fallbackToSystemTrustStore=false");
        config.setUsername(usuario);
        config.setPassword(password);
        config.setMaximumPoolSize(leerEntero(rb, "pool.maximo", POOL_MAXIMO));
        config.setMinimumIdle(leerEntero(rb, "pool.minimoInactivas", POOL_MINIMO_INACTIVAS));
        config.setIdleTimeout(leerLargo(rb, "pool.tiempoInactivaMs", POOL_TIEMPO_INACTIVA_MS));
        config.setConnectionTimeout(leerLargo(rb, "pool.tiempoEsperaMs", POOL_TIEMPO_ESPERA_MS));
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException{
        return dataSource.getConnection();
    }

    /**
     * Cierra el pool y sus conexiones fisicas. Lo invoca reuse-rest al replegar la
     * aplicacion, para que un redespliegue no deje conexiones e hilos huerfanos.
     */
    public synchronized static void cerrar() {
        if (instance != null) {
            instance.dataSource.close();
            instance = null;
        }
    }

    private static int leerEntero(ResourceBundle rb, String clave, int porDefecto) {
        return rb.containsKey(clave) ? Integer.parseInt(rb.getString(clave).trim()) : porDefecto;
    }

    private static long leerLargo(ResourceBundle rb, String clave, long porDefecto) {
        return rb.containsKey(clave) ? Long.parseLong(rb.getString(clave).trim()) : porDefecto;
    }
}
