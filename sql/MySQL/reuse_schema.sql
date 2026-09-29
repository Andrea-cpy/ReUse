-- =====================================================================
-- ReUse | Laboratorio N. 06 | Creacion de la base de datos y datos iniciales
-- Curso: Programacion 3 (INF246) - PUCP 2026-2 | Equipo: Erasmolovers
-- Motor: MySQL 8.0 (InnoDB). Codificacion: utf8mb4.
--
-- Levantamiento de observaciones de la JP (Lab 04):
--   Obs. 1: facultad y carrera son tablas (ya no ENUM). usuario y material
--           apuntan SOLO a carrera; la facultad se obtiene navegando por carrera.
--   Obs. 2: UNIQUE en usuario.correo_institucional y usuario.codigo_pucp.
--   Obs. 3: todas las PK son INT AUTO_INCREMENT (Java nunca asigna IDs).
--   Obs. 4: las fechas de registro usan DEFAULT CURRENT_TIMESTAMP
--           (usuario.fecha_registro, oferta.fecha, transaccion.fecha_inicio,
--           mensaje.fecha_hora, etc.). Java no envia estas fechas al insertar.
--   Obs. 5: material_carrera resuelve la relacion N:M material <-> carrera.
--   Obs. 6: todas las tablas tienen borrado logico (activo) y auditoria
--           (fecha_creacion, fecha_modificacion, usuario_creacion,
--           usuario_modificacion).
--
-- Los estados con valores fijos (maquinas de estado del negocio) se guardan
-- como VARCHAR + CHECK y en Java se leen con Enum.valueOf(...) / .name().
-- Los catalogos que crecen (facultad, carrera, categoria) son tablas.
-- reputacion y contador_reportes son atributos derivados que mantiene la
-- capa de negocio dentro de transacciones (rendimiento del dashboard, RNF-01).
--
-- La seccion 9 crea los procedimientos almacenados que usan los DAO: Java no
-- escribe SQL, solo invoca {call ...} con CallableStatement.
--
-- ADVERTENCIA: el script elimina y vuelve a crear el esquema reuse_db.
-- =====================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS reuse_db;
CREATE DATABASE reuse_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE reuse_db;

-- =====================================================================
-- 1. CATALOGOS ACADEMICOS (Obs. 1)
-- =====================================================================
CREATE TABLE facultad (
    id_facultad          INT          NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(120) NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_facultad PRIMARY KEY (id_facultad),
    CONSTRAINT uq_facultad_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE carrera (
    id_carrera           INT          NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(120) NOT NULL,
    id_facultad          INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_carrera PRIMARY KEY (id_carrera),
    CONSTRAINT uq_carrera_nombre UNIQUE (nombre),
    CONSTRAINT fk_carrera_facultad FOREIGN KEY (id_facultad)
        REFERENCES facultad (id_facultad) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- 2. USUARIOS (Obs. 1, 2, 3 y 4)
-- =====================================================================
CREATE TABLE usuario (
    id_usuario           INT           NOT NULL AUTO_INCREMENT,
    codigo_pucp          CHAR(8)       NOT NULL,
    nombres              VARCHAR(100)  NOT NULL,
    apellido_paterno     VARCHAR(60)   NOT NULL,
    apellido_materno     VARCHAR(60)   NULL,
    correo_institucional VARCHAR(150)  NOT NULL,
    contrasena           VARCHAR(255)  NOT NULL,
    id_carrera           INT           NULL,          -- NULL solo para administradores
    verificado           TINYINT(1)    NOT NULL DEFAULT 0,
    estado_cuenta        VARCHAR(25)   NOT NULL DEFAULT 'PENDIENTE_VERIFICACION',
    reputacion           DECIMAL(3,2)  NOT NULL DEFAULT 0.00,
    contador_reportes    INT           NOT NULL DEFAULT 0,
    foto_perfil          VARCHAR(500)  NULL,
    fecha_registro       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo               TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)   NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)   NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_codigo_pucp UNIQUE (codigo_pucp),
    CONSTRAINT uq_usuario_correo_institucional UNIQUE (correo_institucional),
    CONSTRAINT fk_usuario_carrera FOREIGN KEY (id_carrera)
        REFERENCES carrera (id_carrera) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_usuario_correo CHECK (correo_institucional LIKE '%@pucp.edu.pe'),
    CONSTRAINT chk_usuario_estado_cuenta
        CHECK (estado_cuenta IN ('PENDIENTE_VERIFICACION', 'ACTIVA', 'SUSPENDIDA')),
    CONSTRAINT chk_usuario_reputacion CHECK (reputacion BETWEEN 0 AND 5),
    CONSTRAINT chk_usuario_contador_reportes CHECK (contador_reportes >= 0)
) ENGINE=InnoDB;

-- Subtipo de usuario (herencia): comparte la PK con usuario.
CREATE TABLE administrador (
    id_usuario           INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_administrador PRIMARY KEY (id_usuario),
    CONSTRAINT fk_administrador_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- 3. CATALOGO DE MATERIALES Y ANUNCIOS (Obs. 5)
-- =====================================================================
CREATE TABLE categoria_material (
    id_categoria         INT          NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(80)  NOT NULL,
    descripcion          VARCHAR(300) NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_categoria_material PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_material_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE material_academico (
    id_material          INT          NOT NULL AUTO_INCREMENT,
    titulo               VARCHAR(200) NOT NULL,
    id_categoria         INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_material_academico PRIMARY KEY (id_material),
    CONSTRAINT fk_material_academico_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria_material (id_categoria) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- Tabla intermedia N:M: un material puede servir a una o mas carreras (Obs. 5).
CREATE TABLE material_carrera (
    id_material          INT          NOT NULL,
    id_carrera           INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_material_carrera PRIMARY KEY (id_material, id_carrera),
    CONSTRAINT fk_material_carrera_material FOREIGN KEY (id_material)
        REFERENCES material_academico (id_material) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_material_carrera_carrera FOREIGN KEY (id_carrera)
        REFERENCES carrera (id_carrera) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE anuncio (
    id_anuncio           INT           NOT NULL AUTO_INCREMENT,
    titulo               VARCHAR(150)  NOT NULL,
    precio               DECIMAL(10,2) NOT NULL,
    descripcion          TEXT          NOT NULL,
    condicion            VARCHAR(20)   NOT NULL,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'DISPONIBLE',
    fecha_publicacion    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_vendedor          INT           NOT NULL,
    id_material          INT           NOT NULL,
    activo               TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)   NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)   NULL,
    CONSTRAINT pk_anuncio PRIMARY KEY (id_anuncio),
    CONSTRAINT fk_anuncio_vendedor FOREIGN KEY (id_vendedor)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_anuncio_material FOREIGN KEY (id_material)
        REFERENCES material_academico (id_material) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_anuncio_precio CHECK (precio > 0),
    CONSTRAINT chk_anuncio_condicion CHECK (condicion IN ('NUEVO', 'USADO', 'REACONDICIONADO')),
    CONSTRAINT chk_anuncio_estado
        CHECK (estado IN ('DISPONIBLE', 'RESERVADO', 'VENDIDO', 'OBSERVADO', 'ARCHIVADO'))
) ENGINE=InnoDB;

CREATE TABLE imagen_producto (
    id_imagen            INT          NOT NULL AUTO_INCREMENT,
    url                  VARCHAR(500) NOT NULL,
    peso_bytes           BIGINT       NOT NULL,
    formato              VARCHAR(10)  NOT NULL,
    id_anuncio           INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_imagen_producto PRIMARY KEY (id_imagen),
    CONSTRAINT fk_imagen_producto_anuncio FOREIGN KEY (id_anuncio)
        REFERENCES anuncio (id_anuncio) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_imagen_producto_peso CHECK (peso_bytes > 0)
) ENGINE=InnoDB;

CREATE TABLE favorito (
    id_favorito          INT          NOT NULL AUTO_INCREMENT,
    fecha_guardado       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario           INT          NOT NULL,
    id_anuncio           INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_favorito PRIMARY KEY (id_favorito),
    CONSTRAINT fk_favorito_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_favorito_anuncio FOREIGN KEY (id_anuncio)
        REFERENCES anuncio (id_anuncio) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- 4. NEGOCIACION: OFERTAS, TRANSACCIONES Y CITAS
-- =====================================================================
CREATE TABLE oferta (
    id_oferta            INT           NOT NULL AUTO_INCREMENT,
    monto_propuesto      DECIMAL(10,2) NOT NULL,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    fecha                DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_comprador         INT           NOT NULL,
    id_anuncio           INT           NOT NULL,
    activo               TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)   NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)   NULL,
    CONSTRAINT pk_oferta PRIMARY KEY (id_oferta),
    CONSTRAINT fk_oferta_comprador FOREIGN KEY (id_comprador)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_oferta_anuncio FOREIGN KEY (id_anuncio)
        REFERENCES anuncio (id_anuncio) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_oferta_monto CHECK (monto_propuesto > 0),
    CONSTRAINT chk_oferta_estado
        CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'CONTRAOFERTADA'))
) ENGINE=InnoDB;

CREATE TABLE transaccion (
    id_transaccion         INT          NOT NULL AUTO_INCREMENT,
    fecha_inicio           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin              DATETIME     NULL,
    estado                 VARCHAR(20)  NOT NULL DEFAULT 'EN_NEGOCIACION',
    confirmacion_comprador TINYINT(1)   NOT NULL DEFAULT 0,
    confirmacion_vendedor  TINYINT(1)   NOT NULL DEFAULT 0,
    id_anuncio             INT          NOT NULL,
    id_comprador           INT          NOT NULL,
    id_oferta              INT          NULL,
    activo                 TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion     DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion       VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion   VARCHAR(50)  NULL,
    CONSTRAINT pk_transaccion PRIMARY KEY (id_transaccion),
    CONSTRAINT uq_transaccion_oferta UNIQUE (id_oferta),
    CONSTRAINT fk_transaccion_anuncio FOREIGN KEY (id_anuncio)
        REFERENCES anuncio (id_anuncio) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_transaccion_comprador FOREIGN KEY (id_comprador)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_transaccion_oferta FOREIGN KEY (id_oferta)
        REFERENCES oferta (id_oferta) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_transaccion_estado
        CHECK (estado IN ('EN_NEGOCIACION', 'CITA_CONFIRMADA', 'COMPLETADA', 'CANCELADA'))
) ENGINE=InnoDB;

