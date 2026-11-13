# ReUseWeb — Frontend Blazor integrado con el backend Java (Lab 11)

Frontend en **C# + Blazor Web App (.NET 10) + Bootstrap 5** del sistema ReUse. Desde el Lab 11 las pantallas de los
requerimientos integrados **consumen los servicios web RESTful** del proyecto Java (`reuse-rest`), con el mismo patrón
de la sesión de clase: `Servicios/Rest/HttpUtils` + un servicio por entidad + DTOs.

## Cómo ejecutarlo

1. Desplegar primero el backend Java (ver `BackEnd/README.md`): `http://localhost:8080/reuse-rest/api/v1` debe responder.
2. Si el backend está en otra URL o puerto, cambiar `RestApi:BaseUrl` en `ReUseWeb/appsettings.json`.
3. Abrir `FrontEnd/ReUseWeb.sln` en Visual Studio y ejecutar con **F5**. Se abre `/login`.
4. Ingresar con un usuario de prueba (contraseña `Demo1234`): `a20260001@pucp.edu.pe` (estudiante) o `admin.demo@pucp.edu.pe` (administrador).

> Si el backend no está levantado, las pantallas muestran «ReUse no está disponible en este momento. Inténtalo de nuevo en unos minutos.».

## Cómo está organizado

```
ReUseWeb/
├── Program.cs                     Registra la seguridad (cookie) y los servicios REST
├── appsettings.json               RestApi:BaseUrl → URL del backend Java
├── DTOs/                          Espejo de los DTO de reuse-rest (mismos nombres de campos, JSON camelCase)
├── Servicios/
│   ├── I*Service.cs               Contratos (IAnuncioService, IOfertaService, …)
│   ├── Rest/HttpUtils.cs          GET/POST/PUT/DELETE; si Java responde error, lanza ServicioException con su mensaje
│   ├── Rest/*Service.cs           Un cliente por recurso REST
│   └── Impl/SesionServiceImpl.cs  Login contra POST /auth/login y creación de la cookie con el rol
├── Seguridad/                     AddSeguridad, endpoints /cuenta/login y /cuenta/logout, roles ESTUDIANTE y ADMINISTRADOR
├── Utiles/Formato.cs              Fechas ISO, montos en soles, textos de enums
├── Datos/DatosDemo.cs             Solo para las pantallas que aún no se integran (fuera de los menús)
└── Components/
    ├── PaginaBase.cs              Usuario de la sesión + manejo común de errores (Ejecutar)
    ├── Routes.razor               AuthorizeRouteView: sin sesión → /login; admin → solo rol ADMINISTRADOR
    ├── Shared/                    AnuncioCard, BadgeEstado, Alertas, Cargando, BarraBusqueda, ModalConfirmacion…
    └── Pages/                     Pantallas (las de Admin exigen el rol ADMINISTRADOR)
```

## Requerimientos integrados (12 de 18 = 67 %)

| RF | Pantalla (ruta) | Servicio C# → endpoint Java |
|---|---|---|
| RF-01 Gestionar usuarios | `/login`, `/registro`, `/perfil`, `/perfil/editar`, `/admin/usuarios`, `/admin/usuarios/{id}` | `SesionServiceImpl` → `POST /auth/login` · `IUsuarioService` → `/usuarios` |
| RF-02 Gestionar anuncios | `/` (catálogo con filtros), `/anuncios/{id}`, `/mis-anuncios`, `/mis-anuncios/nuevo`, `/mis-anuncios/{id}/editar` | `IAnuncioService` → `/anuncios` |
| RF-03 Gestionar citas | `/transacciones/{id}/cita`, `/transacciones/{id}` | `ICitaService` → `/citas` · `ITransaccionService` → `/transacciones/{id}/confirmar-cita` |
| RF-05 Puntos de entrega | `/admin/puntos-entrega` (+ nuevo / editar) | `IPuntoEntregaService` → `/puntos-entrega` |
| RF-06 Categorías | `/admin/categorias` (+ nueva / editar) | `ICategoriaService` → `/categorias` |
| RF-07 Favoritos | corazón del catálogo y del detalle, `/favoritos` | `IFavoritoService` → `/favoritos` |
| RF-08 Chatear | `/mensajes/{id}` (se actualiza cada 5 s) | `IChatService` → `/chats/{id}/mensajes` |
| RF-09 Solicitar chat | «Contactar al vendedor» en `/anuncios/{id}`, `/mensajes` | `IChatService` → `/chats` |
| RF-11 Confirmar transacción | `/transacciones/{id}` | `ITransaccionService` → `/transacciones/{id}/confirmar-entrega` |
| RF-15 Historial | `/transacciones` | `ITransaccionService` → `/transacciones?usuario=` |
| RF-16 Respuestas rápidas | `/respuestas-rapidas`, chips del chat | `IRespuestaRapidaService` → `/respuestas-rapidas` |
| RF-17 Ofertas | `/anuncios/{id}/ofertar`, `/mis-ofertas`, `/mis-anuncios/{id}/ofertas` | `IOfertaService` → `/ofertas` |

