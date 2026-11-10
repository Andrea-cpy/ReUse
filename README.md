# ReUse — Marketplace de materiales académicos PUCP

Proyecto de Programación 3 (1INF30) · Equipo **Erasmolovers**.

| Carpeta | Qué contiene |
|---|---|
| `BackEnd/` | Proyecto Java (Maven multi-módulo): modelo, conexión, DAOs, negocio, pruebas y el módulo `reuse-rest` con los servicios web RESTful. Ver `BackEnd/README.md`. |
| `FrontEnd/` | Aplicación Blazor (.NET 10) `ReUseWeb` que consume los servicios REST. Ver `FrontEnd/README.md`. |
| `glassfish8/` | Servidor Eclipse GlassFish 8.0.5 que se usa en los laboratorios (sin su caché OSGi ni logs, que se regeneran al arrancar). |
| `Dependencias/` | Repositorio Maven local que entrega el laboratorio (`Dependencias/repository`). |
| `ScriptSQL/` | `reuse_schema.sql`: crea el esquema `reuse_db` completo con procedimientos y datos de prueba. |

## Orden para levantar todo

1. Ejecutar `ScriptSQL/reuse_schema.sql` en MySQL.
2. En `BackEnd/reuse-dbmanager/src/main/resources/`, copiar `db.properties.example` como `db.properties` con los datos de la BD (no se sube al repo).
3. Compilar `BackEnd` y desplegar `reuse-rest` en `glassfish8` → `http://localhost:8080/reuse-rest/api/v1`.
4. Abrir `FrontEnd/ReUseWeb.sln` y ejecutar. Usuarios de prueba: `a20260001@pucp.edu.pe` … `a20260004@pucp.edu.pe` y `admin.demo@pucp.edu.pe`, clave `Demo1234`.
