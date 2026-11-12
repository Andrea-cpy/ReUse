namespace ReUseWeb.Datos;

// =====================================================================
// DATOS DE EJEMPLO — SOLO PARA LAS PANTALLAS QUE AÚN NO SE INTEGRAN
//
// Desde el Lab 12, las pantallas de los 18 requerimientos funcionales
// consumen los servicios REST del backend Java (carpeta Servicios/Rest).
// Solo las pantallas de administración de materiales y facultades
// (fuera del catálogo de RF y fuera de los menús) siguen usando esta clase.
// =====================================================================

public record AnuncioDemo(int Id, string Titulo, string Material, string Categoria, decimal Precio,
    string Condicion, string Estado, string Descripcion, int IdVendedor, string Vendedor,
    string Carrera, string Fecha, int Ofertas);

public record OfertaDemo(int Id, int IdAnuncio, string Anuncio, string Comprador, string Vendedor,
    decimal PrecioPublicado, decimal Monto, string Estado, string Fecha);

public record TransaccionDemo(int Id, int IdAnuncio, string Anuncio, string Rol, string Contraparte,
    int IdContraparte, decimal Monto, string Estado, string FechaInicio, string? Cita,
    string? PuntoEntrega, string EstadoCita, bool ConfirmoComprador, bool ConfirmoVendedor);

public record ChatDemo(int Id, int IdAnuncio, string Anuncio, string Contraparte, string UltimoMensaje,
    string Hora, string Estado, int NoLeidos, bool SoyVendedor);

public record MensajeDemo(bool Enviado, string Texto, string Hora);

public record NotificacionDemo(int Id, string Tipo, string Mensaje, string Fecha, bool Leida, string Enlace);

public record UsuarioDemo(int Id, string Codigo, string Nombre, string Correo, string Facultad, string Carrera,
    string Estado, double Reputacion, string Nivel, int Reportes, int Ventas, int Compras, string FechaRegistro);

public record CalificacionDemo(string Autor, int Puntaje, string Comentario, string Fecha, string Tipo);

public record InsigniaDemo(int Id, string Nombre, string Descripcion, string Icono, string Tipo, bool Activa,
    string Regla, bool Obtenida, string? FechaObtencion);

public record ReporteDemo(int Id, string Tipo, string Motivo, string Objetivo, int IdObjetivo,
    string Denunciante, string Descripcion, string Fecha, string Estado, string? Revisor);

public record CategoriaDemo(int Id, string Nombre, string Descripcion, bool Activa, int Materiales);

public record MaterialDemo(int Id, string Titulo, string Categoria, string[] Carreras);

public record PuntoEntregaDemo(int Id, string Nombre, string Referencia, string Ubicacion, bool Activo);

public record FacultadDemo(int Id, string Nombre, bool Activa, string[] Carreras);

public record RespuestaRapidaDemo(int Id, string Texto);

public record BloqueoDemo(int Id, string Usuario, string Codigo, string Fecha);

public static class DatosDemo
{
    // Usuario que "inició sesión" en la demo
    public static readonly UsuarioDemo UsuarioActual = new(1, "20221234", "Andrea Díaz Torres",
        "a20221234@pucp.edu.pe", "Ciencias e Ingeniería", "Ingeniería Informática",
        "ACTIVA", 4.7, "CONFIABLE", 0, 6, 3, "02/08/2026");

    public static readonly string[] Condiciones = { "NUEVO", "USADO", "REACONDICIONADO" };

    public static readonly List<CategoriaDemo> Categorias = new()
    {
        new(1, "Libro", "Libros y textos académicos.", true, 3),
        new(2, "Útiles", "Útiles de estudio.", true, 1),
        new(3, "Maqueta", "Maquetas y modelos académicos.", true, 1),
        new(4, "Calculadora", "Calculadoras para cursos.", true, 1),
        new(5, "Equipo de estudio", "Equipamiento académico reutilizable.", true, 1),
        new(6, "Otro", "Otros materiales académicos.", true, 1),
        new(7, "Categoría de prueba", "No disponible para nuevas publicaciones.", false, 0),
    };

