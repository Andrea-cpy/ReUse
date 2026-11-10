# ReUseWeb — Frontend Blazor integrado con el backend Java (Lab 11)

Frontend en **C# + Blazor Web App (.NET 10) + Bootstrap 5** del sistema ReUse. Desde el Lab 11 las pantallas de los
requerimientos integrados **consumen los servicios web RESTful** del proyecto Java (`reuse-rest`), con el mismo patrón
de la sesión de clase: `Servicios/Rest/HttpUtils` + un servicio por entidad + DTOs.

## Cómo ejecutarlo

1. Desplegar primero el backend Java (ver `BackEnd/README.md`): `http://localhost:8080/reuse-rest/api/v1` debe responder.
2. Si el backend está en otra URL o puerto, cambiar `RestApi:BaseUrl` en `ReUseWeb/appsettings.json`.
3. Abrir `FrontEnd/ReUseWeb.sln` en Visual Studio y ejecutar con **F5**. Se abre `/login`.
4. Ingresar con un usuario de prueba (contraseña `Demo1234`): `a20260001@pucp.edu.pe` (estudiante) o `admin.demo@pucp.edu.pe` (administrador).

> Si el backend no está levantado, las pantallas muestran «No se pudo conectar con el servidor de ReUse».

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
El login incluye el botón **Iniciar sesión con Google** (solo visual: es la propuesta para el flujo de verificación con la cuenta @pucp.edu.pe).

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
