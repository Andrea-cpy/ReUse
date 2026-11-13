# Reportes PDF (RF-04) — JasperReports 7.0.6

| Plantilla | Endpoint | Contenido |
|---|---|---|
| `ReporteIncidentes.jrxml` | `GET /reportes/incidentes?estado=&tipo=&administrador=ID` | Denuncias de usuarios y anuncios, su estado y revisor; totales por estado |
| `ReporteTransacciones.jrxml` | `GET /reportes/transacciones?estado=&administrador=ID` | Historial de transacciones, montos y total vendido |
| `ReporteUsuarios.jrxml` | `GET /reportes/usuarios?administrador=ID` | Reputación separada, reportes recibidos y sanciones en 90 días (RF-13) |

Ciclo de la Sesión 25:

1. **Diseño**: los `.jrxml` de esta carpeta (formato de JasperReports 7; se abren en Jaspersoft Studio 7).
2. **Compilación**: se precompilan a `.jasper` con `pe.edu.pucp.reuse.rest.reportes.CompilarReportes`
   (en el IDE: *Run* sobre su `main`, con el módulo `reuse-rest` como directorio de trabajo).
   **Cada vez que se modifique un `.jrxml` hay que volver a ejecutarlo** y subir el `.jasper` nuevo.
3. **Llenado**: `ReporteService` (singleton con caché del `JasperReport`) usa `JRBeanCollectionDataSource`
   con listas de `FilaIncidente`, `FilaTransaccion` y `FilaUsuario`.
4. **Exportación**: `JRPdfExporter` con metadatos (título, autor, asunto); el recurso responde
   `application/pdf` con `Content-Disposition: attachment`.

La fuente es *DejaVu Sans* (la trae `jasperreports-fonts`), así se ven bien las tildes y la ñ.