    public static readonly List<MaterialDemo> Materiales = new()
    {
        new(1, "Cálculo de una variable", "Libro", new[] { "Ingeniería Informática", "Ingeniería Civil" }),
        new(2, "Calculadora científica", "Calculadora", new[] { "Ingeniería Industrial", "Economía" }),
        new(3, "Set de reglas y escuadras", "Útiles", new[] { "Arquitectura", "Diseño Industrial" }),
        new(4, "Maqueta de vivienda", "Maqueta", new[] { "Arquitectura" }),
        new(5, "Lámpara de escritorio", "Equipo de estudio", Array.Empty<string>()),
        new(6, "Introducción al derecho", "Libro", new[] { "Derecho" }),
        new(7, "Soporte para libros", "Otro", Array.Empty<string>()),
        new(8, "Física I — Serway", "Libro", new[] { "Ingeniería Mecánica", "Ingeniería Informática" }),
    };

    public static readonly List<FacultadDemo> Facultades = new()
    {
        new(1, "Arquitectura y Urbanismo", true, new[] { "Arquitectura" }),
        new(2, "Arte y Diseño", true, new[] { "Diseño Gráfico", "Diseño Industrial", "Pintura" }),
        new(3, "Ciencias e Ingeniería", true, new[] { "Ingeniería Informática", "Ingeniería Civil", "Ingeniería Industrial", "Ingeniería Mecánica" }),
        new(4, "Ciencias Sociales", true, new[] { "Economía", "Sociología", "Antropología" }),
        new(5, "Derecho", true, new[] { "Derecho" }),
        new(6, "Gestión y Alta Dirección", true, new[] { "Gestión Empresarial", "Gestión Pública" }),
    };

    public static readonly List<AnuncioDemo> Anuncios = new()
    {
        new(1, "Cálculo de una variable — Stewart 8va ed.", "Cálculo de una variable", "Libro", 45.50m, "USADO", "DISPONIBLE",
            "Texto completo con algunas anotaciones a lápiz. Ideal para Cálculo 1 y 2.", 2, "Diego Ramos Cárdenas", "Ingeniería Civil", "20/10/2026", 2),
        new(2, "Calculadora científica Casio fx-991", "Calculadora científica", "Calculadora", 65.00m, "USADO", "RESERVADO",
            "Funciona correctamente, incluye tapa protectora. Permitida en exámenes.", 1, "Andrea Díaz Torres", "Ingeniería Informática", "18/10/2026", 3),
        new(3, "Set de reglas y escuadras para dibujo", "Set de reglas y escuadras", "Útiles", 18.00m, "NUEVO", "DISPONIBLE",
            "Set nuevo, sin uso. Incluye escalímetro, escuadras de 45° y 60°.", 4, "Lucía Paredes Soto", "Arquitectura", "17/10/2026", 1),
        new(4, "Maqueta de vivienda unifamiliar", "Maqueta de vivienda", "Maqueta", 80.00m, "USADO", "OBSERVADO",
            "Modelo de vivienda en escala 1:50, útil como referencia para Taller 2.", 4, "Lucía Paredes Soto", "Arquitectura", "15/10/2026", 0),
        new(5, "Lámpara LED de escritorio", "Lámpara de escritorio", "Equipo de estudio", 30.00m, "REACONDICIONADO", "DISPONIBLE",
            "Cable reemplazado, tres niveles de intensidad. Perfecta para estudiar de noche.", 1, "Andrea Díaz Torres", "Ingeniería Informática", "12/10/2026", 1),
        new(6, "Introducción al derecho — Rubio Correa", "Introducción al derecho", "Libro", 25.00m, "USADO", "VENDIDO",
            "Libro bien conservado, sin hojas faltantes.", 3, "Mateo Quispe Huamán", "Derecho", "08/10/2026", 4),
        new(7, "Soporte plegable para libros", "Soporte para libros", "Otro", 15.00m, "NUEVO", "DISPONIBLE",
            "Soporte de bambú regulable en cinco posiciones.", 5, "Valeria Chávez León", "Economía", "05/10/2026", 0),
        new(8, "Física I — Serway 10ma ed.", "Física I — Serway", "Libro", 55.00m, "USADO", "DISPONIBLE",
            "Incluye solucionario impreso de los capítulos 1 a 8.", 1, "Andrea Díaz Torres", "Ingeniería Informática", "02/10/2026", 2),
        new(9, "Química General — Chang 12va ed.", "Química General", "Libro", 38.00m, "USADO", "DISPONIBLE",
            "Algunas páginas subrayadas con resaltador. Tapas en buen estado.", 3, "Mateo Quispe Huamán", "Ingeniería Industrial", "24/10/2026", 1),
        new(10, "Calculadora gráfica TI-84 Plus", "Calculadora gráfica", "Calculadora", 220.00m, "REACONDICIONADO", "DISPONIBLE",
            "Pantalla y botones revisados. Incluye cable USB y estuche.", 2, "Diego Ramos Cárdenas", "Ingeniería Civil", "23/10/2026", 0),
        new(11, "Kit de rotuladores para diseño (24)", "Kit de rotuladores", "Útiles", 35.00m, "NUEVO", "DISPONIBLE",
            "Rotuladores de doble punta, sellados.", 5, "Valeria Chávez León", "Diseño Gráfico", "21/10/2026", 0),
        new(12, "Atril de dibujo de mesa", "Atril de dibujo", "Equipo de estudio", 60.00m, "USADO", "DISPONIBLE",
            "Atril de madera con inclinación regulable, tamaño A3.", 4, "Lucía Paredes Soto", "Arquitectura", "19/10/2026", 2),
    };

