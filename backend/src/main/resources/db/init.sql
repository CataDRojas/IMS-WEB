--🧨 SECTION 1 — CLEAN RESET (ALL DROPS)
--SET FOREIGN_KEY_CHECKS = 0;

-- TRIGGERS
--DROP TRIGGER IF EXISTS trg_no_negative_stock;
--DROP TRIGGER IF EXISTS trg_producto_stockcritico_update;
--DROP TRIGGER IF EXISTS trg_detalle_block_after_confirm;
--DROP TRIGGER IF EXISTS trg_detalle_delete_block_after_confirm;
--DROP TRIGGER IF EXISTS trg_detalle_before_insert;
--DROP TRIGGER IF EXISTS trg_categoria_delete_block;
--DROP TRIGGER IF EXISTS trg_configuracion_singleton;
--DROP TRIGGER IF EXISTS trg_detalle_after_insert_recalc;
--DROP TRIGGER IF EXISTS trg_detalle_after_update_recalc;
--DROP TRIGGER IF EXISTS trg_detalle_after_delete_recalc;
--DROP TRIGGER IF EXISTS trg_detalle_calculo_insert;
--DROP TRIGGER IF EXISTS trg_detalle_calculo_update;

--DROP PROCEDURE IF EXISTS sp_recalcular_movimiento;
--DROP PROCEDURE IF EXISTS sp_calcular_descuento_producto;

-- TABLES (ORDER MATTERS)
--DROP TABLE IF EXISTS MovimientoDetalle;
--DROP TABLE IF EXISTS MovimientoLugar;
--DROP TABLE IF EXISTS Movimiento;
--DROP TABLE IF EXISTS usuarios;
--DROP TABLE IF EXISTS productos;
--DROP TABLE IF EXISTS Categoria;
--DROP TABLE IF EXISTS Descuento;
--DROP TABLE IF EXISTS RolPermisos;
--DROP TABLE IF EXISTS Rol;
--DROP TABLE IF EXISTS permisos;
--DROP TABLE IF EXISTS Configuracion;

--SET FOREIGN_KEY_CHECKS = 1;

-- SECTION 2 — TABLES

CREATE TABLE IF NOT EXISTS Configuracion (
    ConfiguracionId TINYINT PRIMARY KEY CHECK (ConfiguracionId = 1),
    EmpresaNombre VARCHAR(150) NOT NULL,
    EmpresaDireccion VARCHAR(255) NOT NULL,
    EmpresaRun VARCHAR(20),
    EmpresaDV VARCHAR(5),
    IVA DECIMAL(5,2) NOT NULL,
    CONSTRAINT chk_iva CHECK (IVA >= 0 AND IVA <= 100)
);

