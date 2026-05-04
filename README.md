# IMS-Web — Inventory Management System Web

Sistema de gestión de inventario y punto de venta web para pequeñas y medianas empresas chilenas. Desarrollado como proyecto de portafolio para el programa **Analista Programador Computacional** de Duoc UC.

---

## Descripción

IMS-Web digitaliza los procesos de inventario que tradicionalmente se manejan con cuadernos o planillas Excel desconectadas, eliminando errores críticos y entregando visibilidad del stock en tiempo real.

El sistema cubre el ciclo completo de inventario: desde la recepción de mercadería, pasando por las ventas con punto de venta, el conteo físico de inventario, hasta la auditoría completa de todos los movimientos, con control de acceso por roles.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | Angular 19 (Standalone Components) + Angular Material |
| Backend | Spring Boot 4 + Spring Security + JWT |
| Base de Datos | MySQL 8 + Flyway (migraciones) |
| ORM | JPA / Hibernate |
| Reportes | Apache POI (exportación Excel) |
| Escaneo | ZXing (códigos de barras) |

---

## Estructura del Proyecto

```
IMS-WEB/
├── backend/          # Spring Boot — API REST
│   └── src/
│       └── main/java/com/ims_web/inventory/
│           ├── controller/     # Endpoints REST
│           ├── service/        # Lógica de negocio
│           ├── entity/         # Entidades JPA
│           ├── dto/            # Data Transfer Objects
│           ├── repository/     # Repositorios JPA
│           ├── util/           # AuditHelper, Excel exporters
│           └── resources/
│               └── db/migration/  # Scripts Flyway V1-V4
└── frontend/         # Angular — SPA
    └── src/app/
        ├── pages/    # Módulos de la aplicación
        ├── services/ # Servicios HTTP
        └── guards/   # Autenticación y permisos
```

---

## Módulos del Sistema

- **Catálogo de Productos** — CRUD de productos con categorías, descuentos, stock crítico y exportación/importación Excel
- **Recepción** — Registro de entrada de mercadería por ubicación física (`ENTRADA`)
- **Inventario** — Conteo físico que ajusta el stock real (`AJUSTE`)
- **Ventas (POS)** — Punto de venta con escaneo de códigos, carrito, descuentos automáticos y confirmación
- **Historial Unificado** — Auditoría completa de todos los movimientos con filtros y exportación Excel
- **Gestión de Roles** — Control de acceso granular por permisos
- **Configuración** — Parámetros del sistema (IVA, datos de empresa)

---

## Requisitos Previos

- Java 21
- Node.js 18+
- MySQL 8
- Angular CLI 19

---

## Instalación y Configuración

### Base de Datos

Configurar las credenciales en `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventario_db
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
```

Al iniciar el backend por primera vez, Spring Boot crea la base de datos automáticamente y Flyway ejecuta los scripts de migración en orden (`V1__init_schema.sql` → `V4__procedures.sql`), dejando el esquema, triggers, constraints y procedures listos sin intervención manual.

---

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

El servidor inicia en `http://localhost:8080`

---

### Frontend

```bash
cd frontend
npm install
ng serve
```

La aplicación queda disponible en `http://localhost:4200`

---

## Credenciales por Defecto

Al iniciar por primera vez, el sistema crea automáticamente los siguientes usuarios de prueba:

| Email | Contraseña | Rol |
|---|---|---|
| admin@ims.cl | 1234 | Administrador (acceso total) |
| carlitos.lechuga@ims.cl | 1234 | Vendedor (ventas) |
| cosme.fulanito@ims.cl | 1234 | Bodeguero (inventario y recepción) |

---

## Arquitectura de Base de Datos

El sistema utiliza **Flyway** para el control de versiones de la BD con 4 scripts de migración:

| Script | Contenido |
|---|---|
| `V1__init_schema.sql` | Estructura de tablas |
| `V2__constraints.sql` | Restricciones y validaciones |
| `V3__triggers.sql` | Triggers de negocio (stock crítico, auditoría, cálculo de precios) |
| `V4__procedures.sql` | Stored procedures (aplicar/revertir stock, recalcular movimiento) |

### Lógica de Stock

El stock se gestiona por ubicación física (`MovimientoLugarProducto`) y se sincroniza automáticamente con el stock total del producto:

- `ENTRADA` → suma stock por ubicación
- `SALIDA` → resta desde ubicación prioritaria
- `AJUSTE` → reemplaza el stock contado

---

## Equipo de Desarrollo

| Nombre | Rol | Responsabilidad |
|---|---|---|
| Catalina Diaz Rojas | Scrum Master / Frontend Lead | UI/UX, diseño responsive, módulo de ventas |
| Javier Salas Leibur | Product Owner / Backend Lead | API REST, seguridad, lógica de negocio |
| Cristóbal Varas Polanco | Arquitecto de Datos e Integración | BD, triggers, procedures, módulos inventario/recepción |

---

## Profesor

**Patricio Oliva Ramírez** — Duoc UC

---

*Proyecto desarrollado en 3 sprints de una semana cada uno, utilizando metodología Scrum.*