    public static readonly List<OfertaDemo> OfertasRecibidas = new()
    {
        new(11, 8, "Física I — Serway 10ma ed.", "Mateo Quispe Huamán", "Andrea Díaz Torres", 55.00m, 48.00m, "PENDIENTE", "26/10/2026 18:20"),
        new(12, 8, "Física I — Serway 10ma ed.", "Valeria Chávez León", "Andrea Díaz Torres", 55.00m, 50.00m, "PENDIENTE", "26/10/2026 20:05"),
        new(13, 5, "Lámpara LED de escritorio", "Diego Ramos Cárdenas", "Andrea Díaz Torres", 30.00m, 25.00m, "RECHAZADA", "22/10/2026 10:15"),
        new(14, 2, "Calculadora científica Casio fx-991", "Lucía Paredes Soto", "Andrea Díaz Torres", 65.00m, 60.00m, "ACEPTADA", "19/10/2026 09:40"),
    };

    public static readonly List<OfertaDemo> MisOfertas = new()
    {
        new(21, 1, "Cálculo de una variable — Stewart 8va ed.", "Andrea Díaz Torres", "Diego Ramos Cárdenas", 45.50m, 40.00m, "PENDIENTE", "25/10/2026 16:00"),
        new(22, 3, "Set de reglas y escuadras para dibujo", "Andrea Díaz Torres", "Lucía Paredes Soto", 18.00m, 15.00m, "CONTRAOFERTADA", "24/10/2026 11:30"),
        new(23, 6, "Introducción al derecho — Rubio Correa", "Andrea Díaz Torres", "Mateo Quispe Huamán", 25.00m, 22.00m, "ACEPTADA", "09/10/2026 08:45"),
        new(24, 7, "Soporte plegable para libros", "Andrea Díaz Torres", "Valeria Chávez León", 15.00m, 10.00m, "RECHAZADA", "06/10/2026 19:10"),
    };