CREATE TABLE punto_entrega (
    id_punto_entrega     INT          NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(120) NOT NULL,
    referencia           VARCHAR(250) NULL,
    ubicacion            VARCHAR(250) NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_punto_entrega PRIMARY KEY (id_punto_entrega),
    CONSTRAINT uq_punto_entrega_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE cita_entrega (
    id_cita              INT          NOT NULL AUTO_INCREMENT,
    fecha_hora           DATETIME     NOT NULL,       -- fecha acordada por los usuarios
    estado               VARCHAR(20)  NOT NULL DEFAULT 'PROPUESTA',
    id_punto_entrega     INT          NOT NULL,
    id_transaccion       INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_cita_entrega PRIMARY KEY (id_cita),
    CONSTRAINT uq_cita_entrega_transaccion UNIQUE (id_transaccion),
    CONSTRAINT fk_cita_entrega_punto FOREIGN KEY (id_punto_entrega)
        REFERENCES punto_entrega (id_punto_entrega) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_cita_entrega_transaccion FOREIGN KEY (id_transaccion)
        REFERENCES transaccion (id_transaccion) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_cita_entrega_estado
        CHECK (estado IN ('PROPUESTA', 'CONFIRMADA', 'CANCELADA', 'REALIZADA'))
) ENGINE=InnoDB;

-- =====================================================================
-- 5. MENSAJERIA
-- =====================================================================
CREATE TABLE canal_chat (
    id_chat              INT          NOT NULL AUTO_INCREMENT,
    estado               VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta      DATETIME     NULL,
    fecha_cierre         DATETIME     NULL,
    id_anuncio           INT          NOT NULL,
    id_comprador         INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_canal_chat PRIMARY KEY (id_chat),
    CONSTRAINT fk_canal_chat_anuncio FOREIGN KEY (id_anuncio)
        REFERENCES anuncio (id_anuncio) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_canal_chat_comprador FOREIGN KEY (id_comprador)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_canal_chat_estado
        CHECK (estado IN ('PENDIENTE', 'ACTIVO', 'RECHAZADO', 'BLOQUEADO', 'CERRADO'))
) ENGINE=InnoDB;

CREATE TABLE mensaje (
    id_mensaje           INT          NOT NULL AUTO_INCREMENT,
    contenido            TEXT         NOT NULL,
    fecha_hora           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leido                TINYINT(1)   NOT NULL DEFAULT 0,
    id_chat              INT          NOT NULL,
    id_emisor            INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_mensaje PRIMARY KEY (id_mensaje),
    CONSTRAINT fk_mensaje_chat FOREIGN KEY (id_chat)
        REFERENCES canal_chat (id_chat) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_mensaje_emisor FOREIGN KEY (id_emisor)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE notificacion (
    id_notificacion      INT          NOT NULL AUTO_INCREMENT,
    mensaje              VARCHAR(500) NOT NULL,
    fecha_hora           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo                 VARCHAR(30)  NOT NULL,
    estado               VARCHAR(20)  NOT NULL DEFAULT 'NO_LEIDA',
    id_destinatario      INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_notificacion PRIMARY KEY (id_notificacion),
    CONSTRAINT fk_notificacion_destinatario FOREIGN KEY (id_destinatario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_notificacion_tipo
        CHECK (tipo IN ('RECORDATORIO_CITA', 'CAMBIO_ESTADO_ANUNCIO', 'NUEVO_MENSAJE',
                        'SOLICITUD_CONTACTO', 'MODERACION', 'SISTEMA')),
    CONSTRAINT chk_notificacion_estado CHECK (estado IN ('NO_LEIDA', 'LEIDA'))
) ENGINE=InnoDB;

CREATE TABLE respuesta_rapida (
    id_respuesta         INT          NOT NULL AUTO_INCREMENT,
    texto                VARCHAR(500) NOT NULL,
    id_creador           INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_respuesta_rapida PRIMARY KEY (id_respuesta),
    CONSTRAINT fk_respuesta_rapida_creador FOREIGN KEY (id_creador)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- 6. MODERACION: BLOQUEOS, CALIFICACIONES Y REPORTES
-- =====================================================================
CREATE TABLE bloqueo (
    id_bloqueo           INT          NOT NULL AUTO_INCREMENT,
    fecha                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    id_bloqueador        INT          NOT NULL,
    id_bloqueado         INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_bloqueo PRIMARY KEY (id_bloqueo),
    CONSTRAINT fk_bloqueo_bloqueador FOREIGN KEY (id_bloqueador)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_bloqueo_bloqueado FOREIGN KEY (id_bloqueado)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_bloqueo_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE=InnoDB;

CREATE TABLE calificacion (
    id_calificacion      INT          NOT NULL AUTO_INCREMENT,
    puntaje              TINYINT      NOT NULL,
    comentario           VARCHAR(500) NULL,
    fecha                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo                 VARCHAR(30)  NOT NULL,
    id_transaccion       INT          NOT NULL,
    id_calificador       INT          NOT NULL,
    id_calificado        INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_calificacion PRIMARY KEY (id_calificacion),
    CONSTRAINT fk_calificacion_transaccion FOREIGN KEY (id_transaccion)
        REFERENCES transaccion (id_transaccion) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_calificacion_calificador FOREIGN KEY (id_calificador)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_calificacion_calificado FOREIGN KEY (id_calificado)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_calificacion_puntaje CHECK (puntaje BETWEEN 1 AND 5),
    CONSTRAINT chk_calificacion_tipo
        CHECK (tipo IN ('COMPRADOR_A_VENDEDOR', 'VENDEDOR_A_COMPRADOR'))
) ENGINE=InnoDB;

-- Supertipo de reportes; cada reporte tiene exactamente UN subtipo
-- (reporte_usuario o reporte_anuncio). Ambos se guardan en una transaccion.
CREATE TABLE reporte (
    id_reporte           INT          NOT NULL AUTO_INCREMENT,
    descripcion          TEXT         NOT NULL,
    fecha_registro       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_revision      VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_revision       DATETIME     NULL,
    id_denunciante       INT          NOT NULL,
    id_revisor           INT          NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_reporte PRIMARY KEY (id_reporte),
    CONSTRAINT fk_reporte_denunciante FOREIGN KEY (id_denunciante)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_reporte_revisor FOREIGN KEY (id_revisor)
        REFERENCES administrador (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_reporte_estado_revision
        CHECK (estado_revision IN ('PENDIENTE', 'SANCIONADO', 'DESESTIMADO'))
) ENGINE=InnoDB;

CREATE TABLE reporte_usuario (
    id_reporte           INT          NOT NULL,
    motivo               VARCHAR(40)  NOT NULL,
    id_denunciado        INT          NOT NULL,
    id_transaccion       INT          NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_reporte_usuario PRIMARY KEY (id_reporte),
    CONSTRAINT fk_reporte_usuario_reporte FOREIGN KEY (id_reporte)
        REFERENCES reporte (id_reporte) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_reporte_usuario_denunciado FOREIGN KEY (id_denunciado)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_reporte_usuario_transaccion FOREIGN KEY (id_transaccion)
        REFERENCES transaccion (id_transaccion) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_reporte_usuario_motivo
        CHECK (motivo IN ('FRAUDE_O_ESTAFA', 'NO_SE_PRESENTO_A_LA_CITA', 'PRODUCTO_NO_COINCIDE',
                          'COMPORTAMIENTO_INAPROPIADO', 'OTRO'))
) ENGINE=InnoDB;

CREATE TABLE reporte_anuncio (
    id_reporte           INT          NOT NULL,
    motivo               VARCHAR(40)  NOT NULL,
    id_anuncio           INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_reporte_anuncio PRIMARY KEY (id_reporte),
    CONSTRAINT fk_reporte_anuncio_reporte FOREIGN KEY (id_reporte)
        REFERENCES reporte (id_reporte) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_reporte_anuncio_anuncio FOREIGN KEY (id_anuncio)
        REFERENCES anuncio (id_anuncio) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_reporte_anuncio_motivo
        CHECK (motivo IN ('CONTENIDO_PROHIBIDO', 'INFORMACION_FALSA', 'SPAM', 'OTRO'))
) ENGINE=InnoDB;

-- =====================================================================
-- 7. GAMIFICACION: INSIGNIAS Y REGLAS
-- =====================================================================
CREATE TABLE insignia (
    id_insignia          INT          NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(120) NOT NULL,
    descripcion          VARCHAR(400) NOT NULL,
    icono                VARCHAR(500) NULL,
    tipo                 VARCHAR(20)  NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_insignia PRIMARY KEY (id_insignia),
    CONSTRAINT uq_insignia_nombre UNIQUE (nombre),
    CONSTRAINT chk_insignia_tipo CHECK (tipo IN ('VENDEDOR', 'COMPRADOR', 'GENERAL'))
) ENGINE=InnoDB;

CREATE TABLE regla_insignia (
    id_regla             INT           NOT NULL AUTO_INCREMENT,
    tipo_metrica         VARCHAR(40)   NOT NULL,
    operador             VARCHAR(20)   NOT NULL,
    valor_objetivo       DECIMAL(10,2) NOT NULL,
    id_insignia          INT           NOT NULL,
    activo               TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)   NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)   NULL,
    CONSTRAINT pk_regla_insignia PRIMARY KEY (id_regla),
    CONSTRAINT fk_regla_insignia_insignia FOREIGN KEY (id_insignia)
        REFERENCES insignia (id_insignia) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_regla_insignia_metrica
        CHECK (tipo_metrica IN ('VENTAS_COMPLETADAS', 'COMPRAS_COMPLETADAS', 'TRANSACCIONES_COMPLETADAS',
                                'CALIFICACION_PROMEDIO', 'REPORTES_SANCIONADOS',
                                'INASISTENCIAS_SANCIONADAS')),
    CONSTRAINT chk_regla_insignia_operador
        CHECK (operador IN ('MAYOR_O_IGUAL', 'MAYOR', 'MENOR_O_IGUAL', 'MENOR', 'IGUAL'))
) ENGINE=InnoDB;

CREATE TABLE insignia_usuario (
    id_insignia_usuario  INT          NOT NULL AUTO_INCREMENT,
    fecha_obtencion      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario           INT          NOT NULL,
    id_insignia          INT          NOT NULL,
    activo               TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   DATETIME     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    usuario_creacion     VARCHAR(50)  NOT NULL DEFAULT 'SISTEMA',
    usuario_modificacion VARCHAR(50)  NULL,
    CONSTRAINT pk_insignia_usuario PRIMARY KEY (id_insignia_usuario),
    CONSTRAINT uq_insignia_usuario UNIQUE (id_usuario, id_insignia),
    CONSTRAINT fk_insignia_usuario_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_insignia_usuario_insignia FOREIGN KEY (id_insignia)
        REFERENCES insignia (id_insignia) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- 8. DATOS INICIALES (padres antes que hijos)
-- No se envian IDs: los genera AUTO_INCREMENT. Como el esquema es nuevo,
-- los IDs se asignan en el orden de insercion (1, 2, 3...) y las FK de
-- las tablas hijas usan esos valores.
-- Las fechas historicas se escriben explicitamente solo para que los datos
-- de prueba sean coherentes; en la aplicacion las asigna la base de datos.
-- =====================================================================
START TRANSACTION;

-- facultad: 13 registros
INSERT INTO facultad (nombre) VALUES
    ('Arquitectura y Urbanismo'),              -- 1
    ('Arte y Diseno'),                          -- 2
    ('Artes Escenicas'),                        -- 3
    ('Ciencias Contables'),                     -- 4
    ('Ciencias e Ingenieria'),                  -- 5
    ('Ciencias Sociales'),                      -- 6
    ('Ciencias y Artes de la Comunicacion'),    -- 7
    ('Derecho'),                                -- 8
    ('Educacion'),                              -- 9
    ('Gastronomia, Hoteleria y Turismo'),       -- 10
    ('Gestion y Alta Direccion'),               -- 11
    ('Letras y Ciencias Humanas'),              -- 12
    ('Estudios Generales Ciencias'),            -- 13
    ('Estudios Generales Letras'),              -- 14
    ('Psicologia');                             -- 15

-- carrera: 55 registros
INSERT INTO carrera (nombre, id_facultad) VALUES
    ('Arquitectura', 1),                            -- 1
    ('Educacion Artistica', 2),                     -- 2
    ('Diseno Grafico', 2),                          -- 3
    ('Diseno Industrial', 2),                       -- 4
    ('Escultura', 2),                               -- 5
    ('Pintura', 2),                                 -- 6
    ('Arte, Moda y Diseno Textil', 2),              -- 7
    ('Grabado', 2),                                 -- 8
    ('Danza', 3),                                   -- 9
    ('Teatro', 3),                                  -- 10
    ('Musica', 3),                                  -- 11
    ('Creacion y Produccion Escenica', 3),          -- 12
    ('Contabilidad', 4),                            -- 13
    ('Estadistica', 5),                             -- 14
    ('Fisica', 5),                                  -- 15
    ('Matematicas', 5),                             -- 16
    ('Quimica', 5),                                 -- 17
    ('Ingenieria Ambiental y Sostenible', 5),       -- 18
    ('Ingenieria Biomedica', 5),                    -- 19
    ('Ingenieria Civil', 5),                        -- 20
    ('Ingenieria de las Telecomunicaciones', 5),    -- 21
    ('Ingenieria de Minas', 5),                     -- 22
    ('Ingenieria Electronica', 5),                  -- 23
    ('Ingenieria Geologica', 5),                    -- 24
    ('Ingenieria Industrial', 5),                   -- 25
    ('Ingenieria Informatica', 5),                  -- 26
    ('Ingenieria Mecanica', 5),                     -- 27
    ('Ingenieria Mecatronica', 5),                  -- 28
    ('Ingenieria Quimica', 5),                      -- 29
    ('Antropologia', 6),                            -- 30
    ('Ciencia Politica y Gobierno', 6),             -- 31
    ('Economia', 6),                                -- 32
    ('Finanzas', 6),                                -- 33
    ('Relaciones Internacionales', 6),              -- 34
    ('Sociologia', 6),                              -- 35
    ('Comunicacion Audiovisual', 7),                -- 36
    ('Comunicacion para el Desarrollo', 7),         -- 37
    ('Publicidad', 7),                              -- 38
    ('Periodismo', 7),                              -- 39
    ('Derecho', 8),                                 -- 40
    ('Educacion Primaria', 9),                      -- 41
    ('Educacion Inicial', 9),                       -- 42
    ('Educacion Secundaria', 9),                    -- 43
    ('Gastronomia', 10),                            -- 44
    ('Hoteleria', 10),                              -- 45
    ('Turismo', 10),                                -- 46
    ('Gestion', 11),                                -- 47
    ('Arqueologia', 12),                            -- 48
    ('Ciencias de la Informacion', 12),             -- 49
    ('Filosofia', 12),                              -- 50
    ('Geografia y Medio Ambiente', 12),             -- 51
    ('Historia', 12),                               -- 52
    ('Humanidades', 12),                            -- 53
    ('Linguistica y Literatura', 12),               -- 54
    ('Psicologia', 13);                             -- 55

-- usuario: 7 registros (contrasenas de prueba, no son credenciales reales)
INSERT INTO usuario (codigo_pucp, nombres, apellido_paterno, apellido_materno, correo_institucional,
                     contrasena, id_carrera, verificado, estado_cuenta, reputacion, contador_reportes,
                     foto_perfil, fecha_registro) VALUES
    ('20260001', 'Ana', 'Torres', 'Vega', 'a20260001@pucp.edu.pe', 'DEMO_NO_USAR_EN_PRODUCCION', 26, 1, 'ACTIVA', 4.50, 0, NULL, '2026-08-01 09:00:00'),                -- 1
    ('20260002', 'Bruno', 'Ramos', 'Soto', 'a20260002@pucp.edu.pe', 'DEMO_NO_USAR_EN_PRODUCCION', 25, 1, 'ACTIVA', 5.00, 0, NULL, '2026-08-01 10:00:00'),              -- 2
    ('20260003', 'Carla', 'Lopez', 'Rios', 'a20260003@pucp.edu.pe', 'DEMO_NO_USAR_EN_PRODUCCION', 40, 1, 'ACTIVA', 0.00, 1, NULL, '2026-08-02 09:00:00'),              -- 3
    ('20260004', 'Diego', 'Mendoza', 'Paz', 'a20260004@pucp.edu.pe', 'DEMO_NO_USAR_EN_PRODUCCION', 1, 1, 'ACTIVA', 5.00, 1, NULL, '2026-08-02 10:00:00'),              -- 4
    ('20260005', 'Elena', 'Castro', NULL, 'a20260005@pucp.edu.pe', 'DEMO_NO_USAR_EN_PRODUCCION', 55, 0, 'PENDIENTE_VERIFICACION', 0.00, 0, NULL, '2026-09-12 08:00:00'), -- 5
    ('20260006', 'Fabian', 'Leon', 'Cruz', 'a20260006@pucp.edu.pe', 'DEMO_NO_USAR_EN_PRODUCCION', 13, 1, 'SUSPENDIDA', 0.00, 0, NULL, '2026-08-03 09:00:00'),          -- 6
    ('00000001', 'Admin', 'Prueba', NULL, 'admin.demo@pucp.edu.pe', 'DEMO_NO_USAR_EN_PRODUCCION', NULL, 1, 'ACTIVA', 0.00, 0, NULL, '2026-08-01 08:00:00');          -- 7

-- administrador: 1 registro
INSERT INTO administrador (id_usuario) VALUES (7);

-- categoria_material: 7 registros
INSERT INTO categoria_material (nombre, descripcion, activo) VALUES
    ('Libro', 'Libros y textos academicos.', 1),                                  -- 1
    ('Utiles', 'Utiles de estudio.', 1),                                          -- 2
    ('Maqueta', 'Maquetas y modelos academicos.', 1),                             -- 3
    ('Calculadora', 'Calculadoras para cursos.', 1),                              -- 4
    ('Equipo de estudio', 'Equipamiento academico reutilizable.', 1),             -- 5
    ('Otro', 'Otros materiales academicos.', 1),                                  -- 6
    ('Categoria de prueba inactiva', 'No disponible para nuevas publicaciones.', 0); -- 7

-- material_academico: 7 registros
INSERT INTO material_academico (titulo, id_categoria) VALUES
    ('Calculo de una variable', 1),     -- 1
    ('Calculadora cientifica', 4),      -- 2
    ('Set de reglas y escuadras', 2),   -- 3
    ('Maqueta de vivienda', 3),         -- 4
    ('Lampara de escritorio', 5),       -- 5
    ('Introduccion al derecho', 1),     -- 6
    ('Soporte para libros', 6);         -- 7

-- material_carrera: un material puede servir a varias carreras (N:M)
INSERT INTO material_carrera (id_material, id_carrera) VALUES
    (1, 26), (1, 25), (1, 16),
    (2, 25), (2, 26),
    (3, 1), (3, 4),
    (4, 1),
    (6, 40);

-- anuncio: 7 registros
INSERT INTO anuncio (titulo, precio, descripcion, condicion, estado, fecha_publicacion, id_vendedor, id_material) VALUES
    ('Libro de calculo usado', 45.50, 'Texto completo con anotaciones a lapiz.', 'USADO', 'VENDIDO', '2026-08-05 09:00:00', 1, 1),              -- 1
    ('Calculadora cientifica', 65.00, 'Funciona correctamente; incluye tapa.', 'USADO', 'RESERVADO', '2026-09-01 09:00:00', 1, 2),              -- 2
    ('Reglas para dibujo', 18.00, 'Set nuevo sin uso.', 'NUEVO', 'DISPONIBLE', '2026-09-02 09:00:00', 4, 3),                                    -- 3
    ('Maqueta arquitectonica', 80.00, 'Modelo de vivienda para estudio.', 'USADO', 'OBSERVADO', '2026-09-03 09:00:00', 4, 4),                   -- 4
    ('Lampara reparada', 30.00, 'Cable reemplazado; luz operativa.', 'REACONDICIONADO', 'ARCHIVADO', '2026-08-10 09:00:00', 1, 5),              -- 5
    ('Libro introductorio de derecho', 25.00, 'Libro conservado, sin hojas faltantes.', 'USADO', 'VENDIDO', '2026-08-15 09:00:00', 4, 6),       -- 6
    ('Soporte de lectura', 15.00, 'Soporte plegable para libros.', 'NUEVO', 'DISPONIBLE', '2026-09-04 09:00:00', 4, 7);                         -- 7

-- imagen_producto: 8 registros
INSERT INTO imagen_producto (url, peso_bytes, formato, id_anuncio) VALUES
    ('https://example.org/reuse/calculo-portada.jpg', 180000, 'jpg', 1),
    ('https://example.org/reuse/calculo-interior.jpg', 160000, 'jpg', 1),
    ('https://example.org/reuse/calculadora.png', 240000, 'png', 2),
    ('https://example.org/reuse/reglas.jpg', 120000, 'jpg', 3),
    ('https://example.org/reuse/maqueta.jpg', 300000, 'jpg', 4),
    ('https://example.org/reuse/lampara.jpg', 190000, 'jpg', 5),
    ('https://example.org/reuse/derecho.jpg', 150000, 'jpg', 6),
    ('https://example.org/reuse/soporte.jpg', 110000, 'jpg', 7);

-- favorito: 3 registros
INSERT INTO favorito (fecha_guardado, id_usuario, id_anuncio) VALUES
    ('2026-09-05 10:00:00', 2, 3),
    ('2026-09-05 11:00:00', 3, 2),
    ('2026-09-06 10:00:00', 1, 7);

-- oferta: 5 registros
INSERT INTO oferta (monto_propuesto, estado, fecha, id_comprador, id_anuncio) VALUES
    (40.00, 'ACEPTADA', '2026-08-06 10:00:00', 2, 1),         -- 1
    (60.00, 'ACEPTADA', '2026-09-11 10:00:00', 2, 2),         -- 2
    (16.00, 'PENDIENTE', '2026-09-12 10:00:00', 2, 3),        -- 3
    (8.00, 'RECHAZADA', '2026-09-10 10:00:00', 3, 7),         -- 4
    (12.00, 'CONTRAOFERTADA', '2026-09-11 11:00:00', 1, 7);   -- 5

-- transaccion: 7 registros
INSERT INTO transaccion (fecha_inicio, fecha_fin, estado, confirmacion_comprador, confirmacion_vendedor,
                         id_anuncio, id_comprador, id_oferta) VALUES
    ('2026-08-06 10:10:00', '2026-08-10 12:15:00', 'COMPLETADA', 1, 1, 1, 2, 1),         -- 1
    ('2026-08-06 11:00:00', '2026-08-07 09:00:00', 'CANCELADA', 0, 0, 1, 3, NULL),       -- 2
    ('2026-09-11 10:10:00', NULL, 'CITA_CONFIRMADA', 0, 0, 2, 2, 2),                     -- 3
    ('2026-09-12 11:00:00', NULL, 'EN_NEGOCIACION', 0, 0, 3, 2, NULL),                   -- 4
    ('2026-08-11 09:00:00', '2026-08-12 13:00:00', 'CANCELADA', 0, 0, 5, 3, NULL),       -- 5
    ('2026-08-16 09:00:00', '2026-08-20 14:10:00', 'COMPLETADA', 1, 1, 6, 1, NULL),      -- 6
    ('2026-09-12 12:00:00', NULL, 'EN_NEGOCIACION', 0, 0, 3, 3, NULL);                   -- 7

-- punto_entrega: 3 registros
INSERT INTO punto_entrega (nombre, referencia, ubicacion, activo) VALUES
    ('Biblioteca Central', 'Ingreso principal.', 'https://www.google.com/maps/search/?api=1&query=Biblioteca+Central+PUCP', 1),
    ('Comedor Central', 'Zona exterior del comedor.', 'https://www.google.com/maps/search/?api=1&query=Comedor+Central+PUCP', 1),
    ('Pabellon en remodelacion', 'Temporalmente fuera de servicio.', 'https://www.google.com/maps/search/?api=1&query=PUCP', 0);

-- cita_entrega: 6 registros
INSERT INTO cita_entrega (fecha_hora, estado, id_punto_entrega, id_transaccion) VALUES
    ('2026-08-10 12:00:00', 'REALIZADA', 1, 1),
    ('2026-08-11 12:00:00', 'CANCELADA', 1, 2),
    ('2026-09-16 12:00:00', 'CONFIRMADA', 2, 3),
    ('2026-09-17 12:00:00', 'PROPUESTA', 1, 4),
    ('2026-08-12 12:00:00', 'CANCELADA', 2, 5),
    ('2026-08-20 14:00:00', 'REALIZADA', 1, 6);

-- canal_chat: 5 registros
INSERT INTO canal_chat (estado, fecha_solicitud, fecha_respuesta, fecha_cierre, id_anuncio, id_comprador) VALUES
    ('CERRADO', '2026-08-06 09:00:00', '2026-08-06 09:10:00', '2026-08-10 13:00:00', 1, 2),
    ('ACTIVO', '2026-09-11 09:00:00', '2026-09-11 09:10:00', NULL, 2, 2),
    ('PENDIENTE', '2026-09-12 09:00:00', NULL, NULL, 3, 2),
    ('RECHAZADO', '2026-08-13 09:00:00', '2026-08-13 09:10:00', '2026-08-13 09:10:00', 5, 3),
    ('BLOQUEADO', '2026-08-15 09:00:00', NULL, '2026-08-15 09:10:00', 5, 3);

-- mensaje: 4 registros
INSERT INTO mensaje (contenido, fecha_hora, leido, id_chat, id_emisor) VALUES
    ('Hola, el libro tiene todas las paginas?', '2026-08-06 09:15:00', 1, 1, 2),
    ('Si, esta completo. Podemos coordinar la entrega.', '2026-08-06 09:20:00', 1, 1, 1),
    ('Confirmamos en el comedor a las doce?', '2026-09-12 09:00:00', 1, 2, 2),
    ('Si, nos vemos el miercoles 16.', '2026-09-12 09:05:00', 0, 2, 1);

-- notificacion: 6 registros
INSERT INTO notificacion (mensaje, fecha_hora, tipo, estado, id_destinatario) VALUES
    ('Tienes una cita el 16/09 a las 12:00 en el Comedor Central.', '2026-09-15 12:00:00', 'RECORDATORIO_CITA', 'NO_LEIDA', 2),
    ('Tu anuncio de calculo fue vendido.', '2026-08-10 12:15:00', 'CAMBIO_ESTADO_ANUNCIO', 'LEIDA', 1),
    ('Ana te envio un mensaje sobre la calculadora.', '2026-09-12 09:05:00', 'NUEVO_MENSAJE', 'NO_LEIDA', 2),
    ('Bruno solicita conversar sobre las reglas.', '2026-09-12 09:00:00', 'SOLICITUD_CONTACTO', 'NO_LEIDA', 4),
    ('El reporte de inasistencia fue resuelto.', '2026-08-14 10:00:00', 'MODERACION', 'LEIDA', 3),
    ('Bienvenida a ReUse. Verifica tu correo institucional.', '2026-09-12 08:00:00', 'SISTEMA', 'NO_LEIDA', 5);

-- respuesta_rapida: 3 registros
INSERT INTO respuesta_rapida (texto, id_creador) VALUES
    ('Hola, el material sigue disponible.', 1),
    ('Podemos encontrarnos en la Biblioteca Central.', 1),
    ('Gracias por tu interes. Enseguida te respondo.', 4);

-- bloqueo: 2 registros
INSERT INTO bloqueo (fecha, estado, id_bloqueador, id_bloqueado) VALUES
    ('2026-08-13 09:10:00', 'INACTIVO', 1, 3),
    ('2026-08-15 09:10:00', 'ACTIVO', 1, 3);

-- calificacion: 4 registros
INSERT INTO calificacion (puntaje, comentario, fecha, tipo, id_transaccion, id_calificador, id_calificado) VALUES
    (5, 'Libro en buen estado y entrega puntual.', '2026-08-10 13:00:00', 'COMPRADOR_A_VENDEDOR', 1, 2, 1),
    (5, 'Comprador puntual.', '2026-08-10 13:05:00', 'VENDEDOR_A_COMPRADOR', 1, 1, 2),
    (5, 'Todo conforme con el libro.', '2026-08-20 15:00:00', 'COMPRADOR_A_VENDEDOR', 6, 1, 4),
    (4, 'Buena comunicacion.', '2026-08-20 15:05:00', 'VENDEDOR_A_COMPRADOR', 6, 4, 1);

-- reporte: 3 registros (cada uno con su subtipo)
INSERT INTO reporte (descripcion, fecha_registro, estado_revision, fecha_revision, id_denunciante, id_revisor) VALUES
    ('La compradora no se presento a la cita de la lampara.', '2026-08-12 13:10:00', 'SANCIONADO', '2026-08-14 10:00:00', 1, 7),   -- 1
    ('Se solicita revisar la informacion de la maqueta.', '2026-09-10 08:00:00', 'DESESTIMADO', '2026-09-11 08:00:00', 2, 7),       -- 2
    ('Se solicita revisar el comportamiento del usuario.', '2026-09-12 15:00:00', 'PENDIENTE', NULL, 3, NULL);                      -- 3

INSERT INTO reporte_usuario (id_reporte, motivo, id_denunciado, id_transaccion) VALUES
    (1, 'NO_SE_PRESENTO_A_LA_CITA', 3, 5),
    (3, 'COMPORTAMIENTO_INAPROPIADO', 4, NULL);

INSERT INTO reporte_anuncio (id_reporte, motivo, id_anuncio) VALUES
    (2, 'INFORMACION_FALSA', 4);

-- insignia: 5 registros
INSERT INTO insignia (nombre, descripcion, icono, tipo, activo) VALUES
    ('Primera Venta', 'Completar al menos una venta.', 'https://example.org/reuse/primera-venta.png', 'VENDEDOR', 1),   -- 1
    ('Vendedor Confiable', 'Completar cinco ventas y obtener promedio minimo de 4.5.', NULL, 'VENDEDOR', 1),            -- 2
    ('Comprador Puntual', 'Completar una compra sin inasistencias sancionadas.', NULL, 'COMPRADOR', 1),                -- 3
    ('Participante ReUse', 'Completar dos transacciones.', NULL, 'GENERAL', 1),                                         -- 4
    ('Insignia retirada', 'Ejemplo de insignia inactiva, sin otorgamientos.', NULL, 'GENERAL', 0);                      -- 5

-- regla_insignia: 7 registros
INSERT INTO regla_insignia (tipo_metrica, operador, valor_objetivo, id_insignia) VALUES
    ('VENTAS_COMPLETADAS', 'MAYOR_O_IGUAL', 1, 1),
    ('VENTAS_COMPLETADAS', 'MAYOR_O_IGUAL', 5, 2),
    ('CALIFICACION_PROMEDIO', 'MAYOR_O_IGUAL', 4.5, 2),
    ('COMPRAS_COMPLETADAS', 'MAYOR_O_IGUAL', 1, 3),
    ('INASISTENCIAS_SANCIONADAS', 'IGUAL', 0, 3),
    ('TRANSACCIONES_COMPLETADAS', 'MAYOR_O_IGUAL', 2, 4),
    ('TRANSACCIONES_COMPLETADAS', 'MAYOR_O_IGUAL', 1, 5);

-- insignia_usuario: 5 registros
INSERT INTO insignia_usuario (fecha_obtencion, id_usuario, id_insignia) VALUES
    ('2026-08-10 12:20:00', 1, 1),
    ('2026-08-20 14:20:00', 4, 1),
    ('2026-08-10 12:20:00', 2, 3),
    ('2026-08-20 14:20:00', 1, 3),
    ('2026-08-20 14:20:00', 1, 4);

COMMIT;

-- =====================================================================
-- 9. PROCEDIMIENTOS ALMACENADOS (los invocan los DAO con CallableStatement)
-- Convencion por tabla x:
--   insertar_x      -> OUT p_id    = LAST_INSERT_ID() (no se envia el id)
--   modificar_x     -> OUT p_filas = ROW_COUNT()
--   eliminar_x      -> borrado logico (activo = 0), OUT p_filas = ROW_COUNT()
--   buscar_x_por_id -> result set con los alias que lee mapear() en Java
--   listar_xs       -> solo registros activos
-- Las fechas con DEFAULT CURRENT_TIMESTAMP no son parametros: las pone la BD.
-- MySQL guarda en cada procedimiento el sql_mode vigente al crearlo; se usa
-- modo estricto para que un dato demasiado largo sea un error (y la capa de
-- negocio haga rollback) en lugar de truncarse en silencio.
-- =====================================================================
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

DELIMITER $$

-- ---------------------------------------------------------------------
-- facultad
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_facultad(
    IN p_nombre VARCHAR(120),
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO facultad (nombre, activo, usuario_creacion)
    VALUES (p_nombre, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_facultad(
    IN p_id INT,
    IN p_nombre VARCHAR(120),
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE facultad
    SET nombre = p_nombre, activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_facultad = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_facultad(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE facultad SET activo = 0 WHERE id_facultad = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_facultad_por_id(IN p_id INT)
BEGIN
    SELECT id_facultad, nombre,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM facultad
    WHERE id_facultad = p_id;
END$$

CREATE PROCEDURE listar_facultades()
BEGIN
    SELECT id_facultad, nombre,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM facultad
    WHERE activo = 1
    ORDER BY nombre;
END$$

CREATE PROCEDURE buscar_facultad_por_nombre(IN p_nombre VARCHAR(120))
BEGIN
    SELECT id_facultad, nombre,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM facultad
    WHERE nombre = p_nombre;
END$$

-- ---------------------------------------------------------------------
-- carrera
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_carrera(
    IN p_nombre VARCHAR(120),
    IN p_id_facultad INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO carrera (nombre, id_facultad, activo, usuario_creacion)
    VALUES (p_nombre, p_id_facultad, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_carrera(
    IN p_id INT,
    IN p_nombre VARCHAR(120),
    IN p_id_facultad INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE carrera
    SET nombre = p_nombre, id_facultad = p_id_facultad, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_carrera = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_carrera(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE carrera SET activo = 0 WHERE id_carrera = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_carrera_por_id(IN p_id INT)
BEGIN
    SELECT c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre,
           c.activo, c.fecha_creacion, c.fecha_modificacion, c.usuario_creacion, c.usuario_modificacion
    FROM carrera c
    JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE c.id_carrera = p_id;
END$$

CREATE PROCEDURE listar_carreras()
BEGIN
    SELECT c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre,
           c.activo, c.fecha_creacion, c.fecha_modificacion, c.usuario_creacion, c.usuario_modificacion
    FROM carrera c
    JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE c.activo = 1
    ORDER BY c.nombre;
END$$

CREATE PROCEDURE buscar_carrera_por_nombre(IN p_nombre VARCHAR(120))
BEGIN
    SELECT c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre,
           c.activo, c.fecha_creacion, c.fecha_modificacion, c.usuario_creacion, c.usuario_modificacion
    FROM carrera c
    JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE c.nombre = p_nombre;
END$$

CREATE PROCEDURE listar_carreras_por_facultad(IN p_id_facultad INT)
BEGIN
    SELECT c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre,
           c.activo, c.fecha_creacion, c.fecha_modificacion, c.usuario_creacion, c.usuario_modificacion
    FROM carrera c
    JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE c.id_facultad = p_id_facultad AND c.activo = 1
    ORDER BY c.nombre;
END$$

-- ---------------------------------------------------------------------
-- usuario (reputacion, contador_reportes y fecha_registro no son
-- parametros: los asigna la BD o los mantienen procedimientos propios)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_usuario(
    IN p_codigo_pucp CHAR(8),
    IN p_nombres VARCHAR(100),
    IN p_apellido_paterno VARCHAR(60),
    IN p_apellido_materno VARCHAR(60),
    IN p_correo_institucional VARCHAR(150),
    IN p_contrasena VARCHAR(255),
    IN p_id_carrera INT,
    IN p_verificado TINYINT(1),
    IN p_estado_cuenta VARCHAR(25),
    IN p_foto_perfil VARCHAR(500),
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO usuario (codigo_pucp, nombres, apellido_paterno, apellido_materno, correo_institucional,
                         contrasena, id_carrera, verificado, estado_cuenta, foto_perfil, activo,
                         usuario_creacion)
    VALUES (p_codigo_pucp, p_nombres, p_apellido_paterno, p_apellido_materno, p_correo_institucional,
            p_contrasena, p_id_carrera, p_verificado, p_estado_cuenta, p_foto_perfil, p_activo,
            p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_usuario(
    IN p_id INT,
    IN p_codigo_pucp CHAR(8),
    IN p_nombres VARCHAR(100),
    IN p_apellido_paterno VARCHAR(60),
    IN p_apellido_materno VARCHAR(60),
    IN p_correo_institucional VARCHAR(150),
    IN p_contrasena VARCHAR(255),
    IN p_id_carrera INT,
    IN p_verificado TINYINT(1),
    IN p_estado_cuenta VARCHAR(25),
    IN p_foto_perfil VARCHAR(500),
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE usuario
    SET codigo_pucp = p_codigo_pucp, nombres = p_nombres, apellido_paterno = p_apellido_paterno,
        apellido_materno = p_apellido_materno, correo_institucional = p_correo_institucional,
        contrasena = p_contrasena, id_carrera = p_id_carrera, verificado = p_verificado,
        estado_cuenta = p_estado_cuenta, foto_perfil = p_foto_perfil, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_usuario = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_usuario(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE usuario SET activo = 0 WHERE id_usuario = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_usuario_por_id(IN p_id INT)
BEGIN
    SELECT u.id_usuario, u.codigo_pucp, u.nombres, u.apellido_paterno, u.apellido_materno,
           u.correo_institucional, u.contrasena, u.id_carrera, u.verificado, u.estado_cuenta,
           u.reputacion, u.contador_reportes, u.foto_perfil, u.fecha_registro,
           c.nombre AS carrera_nombre, c.id_facultad, f.nombre AS facultad_nombre,
           u.activo, u.fecha_creacion, u.fecha_modificacion, u.usuario_creacion, u.usuario_modificacion
    FROM usuario u
    LEFT JOIN carrera c ON c.id_carrera = u.id_carrera
    LEFT JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE u.id_usuario = p_id;
END$$

CREATE PROCEDURE listar_usuarios()
BEGIN
    SELECT u.id_usuario, u.codigo_pucp, u.nombres, u.apellido_paterno, u.apellido_materno,
           u.correo_institucional, u.contrasena, u.id_carrera, u.verificado, u.estado_cuenta,
           u.reputacion, u.contador_reportes, u.foto_perfil, u.fecha_registro,
           c.nombre AS carrera_nombre, c.id_facultad, f.nombre AS facultad_nombre,
           u.activo, u.fecha_creacion, u.fecha_modificacion, u.usuario_creacion, u.usuario_modificacion
    FROM usuario u
    LEFT JOIN carrera c ON c.id_carrera = u.id_carrera
    LEFT JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE u.activo = 1
    ORDER BY u.apellido_paterno, u.nombres;
END$$

CREATE PROCEDURE buscar_usuario_por_correo(IN p_correo_institucional VARCHAR(150))
BEGIN
    SELECT u.id_usuario, u.codigo_pucp, u.nombres, u.apellido_paterno, u.apellido_materno,
           u.correo_institucional, u.contrasena, u.id_carrera, u.verificado, u.estado_cuenta,
           u.reputacion, u.contador_reportes, u.foto_perfil, u.fecha_registro,
           c.nombre AS carrera_nombre, c.id_facultad, f.nombre AS facultad_nombre,
           u.activo, u.fecha_creacion, u.fecha_modificacion, u.usuario_creacion, u.usuario_modificacion
    FROM usuario u
    LEFT JOIN carrera c ON c.id_carrera = u.id_carrera
    LEFT JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE u.correo_institucional = p_correo_institucional;
END$$

CREATE PROCEDURE buscar_usuario_por_codigo(IN p_codigo_pucp CHAR(8))
BEGIN
    SELECT u.id_usuario, u.codigo_pucp, u.nombres, u.apellido_paterno, u.apellido_materno,
           u.correo_institucional, u.contrasena, u.id_carrera, u.verificado, u.estado_cuenta,
           u.reputacion, u.contador_reportes, u.foto_perfil, u.fecha_registro,
           c.nombre AS carrera_nombre, c.id_facultad, f.nombre AS facultad_nombre,
           u.activo, u.fecha_creacion, u.fecha_modificacion, u.usuario_creacion, u.usuario_modificacion
    FROM usuario u
    LEFT JOIN carrera c ON c.id_carrera = u.id_carrera
    LEFT JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE u.codigo_pucp = p_codigo_pucp;
END$$

CREATE PROCEDURE actualizar_estado_cuenta(IN p_id INT, IN p_estado_cuenta VARCHAR(25), OUT p_filas INT)
BEGIN
    UPDATE usuario SET estado_cuenta = p_estado_cuenta WHERE id_usuario = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE actualizar_contador_reportes(IN p_id INT, IN p_variacion INT, OUT p_filas INT)
BEGIN
    UPDATE usuario
    SET contador_reportes = GREATEST(contador_reportes + p_variacion, 0)
    WHERE id_usuario = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- RF-18: reputacion = promedio de las calificaciones activas recibidas.
CREATE PROCEDURE recalcular_reputacion(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE usuario
    SET reputacion = (SELECT COALESCE(ROUND(AVG(c.puntaje), 2), 0)
                      FROM calificacion c
                      WHERE c.id_calificado = p_id AND c.activo = 1)
    WHERE id_usuario = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- RF-18: valor real de una metrica de insignia para un usuario.
CREATE PROCEDURE obtener_metrica_usuario(IN p_id INT, IN p_metrica VARCHAR(40), OUT p_valor DOUBLE)
BEGIN
    SET p_valor = 0;
    IF p_metrica = 'VENTAS_COMPLETADAS' THEN
        SELECT COUNT(*) INTO p_valor
        FROM transaccion t
        JOIN anuncio a ON a.id_anuncio = t.id_anuncio
        WHERE a.id_vendedor = p_id AND t.estado = 'COMPLETADA' AND t.activo = 1;
    ELSEIF p_metrica = 'COMPRAS_COMPLETADAS' THEN
        SELECT COUNT(*) INTO p_valor
        FROM transaccion t
        WHERE t.id_comprador = p_id AND t.estado = 'COMPLETADA' AND t.activo = 1;
    ELSEIF p_metrica = 'TRANSACCIONES_COMPLETADAS' THEN
        SELECT COUNT(*) INTO p_valor
        FROM transaccion t
        JOIN anuncio a ON a.id_anuncio = t.id_anuncio
        WHERE (a.id_vendedor = p_id OR t.id_comprador = p_id)
          AND t.estado = 'COMPLETADA' AND t.activo = 1;
    ELSEIF p_metrica = 'CALIFICACION_PROMEDIO' THEN
        SELECT COALESCE(AVG(c.puntaje), 0) INTO p_valor
        FROM calificacion c
        WHERE c.id_calificado = p_id AND c.activo = 1;
    ELSEIF p_metrica = 'REPORTES_SANCIONADOS' THEN
        SELECT COUNT(*) INTO p_valor
        FROM reporte r
        JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
        WHERE ru.id_denunciado = p_id AND r.estado_revision = 'SANCIONADO' AND r.activo = 1;
    ELSEIF p_metrica = 'INASISTENCIAS_SANCIONADAS' THEN
        SELECT COUNT(*) INTO p_valor
        FROM reporte r
        JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
        WHERE ru.id_denunciado = p_id AND r.estado_revision = 'SANCIONADO' AND r.activo = 1
          AND ru.motivo = 'NO_SE_PRESENTO_A_LA_CITA';
    END IF;
END$$

-- ---------------------------------------------------------------------
-- administrador (subtipo de usuario: el DAO llama primero a insertar_usuario)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_administrador(
    IN p_id_usuario INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50))
BEGIN
    INSERT INTO administrador (id_usuario, activo, usuario_creacion)
    VALUES (p_id_usuario, p_activo, p_usuario_creacion);
END$$

CREATE PROCEDURE modificar_administrador(
    IN p_id_usuario INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE administrador
    SET activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_usuario = p_id_usuario;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_administrador(IN p_id_usuario INT, OUT p_filas INT)
BEGIN
    UPDATE administrador SET activo = 0 WHERE id_usuario = p_id_usuario;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_administrador_por_id(IN p_id INT)
BEGIN
    SELECT u.id_usuario, u.codigo_pucp, u.nombres, u.apellido_paterno, u.apellido_materno,
           u.correo_institucional, u.contrasena, u.id_carrera, u.verificado, u.estado_cuenta,
           u.reputacion, u.contador_reportes, u.foto_perfil, u.fecha_registro,
           c.nombre AS carrera_nombre, c.id_facultad, f.nombre AS facultad_nombre,
           u.activo, u.fecha_creacion, u.fecha_modificacion, u.usuario_creacion, u.usuario_modificacion
    FROM usuario u
    LEFT JOIN carrera c ON c.id_carrera = u.id_carrera
    LEFT JOIN facultad f ON f.id_facultad = c.id_facultad
    JOIN administrador a ON a.id_usuario = u.id_usuario
    WHERE u.id_usuario = p_id;
END$$

CREATE PROCEDURE listar_administradores()
BEGIN
    SELECT u.id_usuario, u.codigo_pucp, u.nombres, u.apellido_paterno, u.apellido_materno,
           u.correo_institucional, u.contrasena, u.id_carrera, u.verificado, u.estado_cuenta,
           u.reputacion, u.contador_reportes, u.foto_perfil, u.fecha_registro,
           c.nombre AS carrera_nombre, c.id_facultad, f.nombre AS facultad_nombre,
           u.activo, u.fecha_creacion, u.fecha_modificacion, u.usuario_creacion, u.usuario_modificacion
    FROM usuario u
    LEFT JOIN carrera c ON c.id_carrera = u.id_carrera
    LEFT JOIN facultad f ON f.id_facultad = c.id_facultad
    JOIN administrador a ON a.id_usuario = u.id_usuario
    WHERE a.activo = 1
    ORDER BY u.apellido_paterno, u.nombres;
END$$

-- ---------------------------------------------------------------------
-- categoria_material
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_categoria_material(
    IN p_nombre VARCHAR(80),
    IN p_descripcion VARCHAR(300),
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO categoria_material (nombre, descripcion, activo, usuario_creacion)
    VALUES (p_nombre, p_descripcion, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_categoria_material(
    IN p_id INT,
    IN p_nombre VARCHAR(80),
    IN p_descripcion VARCHAR(300),
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE categoria_material
    SET nombre = p_nombre, descripcion = p_descripcion, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_categoria = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_categoria_material(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE categoria_material SET activo = 0 WHERE id_categoria = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_categoria_material_por_id(IN p_id INT)
BEGIN
    SELECT id_categoria, nombre, descripcion,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM categoria_material
    WHERE id_categoria = p_id;
END$$

CREATE PROCEDURE listar_categorias_material()
BEGIN
    SELECT id_categoria, nombre, descripcion,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM categoria_material
    WHERE activo = 1
    ORDER BY nombre;
END$$

CREATE PROCEDURE buscar_categoria_material_por_nombre(IN p_nombre VARCHAR(80))
BEGIN
    SELECT id_categoria, nombre, descripcion,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM categoria_material
    WHERE nombre = p_nombre;
END$$

-- ---------------------------------------------------------------------
-- material_academico y material_carrera (N:M)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_material_academico(
    IN p_titulo VARCHAR(200),
    IN p_id_categoria INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO material_academico (titulo, id_categoria, activo, usuario_creacion)
    VALUES (p_titulo, p_id_categoria, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_material_academico(
    IN p_id INT,
    IN p_titulo VARCHAR(200),
    IN p_id_categoria INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE material_academico
    SET titulo = p_titulo, id_categoria = p_id_categoria, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_material = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_material_academico(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE material_academico SET activo = 0 WHERE id_material = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_material_academico_por_id(IN p_id INT)
BEGIN
    SELECT m.id_material, m.titulo, m.id_categoria, cm.nombre AS categoria_nombre,
           m.activo, m.fecha_creacion, m.fecha_modificacion, m.usuario_creacion, m.usuario_modificacion
    FROM material_academico m
    JOIN categoria_material cm ON cm.id_categoria = m.id_categoria
    WHERE m.id_material = p_id;
END$$

CREATE PROCEDURE listar_materiales_academicos()
BEGIN
    SELECT m.id_material, m.titulo, m.id_categoria, cm.nombre AS categoria_nombre,
           m.activo, m.fecha_creacion, m.fecha_modificacion, m.usuario_creacion, m.usuario_modificacion
    FROM material_academico m
    JOIN categoria_material cm ON cm.id_categoria = m.id_categoria
    WHERE m.activo = 1
    ORDER BY m.titulo;
END$$

-- Si la fila ya existia (desactivada), se reactiva en lugar de duplicar la PK compuesta.
CREATE PROCEDURE guardar_material_carrera(
    IN p_id_material INT,
    IN p_id_carrera INT,
    IN p_usuario VARCHAR(50))
BEGIN
    INSERT INTO material_carrera (id_material, id_carrera, activo, usuario_creacion)
    VALUES (p_id_material, p_id_carrera, 1, p_usuario)
    ON DUPLICATE KEY UPDATE activo = 1, usuario_modificacion = p_usuario;
END$$

CREATE PROCEDURE desactivar_carreras_material(IN p_id_material INT)
BEGIN
    UPDATE material_carrera SET activo = 0 WHERE id_material = p_id_material;
END$$

CREATE PROCEDURE listar_carreras_por_material(IN p_id_material INT)
BEGIN
    SELECT mc.id_material, c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre
    FROM material_carrera mc
    JOIN carrera c ON c.id_carrera = mc.id_carrera
    JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE mc.activo = 1 AND mc.id_material = p_id_material
    ORDER BY c.nombre;
END$$

-- Carreras de todos los materiales en una sola consulta (evita N+1 llamadas al listar).
CREATE PROCEDURE listar_carreras_de_materiales()
BEGIN
    SELECT mc.id_material, c.id_carrera, c.nombre, c.id_facultad, f.nombre AS facultad_nombre
    FROM material_carrera mc
    JOIN carrera c ON c.id_carrera = mc.id_carrera
    JOIN facultad f ON f.id_facultad = c.id_facultad
    WHERE mc.activo = 1
    ORDER BY c.nombre;
END$$

-- ---------------------------------------------------------------------
-- anuncio (fecha_publicacion la pone la BD; el vendedor no se modifica)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_anuncio(
    IN p_titulo VARCHAR(150),
    IN p_precio DECIMAL(10,2),
    IN p_descripcion TEXT,
    IN p_condicion VARCHAR(20),
    IN p_estado VARCHAR(20),
    IN p_id_vendedor INT,
    IN p_id_material INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO anuncio (titulo, precio, descripcion, condicion, estado, id_vendedor, id_material,
                         activo, usuario_creacion)
    VALUES (p_titulo, p_precio, p_descripcion, p_condicion, p_estado, p_id_vendedor, p_id_material,
            p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_anuncio(
    IN p_id INT,
    IN p_titulo VARCHAR(150),
    IN p_precio DECIMAL(10,2),
    IN p_descripcion TEXT,
    IN p_condicion VARCHAR(20),
    IN p_estado VARCHAR(20),
    IN p_id_material INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE anuncio
    SET titulo = p_titulo, precio = p_precio, descripcion = p_descripcion, condicion = p_condicion,
        estado = p_estado, id_material = p_id_material, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_anuncio = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_anuncio(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE anuncio SET activo = 0 WHERE id_anuncio = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_anuncio_por_id(IN p_id INT)
BEGIN
    SELECT a.id_anuncio, a.titulo, a.precio, a.descripcion, a.condicion, a.estado, a.fecha_publicacion,
           v.id_usuario AS vendedor_id, v.codigo_pucp AS vendedor_codigo,
           v.nombres AS vendedor_nombres, v.apellido_paterno AS vendedor_apellido,
           a.id_material, m.titulo AS material_titulo,
           a.activo, a.fecha_creacion, a.fecha_modificacion, a.usuario_creacion, a.usuario_modificacion
    FROM anuncio a
    JOIN usuario v ON v.id_usuario = a.id_vendedor
    JOIN material_academico m ON m.id_material = a.id_material
    WHERE a.id_anuncio = p_id;
END$$

CREATE PROCEDURE listar_anuncios()
BEGIN
    SELECT a.id_anuncio, a.titulo, a.precio, a.descripcion, a.condicion, a.estado, a.fecha_publicacion,
           v.id_usuario AS vendedor_id, v.codigo_pucp AS vendedor_codigo,
           v.nombres AS vendedor_nombres, v.apellido_paterno AS vendedor_apellido,
           a.id_material, m.titulo AS material_titulo,
           a.activo, a.fecha_creacion, a.fecha_modificacion, a.usuario_creacion, a.usuario_modificacion
    FROM anuncio a
    JOIN usuario v ON v.id_usuario = a.id_vendedor
    JOIN material_academico m ON m.id_material = a.id_material
    WHERE a.activo = 1
    ORDER BY a.fecha_publicacion DESC;
END$$

-- Control optimista: solo cambia si el anuncio sigue en p_estado_actual (0 filas si no).
CREATE PROCEDURE cambiar_estado_anuncio(
    IN p_id INT,
    IN p_estado_actual VARCHAR(20),
    IN p_estado_nuevo VARCHAR(20),
    OUT p_filas INT)
BEGIN
    UPDATE anuncio SET estado = p_estado_nuevo WHERE id_anuncio = p_id AND estado = p_estado_actual;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE contar_transacciones_anuncio(IN p_id INT, OUT p_total INT)
BEGIN
    SELECT COUNT(*) INTO p_total FROM transaccion WHERE id_anuncio = p_id AND activo = 1;
END$$

-- ---------------------------------------------------------------------
-- imagen_producto
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_imagen_producto(
    IN p_url VARCHAR(500),
    IN p_peso_bytes BIGINT,
    IN p_formato VARCHAR(10),
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO imagen_producto (url, peso_bytes, formato, id_anuncio, activo, usuario_creacion)
    VALUES (p_url, p_peso_bytes, p_formato, p_id_anuncio, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_imagen_producto(
    IN p_id INT,
    IN p_url VARCHAR(500),
    IN p_peso_bytes BIGINT,
    IN p_formato VARCHAR(10),
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE imagen_producto
    SET url = p_url, peso_bytes = p_peso_bytes, formato = p_formato, id_anuncio = p_id_anuncio,
        activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_imagen = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_imagen_producto(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE imagen_producto SET activo = 0 WHERE id_imagen = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_imagen_producto_por_id(IN p_id INT)
BEGIN
    SELECT i.id_imagen, i.url, i.peso_bytes, i.formato,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           i.activo, i.fecha_creacion, i.fecha_modificacion, i.usuario_creacion, i.usuario_modificacion
    FROM imagen_producto i
    JOIN anuncio a ON a.id_anuncio = i.id_anuncio
    WHERE i.id_imagen = p_id;
END$$

CREATE PROCEDURE listar_imagenes_producto()
BEGIN
    SELECT i.id_imagen, i.url, i.peso_bytes, i.formato,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           i.activo, i.fecha_creacion, i.fecha_modificacion, i.usuario_creacion, i.usuario_modificacion
    FROM imagen_producto i
    JOIN anuncio a ON a.id_anuncio = i.id_anuncio
    WHERE i.activo = 1
    ORDER BY i.id_anuncio, i.id_imagen;
END$$

CREATE PROCEDURE listar_imagenes_por_anuncio(IN p_id_anuncio INT)
BEGIN
    SELECT i.id_imagen, i.url, i.peso_bytes, i.formato,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           i.activo, i.fecha_creacion, i.fecha_modificacion, i.usuario_creacion, i.usuario_modificacion
    FROM imagen_producto i
    JOIN anuncio a ON a.id_anuncio = i.id_anuncio
    WHERE i.id_anuncio = p_id_anuncio AND i.activo = 1
    ORDER BY i.id_imagen;
END$$

CREATE PROCEDURE eliminar_imagenes_por_anuncio(IN p_id_anuncio INT, OUT p_filas INT)
BEGIN
    UPDATE imagen_producto SET activo = 0 WHERE id_anuncio = p_id_anuncio;
    SET p_filas = ROW_COUNT();
END$$

-- ---------------------------------------------------------------------
-- favorito (fecha_guardado la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_favorito(
    IN p_id_usuario INT,
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO favorito (id_usuario, id_anuncio, activo, usuario_creacion)
    VALUES (p_id_usuario, p_id_anuncio, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_favorito(
    IN p_id INT,
    IN p_id_usuario INT,
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE favorito
    SET id_usuario = p_id_usuario, id_anuncio = p_id_anuncio, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_favorito = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_favorito(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE favorito SET activo = 0 WHERE id_favorito = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_favorito_por_id(IN p_id INT)
BEGIN
    SELECT fa.id_favorito, fa.fecha_guardado,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           fa.activo, fa.fecha_creacion, fa.fecha_modificacion, fa.usuario_creacion, fa.usuario_modificacion
    FROM favorito fa
    JOIN usuario u ON u.id_usuario = fa.id_usuario
    JOIN anuncio a ON a.id_anuncio = fa.id_anuncio
    WHERE fa.id_favorito = p_id;
END$$

CREATE PROCEDURE listar_favoritos()
BEGIN
    SELECT fa.id_favorito, fa.fecha_guardado,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           fa.activo, fa.fecha_creacion, fa.fecha_modificacion, fa.usuario_creacion, fa.usuario_modificacion
    FROM favorito fa
    JOIN usuario u ON u.id_usuario = fa.id_usuario
    JOIN anuncio a ON a.id_anuncio = fa.id_anuncio
    WHERE fa.activo = 1
    ORDER BY fa.fecha_guardado DESC;
END$$

CREATE PROCEDURE buscar_favorito_activo(IN p_id_usuario INT, IN p_id_anuncio INT)
BEGIN
    SELECT fa.id_favorito, fa.fecha_guardado,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           fa.activo, fa.fecha_creacion, fa.fecha_modificacion, fa.usuario_creacion, fa.usuario_modificacion
    FROM favorito fa
    JOIN usuario u ON u.id_usuario = fa.id_usuario
    JOIN anuncio a ON a.id_anuncio = fa.id_anuncio
    WHERE fa.id_usuario = p_id_usuario AND fa.id_anuncio = p_id_anuncio AND fa.activo = 1;
END$$

CREATE PROCEDURE listar_favoritos_por_usuario(IN p_id_usuario INT)
BEGIN
    SELECT fa.id_favorito, fa.fecha_guardado,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           fa.activo, fa.fecha_creacion, fa.fecha_modificacion, fa.usuario_creacion, fa.usuario_modificacion
    FROM favorito fa
    JOIN usuario u ON u.id_usuario = fa.id_usuario
    JOIN anuncio a ON a.id_anuncio = fa.id_anuncio
    WHERE fa.id_usuario = p_id_usuario AND fa.activo = 1
    ORDER BY fa.fecha_guardado DESC;
END$$

-- ---------------------------------------------------------------------
-- oferta (la fecha la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_oferta(
    IN p_monto_propuesto DECIMAL(10,2),
    IN p_estado VARCHAR(20),
    IN p_id_comprador INT,
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO oferta (monto_propuesto, estado, id_comprador, id_anuncio, activo, usuario_creacion)
    VALUES (p_monto_propuesto, p_estado, p_id_comprador, p_id_anuncio, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_oferta(
    IN p_id INT,
    IN p_monto_propuesto DECIMAL(10,2),
    IN p_estado VARCHAR(20),
    IN p_id_comprador INT,
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE oferta
    SET monto_propuesto = p_monto_propuesto, estado = p_estado, id_comprador = p_id_comprador,
        id_anuncio = p_id_anuncio, activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_oferta = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_oferta(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE oferta SET activo = 0 WHERE id_oferta = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_oferta_por_id(IN p_id INT)
BEGIN
    SELECT o.id_oferta, o.monto_propuesto, o.estado, o.fecha,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           o.activo, o.fecha_creacion, o.fecha_modificacion, o.usuario_creacion, o.usuario_modificacion
    FROM oferta o
    JOIN usuario u ON u.id_usuario = o.id_comprador
    JOIN anuncio a ON a.id_anuncio = o.id_anuncio
    WHERE o.id_oferta = p_id;
END$$

CREATE PROCEDURE listar_ofertas()
BEGIN
    SELECT o.id_oferta, o.monto_propuesto, o.estado, o.fecha,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           o.activo, o.fecha_creacion, o.fecha_modificacion, o.usuario_creacion, o.usuario_modificacion
    FROM oferta o
    JOIN usuario u ON u.id_usuario = o.id_comprador
    JOIN anuncio a ON a.id_anuncio = o.id_anuncio
    WHERE o.activo = 1
    ORDER BY o.fecha DESC;
END$$

CREATE PROCEDURE cambiar_estado_oferta(IN p_id INT, IN p_estado VARCHAR(20), OUT p_filas INT)
BEGIN
    UPDATE oferta SET estado = p_estado WHERE id_oferta = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- Al aceptar una oferta se rechazan las demas ofertas pendientes del anuncio.
CREATE PROCEDURE rechazar_ofertas_pendientes(
    IN p_id_anuncio INT,
    IN p_id_oferta_aceptada INT,
    OUT p_filas INT)
BEGIN
    UPDATE oferta SET estado = 'RECHAZADA'
    WHERE id_anuncio = p_id_anuncio AND id_oferta <> p_id_oferta_aceptada
      AND estado = 'PENDIENTE' AND activo = 1;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE listar_ofertas_por_anuncio(IN p_id_anuncio INT)
BEGIN
    SELECT o.id_oferta, o.monto_propuesto, o.estado, o.fecha,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           o.activo, o.fecha_creacion, o.fecha_modificacion, o.usuario_creacion, o.usuario_modificacion
    FROM oferta o
    JOIN usuario u ON u.id_usuario = o.id_comprador
    JOIN anuncio a ON a.id_anuncio = o.id_anuncio
    WHERE o.id_anuncio = p_id_anuncio AND o.activo = 1
    ORDER BY o.fecha DESC;
END$$

-- ---------------------------------------------------------------------
-- transaccion (fecha_inicio la pone la BD; fecha_fin solo finalizar_transaccion)
-- La cita se trae con LEFT JOIN: cita_entrega.id_transaccion es UNIQUE.
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_transaccion(
    IN p_estado VARCHAR(20),
    IN p_confirmacion_comprador TINYINT(1),
    IN p_confirmacion_vendedor TINYINT(1),
    IN p_id_anuncio INT,
    IN p_id_comprador INT,
    IN p_id_oferta INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO transaccion (estado, confirmacion_comprador, confirmacion_vendedor, id_anuncio,
                             id_comprador, id_oferta, activo, usuario_creacion)
    VALUES (p_estado, p_confirmacion_comprador, p_confirmacion_vendedor, p_id_anuncio,
            p_id_comprador, p_id_oferta, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_transaccion(
    IN p_id INT,
    IN p_estado VARCHAR(20),
    IN p_confirmacion_comprador TINYINT(1),
    IN p_confirmacion_vendedor TINYINT(1),
    IN p_id_anuncio INT,
    IN p_id_comprador INT,
    IN p_id_oferta INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE transaccion
    SET estado = p_estado, confirmacion_comprador = p_confirmacion_comprador,
        confirmacion_vendedor = p_confirmacion_vendedor, id_anuncio = p_id_anuncio,
        id_comprador = p_id_comprador, id_oferta = p_id_oferta, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_transaccion = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_transaccion(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE transaccion SET activo = 0 WHERE id_transaccion = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_transaccion_por_id(IN p_id INT)
BEGIN
    SELECT t.id_transaccion, t.fecha_inicio, t.fecha_fin, t.estado,
           t.confirmacion_comprador, t.confirmacion_vendedor,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           t.id_oferta, o.monto_propuesto AS oferta_monto, o.estado AS oferta_estado,
           ce.id_cita, ce.fecha_hora AS cita_fecha_hora, ce.estado AS cita_estado,
           t.activo, t.fecha_creacion, t.fecha_modificacion, t.usuario_creacion, t.usuario_modificacion
    FROM transaccion t
    JOIN anuncio a ON a.id_anuncio = t.id_anuncio
    JOIN usuario u ON u.id_usuario = t.id_comprador
    LEFT JOIN oferta o ON o.id_oferta = t.id_oferta
    LEFT JOIN cita_entrega ce ON ce.id_transaccion = t.id_transaccion AND ce.activo = 1
    WHERE t.id_transaccion = p_id;
END$$

CREATE PROCEDURE listar_transacciones()
BEGIN
    SELECT t.id_transaccion, t.fecha_inicio, t.fecha_fin, t.estado,
           t.confirmacion_comprador, t.confirmacion_vendedor,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           t.id_oferta, o.monto_propuesto AS oferta_monto, o.estado AS oferta_estado,
           ce.id_cita, ce.fecha_hora AS cita_fecha_hora, ce.estado AS cita_estado,
           t.activo, t.fecha_creacion, t.fecha_modificacion, t.usuario_creacion, t.usuario_modificacion
    FROM transaccion t
    JOIN anuncio a ON a.id_anuncio = t.id_anuncio
    JOIN usuario u ON u.id_usuario = t.id_comprador
    LEFT JOIN oferta o ON o.id_oferta = t.id_oferta
    LEFT JOIN cita_entrega ce ON ce.id_transaccion = t.id_transaccion AND ce.activo = 1
    WHERE t.activo = 1
    ORDER BY t.fecha_inicio DESC;
END$$

CREATE PROCEDURE cambiar_estado_transaccion(IN p_id INT, IN p_estado VARCHAR(20), OUT p_filas INT)
BEGIN
    UPDATE transaccion SET estado = p_estado WHERE id_transaccion = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- COMPLETADA o CANCELADA; fecha_fin con la hora de la BD.
CREATE PROCEDURE finalizar_transaccion(IN p_id INT, IN p_estado VARCHAR(20), OUT p_filas INT)
BEGIN
    UPDATE transaccion SET estado = p_estado, fecha_fin = CURRENT_TIMESTAMP WHERE id_transaccion = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- RF-11: confirmacion del comprador (p_del_comprador = 1) o del vendedor (0).
CREATE PROCEDURE registrar_confirmacion_transaccion(
    IN p_id INT,
    IN p_del_comprador TINYINT(1),
    OUT p_filas INT)
BEGIN
    IF p_del_comprador = 1 THEN
        UPDATE transaccion SET confirmacion_comprador = 1 WHERE id_transaccion = p_id;
        SET p_filas = ROW_COUNT();
    ELSE
        UPDATE transaccion SET confirmacion_vendedor = 1 WHERE id_transaccion = p_id;
        SET p_filas = ROW_COUNT();
    END IF;
END$$

CREATE PROCEDURE listar_transacciones_por_anuncio(IN p_id_anuncio INT)
BEGIN
    SELECT t.id_transaccion, t.fecha_inicio, t.fecha_fin, t.estado,
           t.confirmacion_comprador, t.confirmacion_vendedor,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           t.id_oferta, o.monto_propuesto AS oferta_monto, o.estado AS oferta_estado,
           ce.id_cita, ce.fecha_hora AS cita_fecha_hora, ce.estado AS cita_estado,
           t.activo, t.fecha_creacion, t.fecha_modificacion, t.usuario_creacion, t.usuario_modificacion
    FROM transaccion t
    JOIN anuncio a ON a.id_anuncio = t.id_anuncio
    JOIN usuario u ON u.id_usuario = t.id_comprador
    LEFT JOIN oferta o ON o.id_oferta = t.id_oferta
    LEFT JOIN cita_entrega ce ON ce.id_transaccion = t.id_transaccion AND ce.activo = 1
    WHERE t.id_anuncio = p_id_anuncio AND t.activo = 1
    ORDER BY t.fecha_inicio;
END$$

-- ---------------------------------------------------------------------
-- punto_entrega
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_punto_entrega(
    IN p_nombre VARCHAR(120),
    IN p_referencia VARCHAR(250),
    IN p_ubicacion VARCHAR(250),
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO punto_entrega (nombre, referencia, ubicacion, activo, usuario_creacion)
    VALUES (p_nombre, p_referencia, p_ubicacion, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_punto_entrega(
    IN p_id INT,
    IN p_nombre VARCHAR(120),
    IN p_referencia VARCHAR(250),
    IN p_ubicacion VARCHAR(250),
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE punto_entrega
    SET nombre = p_nombre, referencia = p_referencia, ubicacion = p_ubicacion, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_punto_entrega = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_punto_entrega(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE punto_entrega SET activo = 0 WHERE id_punto_entrega = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_punto_entrega_por_id(IN p_id INT)
BEGIN
    SELECT id_punto_entrega, nombre, referencia, ubicacion,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM punto_entrega
    WHERE id_punto_entrega = p_id;
END$$

CREATE PROCEDURE listar_puntos_entrega()
BEGIN
    SELECT id_punto_entrega, nombre, referencia, ubicacion,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM punto_entrega
    WHERE activo = 1
    ORDER BY nombre;
END$$

CREATE PROCEDURE buscar_punto_entrega_por_nombre(IN p_nombre VARCHAR(120))
BEGIN
    SELECT id_punto_entrega, nombre, referencia, ubicacion,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM punto_entrega
    WHERE nombre = p_nombre;
END$$

-- ---------------------------------------------------------------------
-- cita_entrega (fecha_hora si es parametro: es la fecha acordada)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_cita_entrega(
    IN p_fecha_hora DATETIME,
    IN p_estado VARCHAR(20),
    IN p_id_punto_entrega INT,
    IN p_id_transaccion INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO cita_entrega (fecha_hora, estado, id_punto_entrega, id_transaccion, activo, usuario_creacion)
    VALUES (p_fecha_hora, p_estado, p_id_punto_entrega, p_id_transaccion, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_cita_entrega(
    IN p_id INT,
    IN p_fecha_hora DATETIME,
    IN p_estado VARCHAR(20),
    IN p_id_punto_entrega INT,
    IN p_id_transaccion INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE cita_entrega
    SET fecha_hora = p_fecha_hora, estado = p_estado, id_punto_entrega = p_id_punto_entrega,
        id_transaccion = p_id_transaccion, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_cita = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_cita_entrega(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE cita_entrega SET activo = 0 WHERE id_cita = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_cita_entrega_por_id(IN p_id INT)
BEGIN
    SELECT ce.id_cita, ce.fecha_hora, ce.estado,
           ce.id_punto_entrega, p.nombre AS punto_nombre, p.activo AS punto_activo,
           ce.id_transaccion, t.estado AS transaccion_estado,
           ce.activo, ce.fecha_creacion, ce.fecha_modificacion, ce.usuario_creacion, ce.usuario_modificacion
    FROM cita_entrega ce
    JOIN punto_entrega p ON p.id_punto_entrega = ce.id_punto_entrega
    JOIN transaccion t ON t.id_transaccion = ce.id_transaccion
    WHERE ce.id_cita = p_id;
END$$

CREATE PROCEDURE listar_citas_entrega()
BEGIN
    SELECT ce.id_cita, ce.fecha_hora, ce.estado,
           ce.id_punto_entrega, p.nombre AS punto_nombre, p.activo AS punto_activo,
           ce.id_transaccion, t.estado AS transaccion_estado,
           ce.activo, ce.fecha_creacion, ce.fecha_modificacion, ce.usuario_creacion, ce.usuario_modificacion
    FROM cita_entrega ce
    JOIN punto_entrega p ON p.id_punto_entrega = ce.id_punto_entrega
    JOIN transaccion t ON t.id_transaccion = ce.id_transaccion
    WHERE ce.activo = 1
    ORDER BY ce.fecha_hora;
END$$

CREATE PROCEDURE buscar_cita_por_transaccion(IN p_id_transaccion INT)
BEGIN
    SELECT ce.id_cita, ce.fecha_hora, ce.estado,
           ce.id_punto_entrega, p.nombre AS punto_nombre, p.activo AS punto_activo,
           ce.id_transaccion, t.estado AS transaccion_estado,
           ce.activo, ce.fecha_creacion, ce.fecha_modificacion, ce.usuario_creacion, ce.usuario_modificacion
    FROM cita_entrega ce
    JOIN punto_entrega p ON p.id_punto_entrega = ce.id_punto_entrega
    JOIN transaccion t ON t.id_transaccion = ce.id_transaccion
    WHERE ce.id_transaccion = p_id_transaccion AND ce.activo = 1;
END$$

CREATE PROCEDURE cambiar_estado_cita(IN p_id INT, IN p_estado VARCHAR(20), OUT p_filas INT)
BEGIN
    UPDATE cita_entrega SET estado = p_estado WHERE id_cita = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- ---------------------------------------------------------------------
-- canal_chat (fecha_solicitud la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_canal_chat(
    IN p_estado VARCHAR(20),
    IN p_id_anuncio INT,
    IN p_id_comprador INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO canal_chat (estado, id_anuncio, id_comprador, activo, usuario_creacion)
    VALUES (p_estado, p_id_anuncio, p_id_comprador, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_canal_chat(
    IN p_id INT,
    IN p_estado VARCHAR(20),
    IN p_id_anuncio INT,
    IN p_id_comprador INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE canal_chat
    SET estado = p_estado, id_anuncio = p_id_anuncio, id_comprador = p_id_comprador, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_chat = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_canal_chat(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE canal_chat SET activo = 0 WHERE id_chat = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_canal_chat_por_id(IN p_id INT)
BEGIN
    SELECT ch.id_chat, ch.estado, ch.fecha_solicitud, ch.fecha_respuesta, ch.fecha_cierre,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           ch.activo, ch.fecha_creacion, ch.fecha_modificacion, ch.usuario_creacion, ch.usuario_modificacion
    FROM canal_chat ch
    JOIN anuncio a ON a.id_anuncio = ch.id_anuncio
    JOIN usuario u ON u.id_usuario = ch.id_comprador
    WHERE ch.id_chat = p_id;
END$$

CREATE PROCEDURE listar_canales_chat()
BEGIN
    SELECT ch.id_chat, ch.estado, ch.fecha_solicitud, ch.fecha_respuesta, ch.fecha_cierre,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           ch.activo, ch.fecha_creacion, ch.fecha_modificacion, ch.usuario_creacion, ch.usuario_modificacion
    FROM canal_chat ch
    JOIN anuncio a ON a.id_anuncio = ch.id_anuncio
    JOIN usuario u ON u.id_usuario = ch.id_comprador
    WHERE ch.activo = 1
    ORDER BY ch.fecha_solicitud DESC;
END$$

-- RF-09: ACTIVO o RECHAZADO; fecha_respuesta (y fecha_cierre si se rechaza) con la hora de la BD.
CREATE PROCEDURE responder_solicitud_chat(IN p_id INT, IN p_estado VARCHAR(20), OUT p_filas INT)
BEGIN
    IF p_estado = 'RECHAZADO' THEN
        UPDATE canal_chat
        SET estado = p_estado, fecha_respuesta = CURRENT_TIMESTAMP, fecha_cierre = CURRENT_TIMESTAMP
        WHERE id_chat = p_id;
        SET p_filas = ROW_COUNT();
    ELSE
        UPDATE canal_chat
        SET estado = p_estado, fecha_respuesta = CURRENT_TIMESTAMP
        WHERE id_chat = p_id;
        SET p_filas = ROW_COUNT();
    END IF;
END$$

-- CERRADO o BLOQUEADO; fecha_cierre con la hora de la BD.
CREATE PROCEDURE cerrar_canal_chat(IN p_id INT, IN p_estado VARCHAR(20), OUT p_filas INT)
BEGIN
    UPDATE canal_chat SET estado = p_estado, fecha_cierre = CURRENT_TIMESTAMP WHERE id_chat = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- RF-10: bloquea los chats pendientes o activos entre dos usuarios (en ambos sentidos).
CREATE PROCEDURE bloquear_chats_entre(IN p_id_usuario_a INT, IN p_id_usuario_b INT, OUT p_filas INT)
BEGIN
    UPDATE canal_chat ch
    JOIN anuncio a ON a.id_anuncio = ch.id_anuncio
    SET ch.estado = 'BLOQUEADO', ch.fecha_cierre = CURRENT_TIMESTAMP
    WHERE ch.estado IN ('PENDIENTE', 'ACTIVO') AND ch.activo = 1
      AND ((ch.id_comprador = p_id_usuario_a AND a.id_vendedor = p_id_usuario_b)
        OR (ch.id_comprador = p_id_usuario_b AND a.id_vendedor = p_id_usuario_a));
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_chat_abierto(IN p_id_anuncio INT, IN p_id_comprador INT)
BEGIN
    SELECT ch.id_chat, ch.estado, ch.fecha_solicitud, ch.fecha_respuesta, ch.fecha_cierre,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor,
           u.id_usuario AS comprador_id, u.codigo_pucp AS comprador_codigo,
           u.nombres AS comprador_nombres, u.apellido_paterno AS comprador_apellido,
           ch.activo, ch.fecha_creacion, ch.fecha_modificacion, ch.usuario_creacion, ch.usuario_modificacion
    FROM canal_chat ch
    JOIN anuncio a ON a.id_anuncio = ch.id_anuncio
    JOIN usuario u ON u.id_usuario = ch.id_comprador
    WHERE ch.id_anuncio = p_id_anuncio AND ch.id_comprador = p_id_comprador
      AND ch.estado IN ('PENDIENTE', 'ACTIVO') AND ch.activo = 1;
END$$

-- ---------------------------------------------------------------------
-- mensaje (fecha_hora la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_mensaje(
    IN p_contenido TEXT,
    IN p_leido TINYINT(1),
    IN p_id_chat INT,
    IN p_id_emisor INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO mensaje (contenido, leido, id_chat, id_emisor, activo, usuario_creacion)
    VALUES (p_contenido, p_leido, p_id_chat, p_id_emisor, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_mensaje(
    IN p_id INT,
    IN p_contenido TEXT,
    IN p_leido TINYINT(1),
    IN p_id_chat INT,
    IN p_id_emisor INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE mensaje
    SET contenido = p_contenido, leido = p_leido, id_chat = p_id_chat, id_emisor = p_id_emisor,
        activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_mensaje = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_mensaje(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE mensaje SET activo = 0 WHERE id_mensaje = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_mensaje_por_id(IN p_id INT)
BEGIN
    SELECT me.id_mensaje, me.contenido, me.fecha_hora, me.leido,
           me.id_chat, ch.estado AS chat_estado,
           u.id_usuario AS emisor_id, u.codigo_pucp AS emisor_codigo,
           u.nombres AS emisor_nombres, u.apellido_paterno AS emisor_apellido,
           me.activo, me.fecha_creacion, me.fecha_modificacion, me.usuario_creacion, me.usuario_modificacion
    FROM mensaje me
    JOIN canal_chat ch ON ch.id_chat = me.id_chat
    JOIN usuario u ON u.id_usuario = me.id_emisor
    WHERE me.id_mensaje = p_id;
END$$

CREATE PROCEDURE listar_mensajes()
BEGIN
    SELECT me.id_mensaje, me.contenido, me.fecha_hora, me.leido,
           me.id_chat, ch.estado AS chat_estado,
           u.id_usuario AS emisor_id, u.codigo_pucp AS emisor_codigo,
           u.nombres AS emisor_nombres, u.apellido_paterno AS emisor_apellido,
           me.activo, me.fecha_creacion, me.fecha_modificacion, me.usuario_creacion, me.usuario_modificacion
    FROM mensaje me
    JOIN canal_chat ch ON ch.id_chat = me.id_chat
    JOIN usuario u ON u.id_usuario = me.id_emisor
    WHERE me.activo = 1
    ORDER BY me.fecha_hora;
END$$

CREATE PROCEDURE listar_mensajes_por_chat(IN p_id_chat INT)
BEGIN
    SELECT me.id_mensaje, me.contenido, me.fecha_hora, me.leido,
           me.id_chat, ch.estado AS chat_estado,
           u.id_usuario AS emisor_id, u.codigo_pucp AS emisor_codigo,
           u.nombres AS emisor_nombres, u.apellido_paterno AS emisor_apellido,
           me.activo, me.fecha_creacion, me.fecha_modificacion, me.usuario_creacion, me.usuario_modificacion
    FROM mensaje me
    JOIN canal_chat ch ON ch.id_chat = me.id_chat
    JOIN usuario u ON u.id_usuario = me.id_emisor
    WHERE me.id_chat = p_id_chat AND me.activo = 1
    ORDER BY me.fecha_hora;
END$$

-- ---------------------------------------------------------------------
-- notificacion (fecha_hora la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_notificacion(
    IN p_mensaje VARCHAR(500),
    IN p_tipo VARCHAR(30),
    IN p_estado VARCHAR(20),
    IN p_id_destinatario INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO notificacion (mensaje, tipo, estado, id_destinatario, activo, usuario_creacion)
    VALUES (p_mensaje, p_tipo, p_estado, p_id_destinatario, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_notificacion(
    IN p_id INT,
    IN p_mensaje VARCHAR(500),
    IN p_tipo VARCHAR(30),
    IN p_estado VARCHAR(20),
    IN p_id_destinatario INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE notificacion
    SET mensaje = p_mensaje, tipo = p_tipo, estado = p_estado, id_destinatario = p_id_destinatario,
        activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_notificacion = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_notificacion(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE notificacion SET activo = 0 WHERE id_notificacion = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_notificacion_por_id(IN p_id INT)
BEGIN
    SELECT n.id_notificacion, n.mensaje, n.fecha_hora, n.tipo, n.estado,
           u.id_usuario AS destinatario_id, u.codigo_pucp AS destinatario_codigo,
           u.nombres AS destinatario_nombres, u.apellido_paterno AS destinatario_apellido,
           n.activo, n.fecha_creacion, n.fecha_modificacion, n.usuario_creacion, n.usuario_modificacion
    FROM notificacion n
    JOIN usuario u ON u.id_usuario = n.id_destinatario
    WHERE n.id_notificacion = p_id;
END$$

CREATE PROCEDURE listar_notificaciones()
BEGIN
    SELECT n.id_notificacion, n.mensaje, n.fecha_hora, n.tipo, n.estado,
           u.id_usuario AS destinatario_id, u.codigo_pucp AS destinatario_codigo,
           u.nombres AS destinatario_nombres, u.apellido_paterno AS destinatario_apellido,
           n.activo, n.fecha_creacion, n.fecha_modificacion, n.usuario_creacion, n.usuario_modificacion
    FROM notificacion n
    JOIN usuario u ON u.id_usuario = n.id_destinatario
    WHERE n.activo = 1
    ORDER BY n.fecha_hora DESC;
END$$

CREATE PROCEDURE listar_notificaciones_por_destinatario(IN p_id_destinatario INT)
BEGIN
    SELECT n.id_notificacion, n.mensaje, n.fecha_hora, n.tipo, n.estado,
           u.id_usuario AS destinatario_id, u.codigo_pucp AS destinatario_codigo,
           u.nombres AS destinatario_nombres, u.apellido_paterno AS destinatario_apellido,
           n.activo, n.fecha_creacion, n.fecha_modificacion, n.usuario_creacion, n.usuario_modificacion
    FROM notificacion n
    JOIN usuario u ON u.id_usuario = n.id_destinatario
    WHERE n.id_destinatario = p_id_destinatario AND n.activo = 1
    ORDER BY n.fecha_hora DESC;
END$$

-- ---------------------------------------------------------------------
-- respuesta_rapida
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_respuesta_rapida(
    IN p_texto VARCHAR(500),
    IN p_id_creador INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO respuesta_rapida (texto, id_creador, activo, usuario_creacion)
    VALUES (p_texto, p_id_creador, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_respuesta_rapida(
    IN p_id INT,
    IN p_texto VARCHAR(500),
    IN p_id_creador INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE respuesta_rapida
    SET texto = p_texto, id_creador = p_id_creador, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_respuesta = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_respuesta_rapida(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE respuesta_rapida SET activo = 0 WHERE id_respuesta = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_respuesta_rapida_por_id(IN p_id INT)
BEGIN
    SELECT rr.id_respuesta, rr.texto,
           u.id_usuario AS creador_id, u.codigo_pucp AS creador_codigo,
           u.nombres AS creador_nombres, u.apellido_paterno AS creador_apellido,
           rr.activo, rr.fecha_creacion, rr.fecha_modificacion, rr.usuario_creacion, rr.usuario_modificacion
    FROM respuesta_rapida rr
    JOIN usuario u ON u.id_usuario = rr.id_creador
    WHERE rr.id_respuesta = p_id;
END$$

CREATE PROCEDURE listar_respuestas_rapidas()
BEGIN
    SELECT rr.id_respuesta, rr.texto,
           u.id_usuario AS creador_id, u.codigo_pucp AS creador_codigo,
           u.nombres AS creador_nombres, u.apellido_paterno AS creador_apellido,
           rr.activo, rr.fecha_creacion, rr.fecha_modificacion, rr.usuario_creacion, rr.usuario_modificacion
    FROM respuesta_rapida rr
    JOIN usuario u ON u.id_usuario = rr.id_creador
    WHERE rr.activo = 1
    ORDER BY rr.id_creador, rr.id_respuesta;
END$$

CREATE PROCEDURE listar_respuestas_rapidas_por_creador(IN p_id_creador INT)
BEGIN
    SELECT rr.id_respuesta, rr.texto,
           u.id_usuario AS creador_id, u.codigo_pucp AS creador_codigo,
           u.nombres AS creador_nombres, u.apellido_paterno AS creador_apellido,
           rr.activo, rr.fecha_creacion, rr.fecha_modificacion, rr.usuario_creacion, rr.usuario_modificacion
    FROM respuesta_rapida rr
    JOIN usuario u ON u.id_usuario = rr.id_creador
    WHERE rr.id_creador = p_id_creador AND rr.activo = 1
    ORDER BY rr.id_respuesta;
END$$

-- ---------------------------------------------------------------------
-- bloqueo (la fecha la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_bloqueo(
    IN p_estado VARCHAR(20),
    IN p_id_bloqueador INT,
    IN p_id_bloqueado INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO bloqueo (estado, id_bloqueador, id_bloqueado, activo, usuario_creacion)
    VALUES (p_estado, p_id_bloqueador, p_id_bloqueado, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_bloqueo(
    IN p_id INT,
    IN p_estado VARCHAR(20),
    IN p_id_bloqueador INT,
    IN p_id_bloqueado INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE bloqueo
    SET estado = p_estado, id_bloqueador = p_id_bloqueador, id_bloqueado = p_id_bloqueado,
        activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_bloqueo = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_bloqueo(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE bloqueo SET activo = 0 WHERE id_bloqueo = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_bloqueo_por_id(IN p_id INT)
BEGIN
    SELECT b.id_bloqueo, b.fecha, b.estado,
           ur.id_usuario AS bloqueador_id, ur.codigo_pucp AS bloqueador_codigo,
           ur.nombres AS bloqueador_nombres, ur.apellido_paterno AS bloqueador_apellido,
           ud.id_usuario AS bloqueado_id, ud.codigo_pucp AS bloqueado_codigo,
           ud.nombres AS bloqueado_nombres, ud.apellido_paterno AS bloqueado_apellido,
           b.activo, b.fecha_creacion, b.fecha_modificacion, b.usuario_creacion, b.usuario_modificacion
    FROM bloqueo b
    JOIN usuario ur ON ur.id_usuario = b.id_bloqueador
    JOIN usuario ud ON ud.id_usuario = b.id_bloqueado
    WHERE b.id_bloqueo = p_id;
END$$

CREATE PROCEDURE listar_bloqueos()
BEGIN
    SELECT b.id_bloqueo, b.fecha, b.estado,
           ur.id_usuario AS bloqueador_id, ur.codigo_pucp AS bloqueador_codigo,
           ur.nombres AS bloqueador_nombres, ur.apellido_paterno AS bloqueador_apellido,
           ud.id_usuario AS bloqueado_id, ud.codigo_pucp AS bloqueado_codigo,
           ud.nombres AS bloqueado_nombres, ud.apellido_paterno AS bloqueado_apellido,
           b.activo, b.fecha_creacion, b.fecha_modificacion, b.usuario_creacion, b.usuario_modificacion
    FROM bloqueo b
    JOIN usuario ur ON ur.id_usuario = b.id_bloqueador
    JOIN usuario ud ON ud.id_usuario = b.id_bloqueado
    WHERE b.activo = 1
    ORDER BY b.fecha DESC;
END$$

-- RF-10: el bloqueo es bidireccional.
CREATE PROCEDURE buscar_bloqueo_activo_entre(IN p_id_usuario_a INT, IN p_id_usuario_b INT)
BEGIN
    SELECT b.id_bloqueo, b.fecha, b.estado,
           ur.id_usuario AS bloqueador_id, ur.codigo_pucp AS bloqueador_codigo,
           ur.nombres AS bloqueador_nombres, ur.apellido_paterno AS bloqueador_apellido,
           ud.id_usuario AS bloqueado_id, ud.codigo_pucp AS bloqueado_codigo,
           ud.nombres AS bloqueado_nombres, ud.apellido_paterno AS bloqueado_apellido,
           b.activo, b.fecha_creacion, b.fecha_modificacion, b.usuario_creacion, b.usuario_modificacion
    FROM bloqueo b
    JOIN usuario ur ON ur.id_usuario = b.id_bloqueador
    JOIN usuario ud ON ud.id_usuario = b.id_bloqueado
    WHERE b.estado = 'ACTIVO' AND b.activo = 1
      AND ((b.id_bloqueador = p_id_usuario_a AND b.id_bloqueado = p_id_usuario_b)
        OR (b.id_bloqueador = p_id_usuario_b AND b.id_bloqueado = p_id_usuario_a));
END$$

-- ---------------------------------------------------------------------
-- calificacion (la fecha la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_calificacion(
    IN p_puntaje TINYINT,
    IN p_comentario VARCHAR(500),
    IN p_tipo VARCHAR(30),
    IN p_id_transaccion INT,
    IN p_id_calificador INT,
    IN p_id_calificado INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO calificacion (puntaje, comentario, tipo, id_transaccion, id_calificador, id_calificado,
                              activo, usuario_creacion)
    VALUES (p_puntaje, p_comentario, p_tipo, p_id_transaccion, p_id_calificador, p_id_calificado,
            p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_calificacion(
    IN p_id INT,
    IN p_puntaje TINYINT,
    IN p_comentario VARCHAR(500),
    IN p_tipo VARCHAR(30),
    IN p_id_transaccion INT,
    IN p_id_calificador INT,
    IN p_id_calificado INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE calificacion
    SET puntaje = p_puntaje, comentario = p_comentario, tipo = p_tipo, id_transaccion = p_id_transaccion,
        id_calificador = p_id_calificador, id_calificado = p_id_calificado, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_calificacion = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_calificacion(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE calificacion SET activo = 0 WHERE id_calificacion = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_calificacion_por_id(IN p_id INT)
BEGIN
    SELECT ca.id_calificacion, ca.puntaje, ca.comentario, ca.fecha, ca.tipo,
           ca.id_transaccion, t.estado AS transaccion_estado,
           cr.id_usuario AS calificador_id, cr.codigo_pucp AS calificador_codigo,
           cr.nombres AS calificador_nombres, cr.apellido_paterno AS calificador_apellido,
           cd.id_usuario AS calificado_id, cd.codigo_pucp AS calificado_codigo,
           cd.nombres AS calificado_nombres, cd.apellido_paterno AS calificado_apellido,
           ca.activo, ca.fecha_creacion, ca.fecha_modificacion, ca.usuario_creacion, ca.usuario_modificacion
    FROM calificacion ca
    JOIN transaccion t ON t.id_transaccion = ca.id_transaccion
    JOIN usuario cr ON cr.id_usuario = ca.id_calificador
    JOIN usuario cd ON cd.id_usuario = ca.id_calificado
    WHERE ca.id_calificacion = p_id;
END$$

CREATE PROCEDURE listar_calificaciones()
BEGIN
    SELECT ca.id_calificacion, ca.puntaje, ca.comentario, ca.fecha, ca.tipo,
           ca.id_transaccion, t.estado AS transaccion_estado,
           cr.id_usuario AS calificador_id, cr.codigo_pucp AS calificador_codigo,
           cr.nombres AS calificador_nombres, cr.apellido_paterno AS calificador_apellido,
           cd.id_usuario AS calificado_id, cd.codigo_pucp AS calificado_codigo,
           cd.nombres AS calificado_nombres, cd.apellido_paterno AS calificado_apellido,
           ca.activo, ca.fecha_creacion, ca.fecha_modificacion, ca.usuario_creacion, ca.usuario_modificacion
    FROM calificacion ca
    JOIN transaccion t ON t.id_transaccion = ca.id_transaccion
    JOIN usuario cr ON cr.id_usuario = ca.id_calificador
    JOIN usuario cd ON cd.id_usuario = ca.id_calificado
    WHERE ca.activo = 1
    ORDER BY ca.fecha DESC;
END$$

CREATE PROCEDURE buscar_calificacion_por_transaccion_y_calificador(
    IN p_id_transaccion INT,
    IN p_id_calificador INT)
BEGIN
    SELECT ca.id_calificacion, ca.puntaje, ca.comentario, ca.fecha, ca.tipo,
           ca.id_transaccion, t.estado AS transaccion_estado,
           cr.id_usuario AS calificador_id, cr.codigo_pucp AS calificador_codigo,
           cr.nombres AS calificador_nombres, cr.apellido_paterno AS calificador_apellido,
           cd.id_usuario AS calificado_id, cd.codigo_pucp AS calificado_codigo,
           cd.nombres AS calificado_nombres, cd.apellido_paterno AS calificado_apellido,
           ca.activo, ca.fecha_creacion, ca.fecha_modificacion, ca.usuario_creacion, ca.usuario_modificacion
    FROM calificacion ca
    JOIN transaccion t ON t.id_transaccion = ca.id_transaccion
    JOIN usuario cr ON cr.id_usuario = ca.id_calificador
    JOIN usuario cd ON cd.id_usuario = ca.id_calificado
    WHERE ca.id_transaccion = p_id_transaccion AND ca.id_calificador = p_id_calificador AND ca.activo = 1;
END$$

-- ---------------------------------------------------------------------
-- reporte (supertipo): el DAO de cada subtipo llama primero a insertar_reporte
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_reporte(
    IN p_descripcion TEXT,
    IN p_estado_revision VARCHAR(20),
    IN p_id_denunciante INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO reporte (descripcion, estado_revision, id_denunciante, activo, usuario_creacion)
    VALUES (p_descripcion, p_estado_revision, p_id_denunciante, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

-- El estado de revision no se modifica aqui: solo cambia con resolver_reporte.
CREATE PROCEDURE modificar_reporte(
    IN p_id INT,
    IN p_descripcion TEXT,
    IN p_id_denunciante INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE reporte
    SET descripcion = p_descripcion, id_denunciante = p_id_denunciante, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_reporte = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_reporte(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE reporte SET activo = 0 WHERE id_reporte = p_id;
    SET p_filas = ROW_COUNT();
END$$

-- Sanciona o desestima un reporte PENDIENTE (0 filas si ya estaba resuelto).
CREATE PROCEDURE resolver_reporte(
    IN p_id INT,
    IN p_estado_revision VARCHAR(20),
    IN p_id_revisor INT,
    OUT p_filas INT)
BEGIN
    UPDATE reporte
    SET estado_revision = p_estado_revision, fecha_revision = CURRENT_TIMESTAMP, id_revisor = p_id_revisor
    WHERE id_reporte = p_id AND estado_revision = 'PENDIENTE';
    SET p_filas = ROW_COUNT();
END$$

-- ---------------------------------------------------------------------
-- reporte_usuario
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_reporte_usuario(
    IN p_id_reporte INT,
    IN p_motivo VARCHAR(40),
    IN p_id_denunciado INT,
    IN p_id_transaccion INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50))
BEGIN
    INSERT INTO reporte_usuario (id_reporte, motivo, id_denunciado, id_transaccion, activo, usuario_creacion)
    VALUES (p_id_reporte, p_motivo, p_id_denunciado, p_id_transaccion, p_activo, p_usuario_creacion);
END$$

CREATE PROCEDURE modificar_reporte_usuario(
    IN p_id_reporte INT,
    IN p_motivo VARCHAR(40),
    IN p_id_denunciado INT,
    IN p_id_transaccion INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE reporte_usuario
    SET motivo = p_motivo, id_denunciado = p_id_denunciado, id_transaccion = p_id_transaccion,
        activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_reporte = p_id_reporte;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_reporte_usuario(IN p_id_reporte INT, OUT p_filas INT)
BEGIN
    UPDATE reporte_usuario SET activo = 0 WHERE id_reporte = p_id_reporte;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_reporte_usuario_por_id(IN p_id INT)
BEGIN
    SELECT r.id_reporte, r.descripcion, r.fecha_registro, r.estado_revision, r.fecha_revision,
           d.id_usuario AS denunciante_id, d.codigo_pucp AS denunciante_codigo,
           d.nombres AS denunciante_nombres, d.apellido_paterno AS denunciante_apellido,
           rv.id_usuario AS revisor_id, rv.codigo_pucp AS revisor_codigo,
           rv.nombres AS revisor_nombres, rv.apellido_paterno AS revisor_apellido,
           r.activo, r.fecha_creacion, r.fecha_modificacion, r.usuario_creacion, r.usuario_modificacion,
           ru.motivo, ru.id_transaccion,
           dn.id_usuario AS denunciado_id, dn.codigo_pucp AS denunciado_codigo,
           dn.nombres AS denunciado_nombres, dn.apellido_paterno AS denunciado_apellido
    FROM reporte r
    JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
    JOIN usuario d ON d.id_usuario = r.id_denunciante
    LEFT JOIN usuario rv ON rv.id_usuario = r.id_revisor
    JOIN usuario dn ON dn.id_usuario = ru.id_denunciado
    WHERE r.id_reporte = p_id;
END$$

CREATE PROCEDURE listar_reportes_usuario()
BEGIN
    SELECT r.id_reporte, r.descripcion, r.fecha_registro, r.estado_revision, r.fecha_revision,
           d.id_usuario AS denunciante_id, d.codigo_pucp AS denunciante_codigo,
           d.nombres AS denunciante_nombres, d.apellido_paterno AS denunciante_apellido,
           rv.id_usuario AS revisor_id, rv.codigo_pucp AS revisor_codigo,
           rv.nombres AS revisor_nombres, rv.apellido_paterno AS revisor_apellido,
           r.activo, r.fecha_creacion, r.fecha_modificacion, r.usuario_creacion, r.usuario_modificacion,
           ru.motivo, ru.id_transaccion,
           dn.id_usuario AS denunciado_id, dn.codigo_pucp AS denunciado_codigo,
           dn.nombres AS denunciado_nombres, dn.apellido_paterno AS denunciado_apellido
    FROM reporte r
    JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
    JOIN usuario d ON d.id_usuario = r.id_denunciante
    LEFT JOIN usuario rv ON rv.id_usuario = r.id_revisor
    JOIN usuario dn ON dn.id_usuario = ru.id_denunciado
    WHERE r.activo = 1
    ORDER BY r.fecha_registro DESC;
END$$

-- RF-13: sanciones recibidas por un usuario en los ultimos p_dias dias.
CREATE PROCEDURE contar_sanciones_recientes(IN p_id_denunciado INT, IN p_dias INT, OUT p_total INT)
BEGIN
    SELECT COUNT(*) INTO p_total
    FROM reporte r
    JOIN reporte_usuario ru ON ru.id_reporte = r.id_reporte
    WHERE ru.id_denunciado = p_id_denunciado AND r.estado_revision = 'SANCIONADO' AND r.activo = 1
      AND r.fecha_revision >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL p_dias DAY);
END$$

-- ---------------------------------------------------------------------
-- reporte_anuncio
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_reporte_anuncio(
    IN p_id_reporte INT,
    IN p_motivo VARCHAR(40),
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50))
BEGIN
    INSERT INTO reporte_anuncio (id_reporte, motivo, id_anuncio, activo, usuario_creacion)
    VALUES (p_id_reporte, p_motivo, p_id_anuncio, p_activo, p_usuario_creacion);
END$$

CREATE PROCEDURE modificar_reporte_anuncio(
    IN p_id_reporte INT,
    IN p_motivo VARCHAR(40),
    IN p_id_anuncio INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE reporte_anuncio
    SET motivo = p_motivo, id_anuncio = p_id_anuncio, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_reporte = p_id_reporte;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_reporte_anuncio(IN p_id_reporte INT, OUT p_filas INT)
BEGIN
    UPDATE reporte_anuncio SET activo = 0 WHERE id_reporte = p_id_reporte;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_reporte_anuncio_por_id(IN p_id INT)
BEGIN
    SELECT r.id_reporte, r.descripcion, r.fecha_registro, r.estado_revision, r.fecha_revision,
           d.id_usuario AS denunciante_id, d.codigo_pucp AS denunciante_codigo,
           d.nombres AS denunciante_nombres, d.apellido_paterno AS denunciante_apellido,
           rv.id_usuario AS revisor_id, rv.codigo_pucp AS revisor_codigo,
           rv.nombres AS revisor_nombres, rv.apellido_paterno AS revisor_apellido,
           r.activo, r.fecha_creacion, r.fecha_modificacion, r.usuario_creacion, r.usuario_modificacion,
           ra.motivo,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor
    FROM reporte r
    JOIN reporte_anuncio ra ON ra.id_reporte = r.id_reporte
    JOIN usuario d ON d.id_usuario = r.id_denunciante
    LEFT JOIN usuario rv ON rv.id_usuario = r.id_revisor
    JOIN anuncio a ON a.id_anuncio = ra.id_anuncio
    WHERE r.id_reporte = p_id;
END$$

CREATE PROCEDURE listar_reportes_anuncio()
BEGIN
    SELECT r.id_reporte, r.descripcion, r.fecha_registro, r.estado_revision, r.fecha_revision,
           d.id_usuario AS denunciante_id, d.codigo_pucp AS denunciante_codigo,
           d.nombres AS denunciante_nombres, d.apellido_paterno AS denunciante_apellido,
           rv.id_usuario AS revisor_id, rv.codigo_pucp AS revisor_codigo,
           rv.nombres AS revisor_nombres, rv.apellido_paterno AS revisor_apellido,
           r.activo, r.fecha_creacion, r.fecha_modificacion, r.usuario_creacion, r.usuario_modificacion,
           ra.motivo,
           a.id_anuncio AS anuncio_id, a.titulo AS anuncio_titulo, a.precio AS anuncio_precio,
           a.estado AS anuncio_estado, a.id_vendedor AS anuncio_id_vendedor
    FROM reporte r
    JOIN reporte_anuncio ra ON ra.id_reporte = r.id_reporte
    JOIN usuario d ON d.id_usuario = r.id_denunciante
    LEFT JOIN usuario rv ON rv.id_usuario = r.id_revisor
    JOIN anuncio a ON a.id_anuncio = ra.id_anuncio
    WHERE r.activo = 1
    ORDER BY r.fecha_registro DESC;
END$$

-- ---------------------------------------------------------------------
-- insignia
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_insignia(
    IN p_nombre VARCHAR(120),
    IN p_descripcion VARCHAR(400),
    IN p_icono VARCHAR(500),
    IN p_tipo VARCHAR(20),
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO insignia (nombre, descripcion, icono, tipo, activo, usuario_creacion)
    VALUES (p_nombre, p_descripcion, p_icono, p_tipo, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_insignia(
    IN p_id INT,
    IN p_nombre VARCHAR(120),
    IN p_descripcion VARCHAR(400),
    IN p_icono VARCHAR(500),
    IN p_tipo VARCHAR(20),
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE insignia
    SET nombre = p_nombre, descripcion = p_descripcion, icono = p_icono, tipo = p_tipo, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_insignia = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_insignia(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE insignia SET activo = 0 WHERE id_insignia = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_insignia_por_id(IN p_id INT)
BEGIN
    SELECT id_insignia, nombre, descripcion, icono, tipo,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM insignia
    WHERE id_insignia = p_id;
END$$

CREATE PROCEDURE listar_insignias()
BEGIN
    SELECT id_insignia, nombre, descripcion, icono, tipo,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM insignia
    WHERE activo = 1
    ORDER BY nombre;
END$$

CREATE PROCEDURE buscar_insignia_por_nombre(IN p_nombre VARCHAR(120))
BEGIN
    SELECT id_insignia, nombre, descripcion, icono, tipo,
           activo, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion
    FROM insignia
    WHERE nombre = p_nombre;
END$$

-- ---------------------------------------------------------------------
-- regla_insignia
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_regla_insignia(
    IN p_tipo_metrica VARCHAR(40),
    IN p_operador VARCHAR(20),
    IN p_valor_objetivo DECIMAL(10,2),
    IN p_id_insignia INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO regla_insignia (tipo_metrica, operador, valor_objetivo, id_insignia, activo, usuario_creacion)
    VALUES (p_tipo_metrica, p_operador, p_valor_objetivo, p_id_insignia, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_regla_insignia(
    IN p_id INT,
    IN p_tipo_metrica VARCHAR(40),
    IN p_operador VARCHAR(20),
    IN p_valor_objetivo DECIMAL(10,2),
    IN p_id_insignia INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE regla_insignia
    SET tipo_metrica = p_tipo_metrica, operador = p_operador, valor_objetivo = p_valor_objetivo,
        id_insignia = p_id_insignia, activo = p_activo, usuario_modificacion = p_usuario_modificacion
    WHERE id_regla = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_regla_insignia(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE regla_insignia SET activo = 0 WHERE id_regla = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_regla_insignia_por_id(IN p_id INT)
BEGIN
    SELECT ri.id_regla, ri.tipo_metrica, ri.operador, ri.valor_objetivo,
           ri.id_insignia, i.nombre AS insignia_nombre,
           ri.activo, ri.fecha_creacion, ri.fecha_modificacion, ri.usuario_creacion, ri.usuario_modificacion
    FROM regla_insignia ri
    JOIN insignia i ON i.id_insignia = ri.id_insignia
    WHERE ri.id_regla = p_id;
END$$

CREATE PROCEDURE listar_reglas_insignia()
BEGIN
    SELECT ri.id_regla, ri.tipo_metrica, ri.operador, ri.valor_objetivo,
           ri.id_insignia, i.nombre AS insignia_nombre,
           ri.activo, ri.fecha_creacion, ri.fecha_modificacion, ri.usuario_creacion, ri.usuario_modificacion
    FROM regla_insignia ri
    JOIN insignia i ON i.id_insignia = ri.id_insignia
    WHERE ri.activo = 1
    ORDER BY ri.id_insignia, ri.id_regla;
END$$

CREATE PROCEDURE listar_reglas_por_insignia(IN p_id_insignia INT)
BEGIN
    SELECT ri.id_regla, ri.tipo_metrica, ri.operador, ri.valor_objetivo,
           ri.id_insignia, i.nombre AS insignia_nombre,
           ri.activo, ri.fecha_creacion, ri.fecha_modificacion, ri.usuario_creacion, ri.usuario_modificacion
    FROM regla_insignia ri
    JOIN insignia i ON i.id_insignia = ri.id_insignia
    WHERE ri.id_insignia = p_id_insignia AND ri.activo = 1
    ORDER BY ri.id_regla;
END$$

-- ---------------------------------------------------------------------
-- insignia_usuario (fecha_obtencion la pone la BD)
-- ---------------------------------------------------------------------
CREATE PROCEDURE insertar_insignia_usuario(
    IN p_id_usuario INT,
    IN p_id_insignia INT,
    IN p_activo TINYINT(1),
    IN p_usuario_creacion VARCHAR(50),
    OUT p_id INT)
BEGIN
    INSERT INTO insignia_usuario (id_usuario, id_insignia, activo, usuario_creacion)
    VALUES (p_id_usuario, p_id_insignia, p_activo, p_usuario_creacion);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_insignia_usuario(
    IN p_id INT,
    IN p_id_usuario INT,
    IN p_id_insignia INT,
    IN p_activo TINYINT(1),
    IN p_usuario_modificacion VARCHAR(50),
    OUT p_filas INT)
BEGIN
    UPDATE insignia_usuario
    SET id_usuario = p_id_usuario, id_insignia = p_id_insignia, activo = p_activo,
        usuario_modificacion = p_usuario_modificacion
    WHERE id_insignia_usuario = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE eliminar_insignia_usuario(IN p_id INT, OUT p_filas INT)
BEGIN
    UPDATE insignia_usuario SET activo = 0 WHERE id_insignia_usuario = p_id;
    SET p_filas = ROW_COUNT();
END$$

CREATE PROCEDURE buscar_insignia_usuario_por_id(IN p_id INT)
BEGIN
    SELECT iu.id_insignia_usuario, iu.fecha_obtencion,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           iu.id_insignia, i.nombre AS insignia_nombre, i.tipo AS insignia_tipo,
           iu.activo, iu.fecha_creacion, iu.fecha_modificacion, iu.usuario_creacion, iu.usuario_modificacion
    FROM insignia_usuario iu
    JOIN usuario u ON u.id_usuario = iu.id_usuario
    JOIN insignia i ON i.id_insignia = iu.id_insignia
    WHERE iu.id_insignia_usuario = p_id;
END$$

CREATE PROCEDURE listar_insignias_usuario()
BEGIN
    SELECT iu.id_insignia_usuario, iu.fecha_obtencion,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           iu.id_insignia, i.nombre AS insignia_nombre, i.tipo AS insignia_tipo,
           iu.activo, iu.fecha_creacion, iu.fecha_modificacion, iu.usuario_creacion, iu.usuario_modificacion
    FROM insignia_usuario iu
    JOIN usuario u ON u.id_usuario = iu.id_usuario
    JOIN insignia i ON i.id_insignia = iu.id_insignia
    WHERE iu.activo = 1
    ORDER BY iu.fecha_obtencion DESC;
END$$

-- Devuelve el otorgamiento aunque este inactivo (la pareja usuario-insignia es UNIQUE).
CREATE PROCEDURE buscar_insignia_usuario_por_usuario_e_insignia(IN p_id_usuario INT, IN p_id_insignia INT)
BEGIN
    SELECT iu.id_insignia_usuario, iu.fecha_obtencion,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           iu.id_insignia, i.nombre AS insignia_nombre, i.tipo AS insignia_tipo,
           iu.activo, iu.fecha_creacion, iu.fecha_modificacion, iu.usuario_creacion, iu.usuario_modificacion
    FROM insignia_usuario iu
    JOIN usuario u ON u.id_usuario = iu.id_usuario
    JOIN insignia i ON i.id_insignia = iu.id_insignia
    WHERE iu.id_usuario = p_id_usuario AND iu.id_insignia = p_id_insignia;
END$$

CREATE PROCEDURE listar_insignias_por_usuario(IN p_id_usuario INT)
BEGIN
    SELECT iu.id_insignia_usuario, iu.fecha_obtencion,
           u.id_usuario AS usuario_id, u.codigo_pucp AS usuario_codigo,
           u.nombres AS usuario_nombres, u.apellido_paterno AS usuario_apellido,
           iu.id_insignia, i.nombre AS insignia_nombre, i.tipo AS insignia_tipo,
           iu.activo, iu.fecha_creacion, iu.fecha_modificacion, iu.usuario_creacion, iu.usuario_modificacion
    FROM insignia_usuario iu
    JOIN usuario u ON u.id_usuario = iu.id_usuario
    JOIN insignia i ON i.id_insignia = iu.id_insignia
    WHERE iu.id_usuario = p_id_usuario AND iu.activo = 1
    ORDER BY iu.fecha_obtencion;
END$$

DELIMITER ;