CREATE TABLE IF NOT EXISTS permisos (
    PermisosId BIGINT AUTO_INCREMENT PRIMARY KEY,
    PermisosNombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Rol (
    RolId BIGINT AUTO_INCREMENT PRIMARY KEY,
    RolNombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS RolPermisos (
    RolId BIGINT NOT NULL,
    PermisosId BIGINT NOT NULL,
    PRIMARY KEY (RolId, PermisosId),
    CONSTRAINT fk_rolpermisos_rol FOREIGN KEY (RolId) REFERENCES Rol(RolId),
    CONSTRAINT fk_rolpermisos_permiso FOREIGN KEY (PermisosId) REFERENCES permisos(PermisosId)
);

CREATE TABLE IF NOT EXISTS Descuento (
    DescuentoId BIGINT AUTO_INCREMENT PRIMARY KEY,
    DescuentoNombre VARCHAR(100) NOT NULL,
    DescuentoTipo VARCHAR(20) NOT NULL,
    DescuentoValor DECIMAL(12,2) NOT NULL,
    DescuentoActivo BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit fields
    DescuentoUsuarioCreacion VARCHAR(100) NOT NULL,
    DescuentoFechaCreacion DATETIME NOT NULL,
    DescuentoUsuarioModif VARCHAR(100),
    DescuentoFechaModif DATETIME,

    CONSTRAINT chk_descuento_valor CHECK (
        (DescuentoTipo = 'MULTIPLICATIVO' AND DescuentoValor > 0)
        OR
        (DescuentoTipo = 'FLAT' AND DescuentoValor >= 0)
        OR
        (DescuentoTipo = 'PORCENTAJE' AND DescuentoValor BETWEEN 0 AND 100)
    ),
    CONSTRAINT chk_descuento_tipo CHECK (DescuentoTipo IN ('FLAT', 'PORCENTAJE', 'MULTIPLICATIVO'))
);

CREATE TABLE IF NOT EXISTS Categoria (
    CategoriaId BIGINT AUTO_INCREMENT PRIMARY KEY,
    CategoriaNombre VARCHAR(100) NOT NULL,
    DescuentoId BIGINT,
    CategoriaUsuarioCreacion VARCHAR(100) NOT NULL,
    CategoriaFechaCreacion DATETIME NOT NULL,
    CategoriaUsuarioModif VARCHAR(100),
    CategoriaFechaModif DATETIME,
    CONSTRAINT fk_categoria_descuento FOREIGN KEY (DescuentoId) REFERENCES Descuento(DescuentoId)
);

CREATE INDEX idx_categoria_descuento ON Categoria(DescuentoId);

CREATE TABLE IF NOT EXISTS productos (
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
    CONSTRAINT fk_producto_categoria FOREIGN KEY (ProductoCategoria) REFERENCES Categoria(CategoriaId),
    CONSTRAINT fk_producto_descuento FOREIGN KEY (DescuentoId) REFERENCES Descuento(DescuentoId),
    CONSTRAINT chk_producto_stock CHECK (ProductoStock >= 0),
    CONSTRAINT chk_producto_precio CHECK (ProductoPrecio >= 0)
);

CREATE INDEX idx_producto_categoria ON productos(ProductoCategoria);
CREATE INDEX idx_producto_descuento ON productos(DescuentoId);

-- 👤 USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
    UsuarioId BIGINT AUTO_INCREMENT PRIMARY KEY,
    UsuarioEmail VARCHAR(150) NOT NULL UNIQUE,
    UsuarioNombre VARCHAR(100) NOT NULL,
    UsuarioRun VARCHAR(20),
    UsuarioDV VARCHAR(5),
    UsuarioPassword VARCHAR(255) NOT NULL,
    UsuarioFechaCreacion DATETIME NOT NULL,
    UsuarioFechaModif DATETIME,  -- Added column
    RolId BIGINT NOT NULL,
    UsuarioActivo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (RolId) REFERENCES Rol(RolId) ON DELETE RESTRICT
);

CREATE INDEX idx_usuario_rol ON usuarios(RolId);
CREATE UNIQUE INDEX idx_usuario_email ON usuarios(UsuarioEmail);

-- 🧾 MOVIMIENTO
CREATE TABLE IF NOT EXISTS Movimiento (
    MovimientoId BIGINT AUTO_INCREMENT PRIMARY KEY,
    MovimientoDescripcion VARCHAR(255),
    MovimientoEstado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    MovimientoTipo VARCHAR(50) NOT NULL,
    MovimientoMetodoPago VARCHAR(50),
    MovimientoStock INT NOT NULL DEFAULT 0,
    MovimientoPrecioBase DECIMAL(12,2) NOT NULL,
    MovimientoPrecioNeto DECIMAL(12,2) NOT NULL DEFAULT 0,
    MovimientoPrecioTotal DECIMAL(12,2) NOT NULL,
    MovimientoDescuento DECIMAL(12,2),
    MovimientoReferenciaExterna VARCHAR(100) UNIQUE,
    MovimientoUsuarioCreacion VARCHAR(100) NOT NULL,
    MovimientoFechaCreacion DATETIME NOT NULL,
    MovimientoUsuarioModif VARCHAR(100),
    MovimientoFechaModif DATETIME,
    CONSTRAINT chk_movimiento_tipo CHECK (MovimientoTipo IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    CONSTRAINT chk_movimiento_estado CHECK (MovimientoEstado IN ('PENDIENTE', 'CONFIRMADO', 'ANULADO'))
);

-- 📍 MOVIMIENTO LUGAR
CREATE TABLE IF NOT EXISTS MovimientoLugar (
    MovimientoLugarId BIGINT AUTO_INCREMENT PRIMARY KEY,
    MovimientoLugarDescripcion VARCHAR(100) NOT NULL,
    MovimientoLugarActivo BOOLEAN NOT NULL DEFAULT TRUE,
    MovimientoLugarUsuarioCreacion VARCHAR(100) NOT NULL,
    MovimientoLugarFechaCreacion DATETIME NOT NULL,
    MovimientoLugarUsuarioModif VARCHAR(100),
    MovimientoLugarFechaModif DATETIME
);

-- 📄 MOVIMIENTO DETALLE
CREATE TABLE IF NOT EXISTS MovimientoDetalle (
    MovimientoDetalleId BIGINT AUTO_INCREMENT PRIMARY KEY,
    MovimientoId BIGINT NOT NULL,
    ProductoId BIGINT NOT NULL,
    MovimientoLugarId BIGINT,
    MovimientoDetalleCantidad INT NOT NULL,
    MovimientoDetallePrecioBase DECIMAL(12,2) NOT NULL,
    MovimientoDetalleDescuentoAplicado DECIMAL(12,2),
    MovimientoDetallePrecioUnitario DECIMAL(12,2) NOT NULL,
    MovimientoDetallePrecioTotal DECIMAL(12,2) NOT NULL,
    MovimientoDetalleDescripcion VARCHAR(255), -- added
    CONSTRAINT fk_detalle_movimiento FOREIGN KEY (MovimientoId) REFERENCES Movimiento(MovimientoId),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (ProductoId) REFERENCES productos(ProductoId),
    CONSTRAINT fk_detalle_lugar FOREIGN KEY (MovimientoLugarId) REFERENCES MovimientoLugar(MovimientoLugarId),
    CONSTRAINT chk_det_cantidad CHECK (MovimientoDetalleCantidad <> 0),
    CONSTRAINT chk_det_precio_unit CHECK (MovimientoDetallePrecioUnitario >= 0),
    CONSTRAINT chk_det_precio_total CHECK (MovimientoDetallePrecioTotal >= 0)
);

CREATE INDEX idx_det_mov ON MovimientoDetalle(MovimientoId);
CREATE INDEX idx_det_prod ON MovimientoDetalle(ProductoId);
CREATE INDEX idx_det_lugar ON MovimientoDetalle(MovimientoLugarId);

--🔒 SECTION 3 — TRIGGERS & PROCEDURES

DELIMITER $$

-- 🚫 PREVENT NEGATIVE STOCK
DROP TRIGGER IF EXISTS trg_no_negative_stock
CREATE TRIGGER trg_no_negative_stock
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    IF NEW.ProductoStock < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_PRODUCTO_STOCK_NEGATIVE|Stock cannot be negative';
    END IF;
END$$

-- ⚠️ AUTO-UPDATE STOCK CRITICO FLAG
DROP TRIGGER IF EXISTS trg_producto_stockcritico_update
CREATE TRIGGER trg_producto_stockcritico_update
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    SET NEW.ProductoStockCritico = (NEW.ProductoStock <= NEW.ProductoCriticoNumero);
END$$

-- 🔒 PREVENT MODIFICATION/DELETION OF CONFIRMED DETALLE
DROP TRIGGER IF EXISTS trg_detalle_block_after_confirm
CREATE TRIGGER trg_detalle_block_after_confirm
BEFORE UPDATE ON MovimientoDetalle
FOR EACH ROW
BEGIN
    DECLARE v_estado VARCHAR(20);
    SELECT MovimientoEstado INTO v_estado
    FROM Movimiento
    WHERE MovimientoId = OLD.MovimientoId;
    IF v_estado = 'CONFIRMADO' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_DETALLE_CONFIRMED_UPDATE|Cannot modify detail of a confirmed movimiento';
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_detalle_delete_block_after_confirm
CREATE TRIGGER trg_detalle_delete_block_after_confirm
BEFORE DELETE ON MovimientoDetalle
FOR EACH ROW
BEGIN
    DECLARE v_estado VARCHAR(20);
    SELECT MovimientoEstado INTO v_estado
    FROM Movimiento
    WHERE MovimientoId = OLD.MovimientoId;
    IF v_estado = 'CONFIRMADO' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_DETALLE_CONFIRMED_DELETE|Cannot delete detail of a confirmed movimiento';
    END IF;
END$$

-- 🛡️ BEFORE INSERT SAFEGUARD ON MOVIMIENTODETALLE
DROP TRIGGER IF EXISTS trg_detalle_before_insert
CREATE TRIGGER trg_detalle_before_insert
BEFORE INSERT ON MovimientoDetalle
FOR EACH ROW
BEGIN
    IF NEW.MovimientoDetalleCantidad = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_DETALLE_CANTIDAD_ZERO|Cannot insert MovimientoDetalle with zero quantity';
    END IF;
END$$

--🔒 CONFIGURACION SINGLETON
DROP TRIGGER IF EXISTS trg_configuracion_singleton
CREATE TRIGGER trg_configuracion_singleton
BEFORE INSERT ON Configuracion
FOR EACH ROW
BEGIN
    IF (SELECT COUNT(*) FROM Configuracion) >= 1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_CONFIG_SINGLETON|Only one configuration row is allowed';
    END IF;
    SET NEW.ConfiguracionId = 1;
END$$

--🛡️ PREVENT CATEGORY DELETE WITH PRODUCTS
DROP TRIGGER IF EXISTS trg_categoria_delete_block
CREATE TRIGGER trg_categoria_delete_block
BEFORE DELETE ON Categoria
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM productos WHERE ProductoCategoria = OLD.CategoriaId) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_CATEGORIA_IN_USE|Cannot delete category with products assigned';
    END IF;
END$$

DELIMITER ;
DELIMITER $$

-- 🧮 CALCULAR DESCUENTO POR PRODUCTO
DROP PROCEDURE IF EXISTS sp_calcular_descuento_producto$$
CREATE PROCEDURE sp_calcular_descuento_producto(
    IN p_producto_id BIGINT,
    IN p_precio_base DECIMAL(12,2),
    IN p_cantidad INT,
    OUT p_descuento_total DECIMAL(12,2)
)
BEGIN
    DECLARE v_tipo VARCHAR(20);
    DECLARE v_valor DECIMAL(12,2);
    DECLARE v_total DECIMAL(12,2);
    DECLARE v_descuento DECIMAL(12,2) DEFAULT 0;
    DECLARE v_pagables INT;

    SET v_total = p_precio_base * p_cantidad;

    -- PRODUCT-LEVEL DESCUENTO
    SELECT d.DescuentoTipo, d.DescuentoValor
    INTO v_tipo, v_valor
    FROM productos p
    LEFT JOIN Descuento d ON p.DescuentoId = d.DescuentoId
    WHERE p.ProductoId = p_producto_id
    AND d.DescuentoActivo = TRUE
    LIMIT 1;

    -- FALLBACK TO CATEGORIA
    IF v_tipo IS NULL THEN
        SELECT d.DescuentoTipo, d.DescuentoValor
        INTO v_tipo, v_valor
        FROM productos p
        JOIN Categoria c ON p.ProductoCategoria = c.CategoriaId
        LEFT JOIN Descuento d ON c.DescuentoId = d.DescuentoId
        WHERE p.ProductoId = p_producto_id
        AND d.DescuentoActivo = TRUE
        LIMIT 1;
    END IF;

    -- LOGICA DE DESCUENTO
    IF v_tipo = 'FLAT' THEN
        SET v_descuento = v_valor * p_cantidad;
    ELSEIF v_tipo = 'PORCENTAJE' THEN
        SET v_descuento = v_total * (v_valor / 100);
    ELSEIF v_tipo = 'MULTIPLICATIVO' THEN
        SET v_pagables = CEIL(p_cantidad / v_valor);
        SET v_descuento = (p_cantidad - v_pagables) * p_precio_base;
    END IF;

    SET p_descuento_total = IFNULL(v_descuento, 0);
END$$

-- 🔄 MOVIMIENTO DETALLE CALCULATION TRIGGERS
DROP TRIGGER IF EXISTS trg_detalle_calculo_insert$$
CREATE TRIGGER trg_detalle_calculo_insert
BEFORE INSERT ON MovimientoDetalle
FOR EACH ROW
BEGIN
    DECLARE v_precio_base DECIMAL(12,2);
    DECLARE v_descuento_total DECIMAL(12,2);
    DECLARE v_iva_pct DECIMAL(5,2);
    DECLARE v_descuento_unit DECIMAL(12,2);

    SELECT ProductoPrecio INTO v_precio_base FROM productos WHERE ProductoId = NEW.ProductoId;

    CALL sp_calcular_descuento_producto(
        NEW.ProductoId,
        v_precio_base,
        NEW.MovimientoDetalleCantidad,
        v_descuento_total
    );

    SELECT IVA INTO v_iva_pct FROM Configuracion WHERE ConfiguracionId = 1 LIMIT 1;

    SET v_descuento_unit = CASE
        WHEN NEW.MovimientoDetalleCantidad = 0 THEN 0
        ELSE v_descuento_total / NEW.MovimientoDetalleCantidad
    END;

    SET NEW.MovimientoDetallePrecioBase = v_precio_base;
    SET NEW.MovimientoDetalleDescuentoAplicado = v_descuento_unit;
    SET NEW.MovimientoDetallePrecioUnitario = GREATEST(0, (v_precio_base - v_descuento_unit) * (1 + (v_iva_pct / 100)));
    SET NEW.MovimientoDetallePrecioTotal = NEW.MovimientoDetallePrecioUnitario * NEW.MovimientoDetalleCantidad;
END$$

DROP TRIGGER IF EXISTS trg_detalle_calculo_update$$
CREATE TRIGGER trg_detalle_calculo_update
BEFORE UPDATE ON MovimientoDetalle
FOR EACH ROW
BEGIN
    DECLARE v_precio_base DECIMAL(12,2);
    DECLARE v_descuento_total DECIMAL(12,2);
    DECLARE v_iva_pct DECIMAL(5,2);
    DECLARE v_descuento_unit DECIMAL(12,2);

    SELECT ProductoPrecio INTO v_precio_base FROM productos WHERE ProductoId = NEW.ProductoId;

    CALL sp_calcular_descuento_producto(
        NEW.ProductoId,
        v_precio_base,
        NEW.MovimientoDetalleCantidad,
        v_descuento_total
    );

    SELECT IVA INTO v_iva_pct FROM Configuracion WHERE ConfiguracionId = 1 LIMIT 1;

    SET v_descuento_unit = CASE
        WHEN NEW.MovimientoDetalleCantidad = 0 THEN 0
        ELSE v_descuento_total / NEW.MovimientoDetalleCantidad
    END;

    SET NEW.MovimientoDetallePrecioBase = v_precio_base;
    SET NEW.MovimientoDetalleDescuentoAplicado = v_descuento_unit;
    SET NEW.MovimientoDetallePrecioUnitario = GREATEST(0, (v_precio_base - v_descuento_unit) * (1 + (v_iva_pct / 100)));
    SET NEW.MovimientoDetallePrecioTotal = NEW.MovimientoDetallePrecioUnitario * NEW.MovimientoDetalleCantidad;
END$$

-- 🔄 RECALCULAR MOVIMIENTO PROCEDURE
DROP PROCEDURE IF EXISTS sp_recalcular_movimiento$$
CREATE PROCEDURE sp_recalcular_movimiento(p_movimiento_id BIGINT)
BEGIN
    DECLARE v_total DECIMAL(12,2);
    DECLARE v_iva_pct DECIMAL(5,2);
    DECLARE v_neto DECIMAL(12,2);

    SELECT IFNULL(SUM(MovimientoDetallePrecioTotal), 0) INTO v_total
    FROM MovimientoDetalle
    WHERE MovimientoId = p_movimiento_id;

    SELECT IVA INTO v_iva_pct FROM Configuracion WHERE ConfiguracionId = 1 LIMIT 1;

    SET v_neto = v_total / (1 + (v_iva_pct / 100));

    UPDATE Movimiento
    SET
        MovimientoPrecioBase = (SELECT IFNULL(SUM(MovimientoDetallePrecioBase * MovimientoDetalleCantidad), 0)
                               FROM MovimientoDetalle WHERE MovimientoId = p_movimiento_id),
        MovimientoStock = (SELECT IFNULL(SUM(MovimientoDetalleCantidad), 0)
                           FROM MovimientoDetalle WHERE MovimientoId = p_movimiento_id),
        MovimientoPrecioTotal = v_total,
        MovimientoPrecioNeto = v_neto
    WHERE MovimientoId = p_movimiento_id;
END$$

-- 🔄 AUTO RECALCULATION TRIGGERS
DROP TRIGGER IF EXISTS trg_detalle_after_insert_recalc$$
CREATE TRIGGER trg_detalle_after_insert_recalc
AFTER INSERT ON MovimientoDetalle
FOR EACH ROW
BEGIN
    CALL sp_recalcular_movimiento(NEW.MovimientoId);
END$$

DROP TRIGGER IF EXISTS trg_detalle_after_update_recalc$$
CREATE TRIGGER trg_detalle_after_update_recalc
AFTER UPDATE ON MovimientoDetalle
FOR EACH ROW
BEGIN
    CALL sp_recalcular_movimiento(NEW.MovimientoId);
END$$

DROP TRIGGER IF EXISTS trg_detalle_after_delete_recalc$$
CREATE TRIGGER trg_detalle_after_delete_recalc
AFTER DELETE ON MovimientoDetalle
FOR EACH ROW
BEGIN
    CALL sp_recalcular_movimiento(OLD.MovimientoId);
END$$

DELIMITER ;

DROP TRIGGER IF EXISTS trg_usuario_rut_insert;
DELIMITER $$

CREATE TRIGGER trg_usuario_rut_insert
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN
    DECLARE rut_clean VARCHAR(20);
    DECLARE rut_body VARCHAR(20);
    DECLARE dv_input CHAR(1);

    DECLARE sum INT DEFAULT 0;
    DECLARE multiplier INT DEFAULT 2;
    DECLARE len INT;
    DECLARE i INT;
    DECLARE current INT;
    DECLARE remainder INT;
    DECLARE dv_expected CHAR(1);

    -- normalize input (basic cleanup)
    SET rut_clean = REPLACE(REPLACE(NEW.UsuarioRun, '.', ''), '-', '');
    SET len = LENGTH(rut_clean);

    IF len < 2 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_RUT_INVALID|Invalid RUT format';
    END IF;

    SET rut_body = SUBSTRING(rut_clean, 1, len - 1);
    SET dv_input = UPPER(SUBSTRING(rut_clean, len, 1));

    -- numeric validation
    IF rut_body REGEXP '[^0-9]' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_RUT_INVALID|RUN must be numeric';
    END IF;

    -- MOD 11 calculation
    SET i = LENGTH(rut_body);

    WHILE i > 0 DO
        SET current = CAST(SUBSTRING(rut_body, i, 1) AS UNSIGNED);
        SET sum = sum + current * multiplier;

        SET multiplier = multiplier + 1;
        IF multiplier > 7 THEN
            SET multiplier = 2;
        END IF;

        SET i = i - 1;
    END WHILE;

    SET remainder = 11 - (sum % 11);

    IF remainder = 11 THEN
        SET dv_expected = '0';
    ELSEIF remainder = 10 THEN
        SET dv_expected = 'K';
    ELSE
        SET dv_expected = CAST(remainder AS CHAR);
    END IF;

    IF dv_expected <> dv_input THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_RUT_INVALID_DV|RUT verification failed';
    END IF;

END$$

DELIMITER ;

DROP TRIGGER IF EXISTS trg_usuario_rut_update;
DELIMITER $$

CREATE TRIGGER trg_usuario_rut_update
BEFORE UPDATE ON usuarios
FOR EACH ROW
BEGIN
    -- identical logic, replace NEW.UsuarioRun
    DECLARE rut_clean VARCHAR(20);
    DECLARE rut_body VARCHAR(20);
    DECLARE dv_input CHAR(1);

    DECLARE sum INT DEFAULT 0;
    DECLARE multiplier INT DEFAULT 2;
    DECLARE len INT;
    DECLARE i INT;
    DECLARE current INT;
    DECLARE remainder INT;
    DECLARE dv_expected CHAR(1);

    SET rut_clean = REPLACE(REPLACE(NEW.UsuarioRun, '.', ''), '-', '');
    SET len = LENGTH(rut_clean);

    IF len < 2 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_RUT_INVALID|Invalid RUT format';
    END IF;

    SET rut_body = SUBSTRING(rut_clean, 1, len - 1);
    SET dv_input = UPPER(SUBSTRING(rut_clean, len, 1));

    IF rut_body REGEXP '[^0-9]' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_RUT_INVALID|RUN must be numeric';
    END IF;

    SET i = LENGTH(rut_body);

    WHILE i > 0 DO
        SET current = CAST(SUBSTRING(rut_body, i, 1) AS UNSIGNED);
        SET sum = sum + current * multiplier;

        SET multiplier = multiplier + 1;
        IF multiplier > 7 THEN
            SET multiplier = 2;
        END IF;

        SET i = i - 1;
    END WHILE;

    SET remainder = 11 - (sum % 11);

    IF remainder = 11 THEN
        SET dv_expected = '0';
    ELSEIF remainder = 10 THEN
        SET dv_expected = 'K';
    ELSE
        SET dv_expected = CAST(remainder AS CHAR);
    END IF;

    IF dv_expected <> dv_input THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_RUT_INVALID_DV|RUT verification failed';
    END IF;

END$$

DELIMITER ;