    public static readonly List<TransaccionDemo> Transacciones = new()
    {
        new(1, 2, "Calculadora científica Casio fx-991", "VENDEDOR", "Lucía Paredes Soto", 4, 60.00m, "CITA_CONFIRMADA",
            "19/10/2026", "29/10/2026 12:30", "Biblioteca Central", "CONFIRMADA", false, false),
        new(2, 6, "Introducción al derecho — Rubio Correa", "COMPRADOR", "Mateo Quispe Huamán", 3, 22.00m, "COMPLETADA",
            "09/10/2026", "11/10/2026 13:00", "Comedor Central", "REALIZADA", true, true),
        new(3, 1, "Cálculo de una variable — Stewart 8va ed.", "COMPRADOR", "Diego Ramos Cárdenas", 2, 40.00m, "EN_NEGOCIACION",
            "26/10/2026", null, null, "SIN_CITA", false, false),
        new(4, 5, "Lámpara LED de escritorio", "VENDEDOR", "Valeria Chávez León", 5, 28.00m, "CANCELADA",
            "14/10/2026", "16/10/2026 17:00", "Biblioteca Central", "CANCELADA", false, false),
    };

    public static readonly List<ChatDemo> Chats = new()
    {
        new(1, 2, "Calculadora científica Casio fx-991", "Lucía Paredes Soto", "Perfecto, nos vemos el jueves en la biblioteca.", "10:42", "ACTIVO", 2, true),
        new(2, 1, "Cálculo de una variable — Stewart 8va ed.", "Diego Ramos Cárdenas", "¿Podrías bajar un poco el precio?", "Ayer", "ACTIVO", 0, false),
        new(3, 8, "Física I — Serway 10ma ed.", "Mateo Quispe Huamán", "Hola, ¿el libro aún está disponible?", "Ayer", "PENDIENTE", 1, true),
        new(4, 6, "Introducción al derecho — Rubio Correa", "Mateo Quispe Huamán", "¡Gracias por la compra!", "11/10", "CERRADO", 0, false),
    };

    public static readonly List<MensajeDemo> Mensajes = new()
    {
        new(false, "Hola Andrea, ¿la calculadora todavía está disponible?", "09:58"),
        new(true, "Hola, el material sigue disponible.", "10:01"),
        new(false, "Genial. Te mandé una oferta de S/ 60, ¿te parece?", "10:05"),
        new(true, "Sí, ya la acepté. ¿Te parece si coordinamos la entrega?", "10:20"),
        new(false, "Claro, ¿el jueves al mediodía?", "10:31"),
        new(true, "Podemos encontrarnos en la Biblioteca Central.", "10:35"),
        new(false, "Perfecto, nos vemos el jueves en la biblioteca.", "10:42"),
    };

    public static readonly List<RespuestaRapidaDemo> RespuestasRapidas = new()
    {
        new(1, "Hola, el material sigue disponible."),
        new(2, "Podemos encontrarnos en la Biblioteca Central."),
        new(3, "Gracias por tu interés. Enseguida te respondo."),
        new(4, "El precio es negociable, envíame tu oferta."),
    };

    public static readonly List<NotificacionDemo> Notificaciones = new()
    {
        new(1, "RECORDATORIO_CITA", "Tienes una cita de entrega mañana a las 12:30 en Biblioteca Central.", "Hace 1 h", false, "/transacciones/1"),
        new(2, "NUEVO_MENSAJE", "Lucía Paredes te envió un mensaje sobre «Calculadora científica Casio fx-991».", "Hace 2 h", false, "/mensajes/1"),
        new(3, "SOLICITUD_CONTACTO", "Mateo Quispe quiere contactarte por «Física I — Serway 10ma ed.».", "Ayer", false, "/mensajes"),
        new(4, "CAMBIO_ESTADO_ANUNCIO", "Tu anuncio «Calculadora científica Casio fx-991» pasó a RESERVADO.", "19/10/2026", true, "/mis-anuncios"),
        new(5, "MODERACION", "Tu reporte #3 fue revisado por el equipo de moderación.", "15/10/2026", true, "/mis-reportes"),
        new(6, "SISTEMA", "¡Obtuviste la insignia «Primera Venta»!", "10/10/2026", true, "/perfil"),
    };

