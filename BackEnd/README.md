# ReUse — Backend Java (Lab 12: 18 de 18 RF integrados)

Marketplace de materiales académicos entre estudiantes PUCP. Equipo **Erasmolovers** · Programación 3 (1INF30).

## Módulos (Maven multi-módulo)

| Módulo | Capa | Qué contiene |
|---|---|---|
| `reuse-model` | Dominio | Entidades y enums (`UsuarioPUCP`, `Anuncio`, `Oferta`, `Transaccion`, …) |
| `reuse-dbmanager` | Conexión | `DBManager` (singleton, lee `db.properties`) |
| `reuse-accesodatos` | Datos | DAOs que llaman a los procedimientos almacenados |
| `reuse-negocio` | Negocio | BL con todas las reglas (`BLException`, BCrypt, transacciones) |
| `reuse-main` | Pruebas | `Principal`: pruebas de consola de la BL |
| **`reuse-rest`** (nuevo) | **Servicios web** | API RESTful con Jakarta REST (war) que consume el frontend Blazor |

`reuse-rest` sigue el patrón de la sesión de clase: por entidad hay un **DTO** (`rest/dto`), un **Mapper** modelo ↔ DTO (`rest/mapper`) y un **Resource** (`rest/*Resource.java`). Los errores de negocio se convierten en JSON en un solo lugar (`rest/error/BLExceptionMapper`): regla incumplida → **400**, falla de base de datos → **500**, siempre como `{"error": "mensaje", "idSolicitud": "…"}` (el id se registra en el log del servidor: RNF-06).

## Cómo desplegar

1. **Base de datos**: ejecutar completo `ScriptSQL/reuse_schema.sql` (en la raíz del repo; crea tablas, procedimientos y datos de prueba).
2. **Conexión**: copiar `reuse-dbmanager/src/main/resources/db.properties.example` como `db.properties` en la misma carpeta y completar sus datos (ese archivo está en `.gitignore`).
3. **Compilar**: en IntelliJ IDEA, abrir la carpeta `BackEnd` y hacer *Clean and Build* del proyecto padre `ReUse` (o `mvn clean install` dentro de `BackEnd`). Se genera `reuse-rest/target/reuse-rest.war`.
4. **Desplegar**: usar el GlassFish 8.0.5 del laboratorio que está en la carpeta `glassfish8` del repo. En NetBeans: *Services → Servers → Add Server → GlassFish* apuntando a esa carpeta, y luego *Run* sobre `reuse-rest`. También se puede copiar el war a `glassfish8/glassfish/domains/domain1/autodeploy`. Su `domain.xml` espera el JDK en `C:\Program Files\Java\jdk-25.0.4`; si su JDK está en otra ruta, cambien `java-home` en `glassfish8/glassfish/domains/domain1/config/domain.xml`.
5. **Probar**: abrir `http://localhost:8080/reuse-rest/api/v1/categorias` → debe devolver un JSON con las categorías.

URL base: **`http://localhost:8080/reuse-rest/api/v1`** (es la que usa el frontend en `appsettings.json` → `RestApi:BaseUrl`).

## Usuarios de prueba

Todos usan la contraseña **`Demo1234`** (en la base solo se guarda su hash BCrypt).

| Correo | Nombre | Estado | Para probar |
|---|---|---|---|
| `a20260001@pucp.edu.pe` | Ana Torres | ACTIVA | Compradora / vendedora |
| `a20260002@pucp.edu.pe` | Bruno Ramos | ACTIVA | Comprador |
| `a20260003@pucp.edu.pe` | Carla Lopez | ACTIVA | Compradora |
| `a20260004@pucp.edu.pe` | Diego Mendoza | ACTIVA | Vendedor (tiene anuncios DISPONIBLE) |
| `a20260005@pucp.edu.pe` | Elena Castro | PENDIENTE_VERIFICACION | El login se rechaza hasta que un admin la verifique |
| `a20260006@pucp.edu.pe` | Fabian Leon | SUSPENDIDA | El login se rechaza |
| `admin.demo@pucp.edu.pe` | Admin Prueba | ACTIVA + administrador | Panel de administración |

## Endpoints

