using ReUseWeb.Components;
using ReUseWeb.Seguridad;
using ReUseWeb.Servicios;
using ReUseWeb.Servicios.Rest;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

// Sesión con cookie: el login se valida contra el backend Java (POST /auth/login).
// Ingreso con Google (OpenID Connect) si hay Google:ClientId y Google:ClientSecret configurados.
builder.Services.AddSeguridad(builder.Configuration);

// Clientes de los servicios web RESTful del backend Java (URL base en appsettings.json → RestApi:BaseUrl)
builder.Services.AddScoped<IUsuarioService, UsuarioService>();
builder.Services.AddScoped<IAcademicoService, AcademicoService>();
builder.Services.AddScoped<ICategoriaService, CategoriaService>();
builder.Services.AddScoped<IPuntoEntregaService, PuntoEntregaService>();
builder.Services.AddScoped<IAnuncioService, AnuncioService>();
builder.Services.AddScoped<IFavoritoService, FavoritoService>();
builder.Services.AddScoped<IOfertaService, OfertaService>();
builder.Services.AddScoped<ITransaccionService, TransaccionService>();
builder.Services.AddScoped<ICitaService, CitaService>();
builder.Services.AddScoped<IChatService, ChatService>();
builder.Services.AddScoped<IRespuestaRapidaService, RespuestaRapidaService>();
// Lab 12: moderación (RF-04, 13, 14), bloqueos (RF-10), recordatorios (RF-12), calificaciones e insignias (RF-15, 18)
builder.Services.AddScoped<IDenunciaService, DenunciaService>();
builder.Services.AddScoped<IReportePdfService, ReportePdfService>();
builder.Services.AddScoped<IBloqueoService, BloqueoService>();
builder.Services.AddScoped<INotificacionService, NotificacionService>();
builder.Services.AddScoped<ICalificacionService, CalificacionService>();
builder.Services.AddScoped<IInsigniaService, InsigniaService>();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
    app.UseHsts();
}

app.UseStatusCodePagesWithReExecute("/not-found", createScopeForStatusCodePages: true);
app.UseHttpsRedirection();

app.UseAuthentication();
app.UseAuthorization();
app.UseAntiforgery();

app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.MapSesionEndpoints();
app.MapDescargasEndpoints();

app.Run();