    public static readonly List<UsuarioDemo> Usuarios = new()
    {
        UsuarioActual,
        new(2, "20211045", "Diego Ramos Cárdenas", "d20211045@pucp.edu.pe", "Ciencias e Ingeniería", "Ingeniería Civil", "ACTIVA", 4.2, "CONFIABLE", 0, 4, 2, "15/08/2026"),
        new(3, "20190877", "Mateo Quispe Huamán", "m20190877@pucp.edu.pe", "Derecho", "Derecho", "ACTIVA", 4.9, "DESTACADO", 1, 12, 5, "05/08/2026"),
        new(4, "20231560", "Lucía Paredes Soto", "l20231560@pucp.edu.pe", "Arquitectura y Urbanismo", "Arquitectura", "ACTIVA", 3.8, "NUEVO", 1, 2, 1, "20/08/2026"),
        new(5, "20220391", "Valeria Chávez León", "v20220391@pucp.edu.pe", "Ciencias Sociales", "Economía", "PENDIENTE_VERIFICACION", 0, "NUEVO", 0, 0, 0, "24/10/2026"),
        new(6, "20180222", "Renzo Salas Vega", "r20180222@pucp.edu.pe", "Gestión y Alta Dirección", "Gestión Empresarial", "SUSPENDIDA", 2.1, "NUEVO", 4, 1, 3, "10/08/2026"),
    };

    public static readonly List<CalificacionDemo> Calificaciones = new()
    {
        new("Mateo Quispe Huamán", 5, "Muy puntual y el libro estaba tal cual la descripción.", "11/10/2026", "COMPRADOR_A_VENDEDOR"),
        new("Lucía Paredes Soto", 5, "Excelente comunicación, todo muy rápido.", "05/10/2026", "VENDEDOR_A_COMPRADOR"),
        new("Diego Ramos Cárdenas", 4, "Buena experiencia, llegó unos minutos tarde.", "28/09/2026", "COMPRADOR_A_VENDEDOR"),
    };

    public static readonly List<InsigniaDemo> Insignias = new()
    {
        new(1, "Primera Venta", "Completar al menos una venta.", "bi-bag-check", "VENDEDOR", true,
            "VENTAS_COMPLETADAS ≥ 1", true, "10/10/2026"),
        new(2, "Vendedor Confiable", "Completar cinco ventas y obtener promedio mínimo de 4.5.", "bi-shield-check", "VENDEDOR", true,
            "VENTAS_COMPLETADAS ≥ 5 · CALIFICACION_PROMEDIO ≥ 4.5", true, "20/10/2026"),
        new(3, "Comprador Puntual", "Completar una compra sin inasistencias sancionadas.", "bi-clock-history", "COMPRADOR", true,
            "COMPRAS_COMPLETADAS ≥ 1 · INASISTENCIAS_SANCIONADAS = 0", true, "11/10/2026"),
        new(4, "Participante ReUse", "Completar dos transacciones.", "bi-recycle", "GENERAL", true,
            "TRANSACCIONES_COMPLETADAS ≥ 2", true, "11/10/2026"),
        new(5, "Vendedor Experto", "Completar veinte ventas con promedio mínimo de 4.8.", "bi-trophy", "VENDEDOR", true,
            "VENTAS_COMPLETADAS ≥ 20 · CALIFICACION_PROMEDIO ≥ 4.8", false, null),
        new(6, "Insignia retirada", "Ejemplo de insignia inactiva.", "bi-archive", "GENERAL", false,
            "TRANSACCIONES_COMPLETADAS ≥ 100", false, null),
    };

