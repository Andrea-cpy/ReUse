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