| Requerimiento | Método y ruta | Regla principal en la BL |
|---|---|---|
| RF-01 Login | `POST /auth/login` | BCrypt; cuenta ACTIVA; devuelve `administrador` |
| RF-01 Usuarios | `GET /usuarios`, `GET /usuarios/{id}`, `POST /usuarios`, `PUT /usuarios/{id}`, `PUT /usuarios/{id}/contrasena` | Código de 8 dígitos, correo @pucp.edu.pe, sin duplicados, contraseña ≥ 8 |
| RF-01 Estado de cuenta | `PUT /usuarios/{id}/verificar` · `/suspender` · `/reactivar` | Transiciones válidas de `EstadoCuenta` |
| Listas | `GET /facultades`, `GET /facultades/{id}/carreras`, `GET /materiales` | — |
| RF-02 Anuncios | `GET /anuncios?texto=&categoria=&condicion=&precioMin=&precioMax=&excluirUsuario=` · `GET /anuncios?vendedor=` · `GET/POST/PUT/DELETE /anuncios[/{id}]` · `PUT /anuncios/{id}/archivar?vendedor=` · `/republicar?vendedor=` | Precio > 0, material existente, no borrar con transacciones, solo el vendedor cambia el estado |
| RF-05 Puntos de entrega | `GET/POST/PUT/DELETE /puntos-entrega[/{id}]` | No eliminar con citas pendientes |
| RF-06 Categorías | `GET/POST/PUT/DELETE /categorias[/{id}]` | No eliminar con materiales asociados |
| RF-07 Favoritos | `GET /favoritos?usuario=`, `POST /favoritos`, `DELETE /favoritos/{id}` | Sin duplicados; no el propio anuncio |
| RF-09 Solicitar chat | `POST /chats`, `PUT /chats/{id}/aceptar` · `/rechazar` · `/cerrar`, `GET /chats?usuario=`, `GET /chats/{id}` | Anuncio DISPONIBLE, sin bloqueo, una solicitud abierta por anuncio |
| RF-08 Chatear | `GET /chats/{id}/mensajes`, `POST /chats/{id}/mensajes` | Chat ACTIVO y emisor participante |
| RF-16 Respuestas rápidas | `GET /respuestas-rapidas?creador=`, `POST`, `PUT /{id}`, `DELETE /{id}`, `POST /chats/{id}/respuestas-rapidas/{idRespuesta}?emisor=` | Solo respuestas propias |
| RF-17 Ofertas | `GET /ofertas?comprador=`, `GET /anuncios/{id}/ofertas`, `POST /ofertas`, `DELETE /ofertas/{id}`, `PUT /ofertas/{id}/aceptar` · `/rechazar` · `/contraoferta` · `/contraoferta/aceptar` · `/contraoferta/rechazar` | Aceptar abre la transacción y rechaza las demás pendientes; contraoferta > monto ofrecido y ≤ precio |
| RF-15 Historial | `GET /transacciones?usuario=`, `GET /transacciones/{id}` | Compras y ventas del usuario |
| RF-03 Citas | `POST /citas`, `PUT /citas/{id}`, `DELETE /citas/{id}`, `GET /transacciones/{id}/cita`, `PUT /transacciones/{id}/confirmar-cita` | Fecha futura, punto activo; al confirmar, el anuncio pasa a RESERVADO |
| RF-11 Confirmar entrega | `PUT /transacciones/{id}/confirmar-entrega?usuario=`, `PUT /transacciones/{id}/cancelar` | Cada parte confirma una vez; con ambas, COMPLETADA y anuncio VENDIDO (con rollback) |

Las fechas viajan como texto ISO (`2026-11-12T12:30`) y los enums como su nombre (`DISPONIBLE`).

### Endpoints agregados en el Lab 12