    public static readonly List<ReporteDemo> Reportes = new()
    {
        new(1, "ANUNCIO", "INFORMACION_FALSA", "Maqueta de vivienda unifamiliar", 4, "Diego Ramos Cárdenas",
            "La escala indicada no coincide con las fotos del anuncio.", "24/10/2026", "PENDIENTE", null),
        new(2, "USUARIO", "NO_SE_PRESENTO_A_LA_CITA", "Renzo Salas Vega", 6, "Andrea Díaz Torres",
            "Acordamos la entrega en el Comedor Central y no se presentó ni respondió mensajes.", "22/10/2026", "PENDIENTE", null),
        new(3, "USUARIO", "COMPORTAMIENTO_INAPROPIADO", "Renzo Salas Vega", 6, "Valeria Chávez León",
            "Envió mensajes ofensivos después de rechazar su oferta.", "15/10/2026", "SANCIONADO", "Admin Carla Mendoza"),
        new(4, "ANUNCIO", "SPAM", "Soporte plegable para libros", 7, "Mateo Quispe Huamán",
            "Parece publicado varias veces.", "12/10/2026", "DESESTIMADO", "Admin Carla Mendoza"),
        new(5, "USUARIO", "PRODUCTO_NO_COINCIDE", "Lucía Paredes Soto", 4, "Diego Ramos Cárdenas",
            "El set entregado no incluía el escalímetro.", "26/10/2026", "PENDIENTE", null),
    };

    public static readonly List<PuntoEntregaDemo> PuntosEntrega = new()
    {
        new(1, "Biblioteca Central", "Ingreso principal.", "https://www.google.com/maps/search/?api=1&query=Biblioteca+Central+PUCP", true),
        new(2, "Comedor Central", "Zona exterior del comedor.", "https://www.google.com/maps/search/?api=1&query=Comedor+Central+PUCP", true),
        new(3, "Tinkuy — Ciencias e Ingeniería", "Frente a la cafetería.", "https://www.google.com/maps/search/?api=1&query=PUCP", true),
        new(4, "Pabellón en remodelación", "Temporalmente fuera de servicio.", "https://www.google.com/maps/search/?api=1&query=PUCP", false),
    };

    public static readonly List<BloqueoDemo> Bloqueos = new()
    {
        new(1, "Renzo Salas Vega", "20180222", "15/08/2026"),
    };

    // ---------- Ayudas de presentación ----------

    public static AnuncioDemo Anuncio(int id) => Anuncios.FirstOrDefault(a => a.Id == id) ?? Anuncios[0];
    public static TransaccionDemo Transaccion(int id) => Transacciones.FirstOrDefault(t => t.Id == id) ?? Transacciones[0];
    public static ChatDemo Chat(int id) => Chats.FirstOrDefault(c => c.Id == id) ?? Chats[0];
    public static UsuarioDemo Usuario(int id) => Usuarios.FirstOrDefault(u => u.Id == id) ?? Usuarios[1];
    public static ReporteDemo Reporte(int id) => Reportes.FirstOrDefault(r => r.Id == id) ?? Reportes[0];

    public static string IconoCategoria(string categoria) => categoria switch
    {
        "Libro" => "bi-book",
        "Útiles" => "bi-rulers",
        "Maqueta" => "bi-buildings",
        "Calculadora" => "bi-calculator",
        "Equipo de estudio" => "bi-lamp",
        _ => "bi-box-seam"
    };

    public static string TonoCategoria(string categoria) => categoria switch
    {
        "Libro" => "tono-1",
        "Útiles" => "tono-2",
        "Maqueta" => "tono-3",
        "Calculadora" => "tono-5",
        "Equipo de estudio" => "tono-4",
        _ => "tono-6"
    };

    public static string Iniciales(string nombre)
    {
        var partes = nombre.Split(' ', StringSplitOptions.RemoveEmptyEntries);
        return partes.Length >= 2 ? $"{partes[0][0]}{partes[1][0]}" : nombre[..Math.Min(2, nombre.Length)].ToUpper();
    }

    public static string Texto(string valorEnum) =>
        string.IsNullOrEmpty(valorEnum) ? "" :
        char.ToUpper(valorEnum[0]) + valorEnum[1..].ToLower().Replace('_', ' ');
}