La reputación se muestra separada como vendedor y como comprador (perfil, detalle del anuncio y lista de usuarios del admin).
El login y el registro incluyen el botón **Continuar con Google** (ver «Ingreso con Google» más abajo).

## Ingreso con Google (cuenta @pucp.edu.pe)

Un solo botón sirve para ingresar y para registrarse:

1. `POST /cuenta/google` lleva a Google (OpenID Connect, con `hd=pucp.edu.pe` para mostrar primero las cuentas PUCP).
2. Al volver, Blazor envía el **ID token** a `POST /auth/google` del backend. Java valida la firma con las claves de
   Google, el Client ID, el vencimiento, que el correo esté verificado y que la cuenta sea del dominio `pucp.edu.pe`
   (claim `hd`). Una cuenta Google personal o de otro dominio se rechaza con un mensaje en `/login`.
3. Si el correo ya tiene cuenta, se abre la sesión (si estaba `PENDIENTE_VERIFICACION`, queda verificada).
   Si no, se abre la **misma pantalla de registro** (`/registro/google`) con nombres, apellidos, código PUCP
   (tomado del correo `aXXXXXXXX@pucp.edu.pe`) y correo ya rellenados; el estudiante elige facultad, carrera y
   contraseña. Se crea el mismo usuario que con el formulario, pero **ACTIVA y verificada**, sin revisión del
   administrador (`POST /auth/google/registro`).

**Configuración** (una vez por equipo, en [Google Cloud Console](https://console.cloud.google.com/apis/credentials)):

1. Configurar la **pantalla de consentimiento de OAuth** como **Externa** (un proyecto personal no puede ser
   «Interno» de la PUCP), con los permisos `openid`, `email` y `profile`. Mientras esté en modo **Prueba**, agregar
   en *Usuarios de prueba* los correos @pucp.edu.pe que van a ingresar (los del equipo y el del profesor para la demo);
   cualquier otro correo verá «Acceso bloqueado».
2. Crear un *ID de cliente de OAuth* de tipo **Aplicación web** con URIs de redirección autorizados
   `http://localhost:5180/signin-google` y `https://localhost:7180/signin-google` (exactos, sin barra final).
3. Client ID en `ReUseWeb/appsettings.json` → `Google:ClientId` **y** en
   `BackEnd/reuse-negocio/src/main/resources/google.properties` → `clientId` (deben ser el mismo).
   Después de cambiar `google.properties` hay que recompilar y volver a desplegar `reuse-rest`.
4. Client Secret **solo** con user-secrets (no se sube al repositorio), dentro de `FrontEnd/ReUseWeb`:
   `dotnet user-secrets set "Google:ClientSecret" "<secreto>"`. Cada integrante que corra el frontend lo configura
   en su máquina.

Google vuelve a `/signin-google` con una redirección GET (`response_mode=query`), así el ingreso funciona también
con el perfil http sin errores de cookies («Correlation failed»).

Sin esa configuración el botón se muestra igual: el usuario ve «El ingreso con Google no está disponible por ahora»
y el motivo exacto (qué clave falta) queda en el log de Blazor o en el `server.log` de GlassFish.

El resumen del administrador (`/admin`) muestra totales reales: usuarios activos, cuentas por verificar y suspendidas,
vendedores con anuncios, anuncios disponibles por categoría, categorías y puntos activos.

## Recorrido sugerido para la demo

1. **Ana** (`a20260001`) busca «reglas» en el catálogo, la guarda en favoritos, contacta al vendedor y ofrece S/ 15.
2. **Diego** (`a20260004`) acepta la solicitud de chat, responde con una respuesta rápida y contraoferta S/ 16.
3. **Ana** acepta la contraoferta en *Mis ofertas* → se abre la transacción → propone una cita.
4. **Diego** confirma la cita (el anuncio pasa a RESERVADO) y confirma la entrega; **Ana** confirma → COMPLETADA, anuncio VENDIDO.
5. **Admin** verifica a Elena (`a20260005`); Elena ya puede iniciar sesión.

Cada regla que el backend rechaza (por ejemplo, ofertar por tu propio anuncio o eliminar una categoría en uso) aparece en rojo
en la pantalla con el mensaje exacto que devuelve la capa de negocio.