| Requerimiento | Método y ruta | Regla principal en la BL |
|---|---|---|
| RF-01 Recuperar contraseña | `POST /auth/recuperar` `{correo}` · `POST /auth/restablecer` `{correo, codigo, contrasenaNueva}` | Código de 6 dígitos (hash BCrypt), 15 min, 5 intentos, un solo uso. Sin servidor de correo, la respuesta trae `codigoDemo` (`AuthResource.ENTREGAR_CODIGO_EN_RESPUESTA`) |
| RF-04 Denuncias | `GET /denuncias?tipo=&estado=&denunciante=`, `GET /denuncias/{id}`, `POST /denuncias/usuarios`, `POST /denuncias/anuncios`, `PUT /denuncias/{id}/sancionar?administrador=` · `/desestimar?administrador=` | Solo un administrador activo resuelve; solo reportes PENDIENTE; se notifica al denunciante |
| RF-04 Reportes PDF (JasperReports) | `GET /reportes/incidentes?estado=&tipo=`, `GET /reportes/transacciones?estado=`, `GET /reportes/usuarios` (+ `&administrador=ID`) | `application/pdf` con `Content-Disposition: attachment`. Ver `src/main/resources/reports/LEEME.md` |
| RF-10 Bloqueos | `GET /bloqueos?usuario=`, `GET /bloqueos/entre?usuarioA=&usuarioB=`, `POST /bloqueos`, `PUT /bloqueos/{id}/desbloquear` | Cierra los chats con `bloquear_chats_entre` en la misma transacción; oculta los anuncios en ambos sentidos y bloquea chats y ofertas |
| RF-12 Recordatorios | `GET /notificaciones?usuario=`, `PUT /notificaciones/{id}/leer`, `PUT /notificaciones/leer-todas?usuario=`, `GET /notificaciones/recordatorios?usuario=&horas=24` | Citas CONFIRMADAS en las próximas horas (1–168); deja un RECORDATORIO_CITA por cita, sin duplicar |
| RF-13 Reincidentes | `PUT /denuncias/{id}/sancionar` (campo `cuentaSuspendida`), `GET /usuarios/{id}/sanciones` | Con 4 sanciones en 90 días (`contar_sanciones_recientes`) la cuenta pasa a SUSPENDIDA en la misma transacción |
| RF-14 Moderación de anuncios | `GET /anuncios?todos=true`, `PUT /anuncios/{id}/moderar?accion=OBSERVAR\|HABILITAR\|ARCHIVAR` | Denunciar deja el anuncio OBSERVADO; sancionar lo ARCHIVA (borrado lógico); desestimar lo vuelve DISPONIBLE |
| RF-15 Calificar desde el historial | `GET /calificaciones?calificado=` · `?calificador=`, `POST /calificaciones` | Solo transacciones COMPLETADAS, 1–5, una vez por parte; recalcula la reputación (`recalcular_reputacion`) |
| RF-18 Reputación e insignias | `GET /usuarios/{id}/reputacion`, `GET /usuarios/{id}/insignias`, `POST /usuarios/{id}/insignias/evaluar`, `GET/POST/PUT/DELETE /insignias[/{id}]` | Patrón *Specification* (`bl/insignias`): cada insignia es la conjunción de sus reglas; se evalúa sola al calificar y al completar una venta |

## Reputación separada (observación del Lab 9)

La tabla `usuario` ya no tiene una sola columna `reputacion`: ahora tiene `reputacion_vendedor` (promedio de las
calificaciones `COMPRADOR_A_VENDEDOR` recibidas) y `reputacion_comprador` (promedio de las `VENDEDOR_A_COMPRADOR`).
El procedimiento `recalcular_reputacion` actualiza las dos, y viajan así hasta Java (`UsuarioPUCP`), el DTO y Blazor.
Hay que volver a ejecutar el script completo para tener el cambio.

## Cambios en las capas existentes (Lab 11)

- `AnuncioBL`: `listarPorVendedor`, `buscar` (filtros del catálogo), `archivar`, `republicar`.
- `POST /anuncios` acepta las fotos como URLs en `imagenes`; `rest/ImagenesRemotas` descarga cada una para registrar su peso y formato reales, y la BL las guarda junto con el anuncio en una sola transacción (máx. 5, hasta 5 MB, jpg/png/webp).
- `UsuarioBL.iniciarSesion` rechaza también las cuentas `PENDIENTE_VERIFICACION` (antes solo las `SUSPENDIDA`).
- `OfertaBL`: `listarPorComprador`, `contraofertar`, `responderContraoferta`.
- `TransaccionBL.listarPorUsuario` y `CanalChatBL.listarPorUsuario` (como comprador o vendedor).
- `DBManager.getConnection` carga el driver de MySQL de forma explícita (necesario dentro de GlassFish).
