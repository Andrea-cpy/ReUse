package pe.edu.pucp.reuse.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** Todos los servicios quedan bajo http://servidor:puerto/reuse-rest/api/v1/... */
@ApplicationPath("/api/v1")
public class ReUseApplication extends Application {

}
