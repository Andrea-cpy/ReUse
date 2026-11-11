# ReUse — Backend Java (Lab 11: servicios web RESTful)

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
3. **Compilar**: en NetBeans, abrir la carpeta `BackEnd` y hacer *Clean and Build* del proyecto padre `ReUse` (o `mvn clean install` dentro de `BackEnd`). Se genera `reuse-rest/target/reuse-rest.war`.
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
