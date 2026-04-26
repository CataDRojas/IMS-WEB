-- =========================================
-- V1__init_schema.sql
-- PURE STRUCTURE ONLY (NO BUSINESS RULES)
-- =========================================

CREATE TABLE Configuracion (
    ConfiguracionId TINYINT PRIMARY KEY,
    EmpresaNombre VARCHAR(150) NOT NULL,
    EmpresaDireccion VARCHAR(255) NOT NULL,
    EmpresaRun VARCHAR(20),
    EmpresaDV VARCHAR(5),
    IVA DECIMAL(5,2) NOT NULL
);

CREATE TABLE permisos (
    PermisosId BIGINT AUTO_INCREMENT PRIMARY KEY,
    PermisosNombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Rol (
    RolId BIGINT AUTO_INCREMENT PRIMARY KEY,
    RolNombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE RolPermisos (
    RolId BIGINT NOT NULL,
    PermisosId BIGINT NOT NULL,
    PRIMARY KEY (RolId, PermisosId),
    FOREIGN KEY (RolId) REFERENCES Rol(RolId),
    FOREIGN KEY (PermisosId) REFERENCES permisos(PermisosId)
);

CREATE TABLE Descuento (
    DescuentoId BIGINT AUTO_INCREMENT PRIMARY KEY,
    DescuentoNombre VARCHAR(100) NOT NULL,
    DescuentoTipo VARCHAR(20) NOT NULL,
    DescuentoValor DECIMAL(12,2) NOT NULL,
    DescuentoValorSecundario DECIMAL(12,2),
    DescuentoActivo BOOLEAN NOT NULL DEFAULT TRUE,

    DescuentoUsuarioCreacion VARCHAR(100) NOT NULL,
    DescuentoFechaCreacion DATETIME NOT NULL,
    DescuentoUsuarioModif VARCHAR(100),
    DescuentoFechaModif DATETIME
);

CREATE TABLE Categoria (
    CategoriaId BIGINT AUTO_INCREMENT PRIMARY KEY,
    CategoriaNombre VARCHAR(100) NOT NULL,
    DescuentoId BIGINT,

    CategoriaUsuarioCreacion VARCHAR(100) NOT NULL,
    CategoriaFechaCreacion DATETIME NOT NULL,
    CategoriaUsuarioModif VARCHAR(100),
    CategoriaFechaModif DATETIME,

    FOREIGN KEY (DescuentoId) REFERENCES Descuento(DescuentoId)
);

CREATE TABLE productos (
    ProductoId BIGINT AUTO_INCREMENT PRIMARY KEY,
    ProductoNombre VARCHAR(100) NOT NULL,
    ProductoDesc VARCHAR(255),
    ProductoActivo BOOLEAN NOT NULL DEFAULT TRUE,
    ProductoStock INT NOT NULL DEFAULT 0,
    ProductoStockCritico BOOLEAN NOT NULL DEFAULT FALSE,
    ProductoCriticoNumero INT NOT NULL DEFAULT 0,
    ProductoPrecio DECIMAL(12,2) NOT NULL,
    ProductoCantidadLote INT,
    ProductoCodigo VARCHAR(100) NOT NULL UNIQUE,

    ProductoCategoria BIGINT,
    DescuentoId BIGINT,

    ProductosUsuarioCreacion VARCHAR(100) NOT NULL,
    ProductosFechaCreacion DATETIME NOT NULL,
    ProductosUsuarioModif VARCHAR(100),
    ProductosFechaModif DATETIME,

    FOREIGN KEY (ProductoCategoria) REFERENCES Categoria(CategoriaId),
    FOREIGN KEY (DescuentoId) REFERENCES Descuento(DescuentoId)
);

CREATE TABLE usuarios (
    UsuarioId BIGINT AUTO_INCREMENT PRIMARY KEY,
    UsuarioEmail VARCHAR(150) NOT NULL UNIQUE,
    UsuarioNombre VARCHAR(100) NOT NULL,
    UsuarioRun VARCHAR(20),
    UsuarioDV VARCHAR(5),
    UsuarioPassword VARCHAR(255) NOT NULL,
    UsuarioFechaCreacion DATETIME NOT NULL,
    UsuarioFechaModif DATETIME,
    RolId BIGINT NOT NULL,
    UsuarioActivo BOOLEAN NOT NULL DEFAULT TRUE,

    FOREIGN KEY (RolId) REFERENCES Rol(RolId)
);

CREATE TABLE Movimiento (
    MovimientoId BIGINT AUTO_INCREMENT PRIMARY KEY,

    MovimientoDescripcion VARCHAR(255),
    MovimientoEstado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    MovimientoTipo VARCHAR(50) NOT NULL,
    MovimientoMetodoPago VARCHAR(50),

    MovimientoStock INT DEFAULT 0,
    MovimientoPrecioBase DECIMAL(12,2) DEFAULT 0,
    MovimientoPrecioNeto DECIMAL(12,2) DEFAULT 0,
    MovimientoPrecioTotal DECIMAL(12,2) DEFAULT 0,
    MovimientoDescuento DECIMAL(12,2),

    MovimientoReferenciaExterna VARCHAR(100) UNIQUE,

    MovimientoUsuarioCreacion VARCHAR(100) NOT NULL,
    MovimientoFechaCreacion DATETIME NOT NULL,
    MovimientoUsuarioModif VARCHAR(100),
    MovimientoFechaModif DATETIME
);

CREATE TABLE MovimientoLugar (
    MovimientoLugarId BIGINT AUTO_INCREMENT PRIMARY KEY,
    MovimientoLugarDescripcion VARCHAR(100) NOT NULL,
    MovimientoLugarActivo BOOLEAN NOT NULL DEFAULT TRUE,

    MovimientoLugarUsuarioCreacion VARCHAR(100) NOT NULL,
    MovimientoLugarFechaCreacion DATETIME NOT NULL,
    MovimientoLugarUsuarioModif VARCHAR(100),
    MovimientoLugarFechaModif DATETIME
);

CREATE TABLE MovimientoDetalle (
    MovimientoDetalleId BIGINT AUTO_INCREMENT PRIMARY KEY,
    MovimientoId BIGINT NOT NULL,
    ProductoId BIGINT NOT NULL,
    MovimientoLugarId BIGINT,

    MovimientoDetalleCantidad INT NOT NULL,
    MovimientoDetalleUnidadesPorPaquete INT NOT NULL DEFAULT 1,

    MovimientoDetallePrecioBase DECIMAL(12,2) ,
    MovimientoDetalleDescuentoAplicado DECIMAL(12,2),
    MovimientoDetallePrecioUnitario DECIMAL(12,2),
    MovimientoDetallePrecioTotal DECIMAL(12,2),
    MovimientoDetalleDescripcion VARCHAR(255),

    FOREIGN KEY (MovimientoId) REFERENCES Movimiento(MovimientoId),
    FOREIGN KEY (ProductoId) REFERENCES productos(ProductoId),
    FOREIGN KEY (MovimientoLugarId) REFERENCES MovimientoLugar(MovimientoLugarId)
);