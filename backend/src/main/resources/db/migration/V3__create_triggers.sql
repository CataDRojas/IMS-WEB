-- =========================================
-- V3__triggers.sql
-- SYSTEM BEHAVIOR LAYER (TRIGGERS ONLY)
-- =========================================

-- =========================
-- PRODUCT SAFETY
-- =========================
CREATE TRIGGER trg_no_negative_stock
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    IF NEW.ProductoStock < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_PRODUCTO_STOCK_NEGATIVE|Stock cannot be negative';
    END IF;
END;

CREATE TRIGGER trg_producto_stockcritico_update
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    SET NEW.ProductoStockCritico =
        (NEW.ProductoStock <= NEW.ProductoCriticoNumero);
END;

-- =========================
-- MOVIMIENTO DETAIL SAFETY
-- =========================
CREATE TRIGGER trg_detalle_before_insert
BEFORE INSERT ON MovimientoDetalle
FOR EACH ROW
BEGIN
    IF NEW.MovimientoDetalleCantidad = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_DETALLE_CANTIDAD_ZERO|Cannot insert MovimientoDetalle with zero quantity';
    END IF;
END;

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
END;

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
END;

-- =========================
-- CATEGORIA PROTECTION
-- =========================
CREATE TRIGGER trg_categoria_delete_block
BEFORE DELETE ON Categoria
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM productos
        WHERE ProductoCategoria = OLD.CategoriaId
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_CATEGORIA_IN_USE|Cannot delete category with products assigned';
    END IF;
END;

-- =========================
-- CONFIG SINGLETON RULE
-- =========================
CREATE TRIGGER trg_configuracion_singleton
BEFORE INSERT ON Configuracion
FOR EACH ROW
BEGIN
    IF (SELECT COUNT(*) FROM Configuracion) >= 1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERR_CONFIG_SINGLETON|Only one configuration row is allowed';
    END IF;

    SET NEW.ConfiguracionId = 1;
END;



-- =========================
-- STOCK STATE CONTROL
-- =========================
CREATE TRIGGER trg_movimiento_estado_stock
AFTER UPDATE ON Movimiento
FOR EACH ROW
BEGIN
    IF NEW.MovimientoEstado = 'CONFIRMADO'
       AND OLD.MovimientoEstado <> 'CONFIRMADO' THEN
        CALL sp_aplicar_stock(NEW.MovimientoId);
    END IF;

    IF OLD.MovimientoEstado = 'CONFIRMADO'
       AND NEW.MovimientoEstado <> 'CONFIRMADO' THEN
        CALL sp_revertir_stock(NEW.MovimientoId);
    END IF;
END;

-- =========================
-- RUT VALIDATION (USUARIOS)
-- =========================
CREATE TRIGGER trg_usuario_rut_insert
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN

    DECLARE body VARCHAR(20);
    DECLARE dv_input CHAR(1);
    DECLARE dv_calc CHAR(1);

    DECLARE sum INT DEFAULT 0;
    DECLARE mult INT DEFAULT 2;
    DECLARE i INT;
    DECLARE digit INT;
    DECLARE mod11 INT;

    -- OPTIONAL FIELD HANDLING
    IF NEW.UsuarioRun IS NULL OR TRIM(NEW.UsuarioRun) = '' THEN

        IF NEW.UsuarioDV IS NOT NULL AND TRIM(NEW.UsuarioDV) <> '' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ERR_RUT_INVALID_DV_WITHOUT_RUN';
        END IF;

    ELSE

        SET body = TRIM(NEW.UsuarioRun);
        SET dv_input = UPPER(TRIM(NEW.UsuarioDV));

        -- DV required
        IF dv_input IS NULL OR dv_input = '' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ERR_RUT_MISSING_DV';
        END IF;

        -- NUMERIC VALIDATION
        IF body REGEXP '[^0-9]' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ERR_RUT_NON_NUMERIC';
        END IF;

        -- CALCULATION
        SET i = CHAR_LENGTH(body);
        SET sum = 0;
        SET mult = 2;

        WHILE i > 0 DO
            SET digit = CAST(SUBSTRING(body, i, 1) AS UNSIGNED);
            SET sum = sum + digit * mult;

            SET mult = IF(mult = 7, 2, mult + 1);
            SET i = i - 1;
        END WHILE;

        SET mod11 = 11 - (sum % 11);

        IF mod11 = 11 THEN
            SET dv_calc = '0';
        ELSEIF mod11 = 10 THEN
            SET dv_calc = 'K';
        ELSE
            SET dv_calc = CAST(mod11 AS CHAR);
        END IF;

        -- FINAL VALIDATION
        IF dv_calc <> dv_input THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ERR_RUT_DV_MISMATCH';
        END IF;

    END IF;

END;


-- =========================================
-- MOVIMIENTO DETALLE CALCULATION (INSERT)
-- =========================================
CREATE TRIGGER trg_detalle_calculo_insert
BEFORE INSERT ON MovimientoDetalle
FOR EACH ROW
BEGIN
    DECLARE v_tipo_mov VARCHAR(50);

    DECLARE v_precio_base DECIMAL(12,2);
    DECLARE v_descuento_total DECIMAL(12,2);
    DECLARE v_cantidad_real INT;
    DECLARE v_total_final DECIMAL(12,2);

    -- =========================
    -- GET MOVIMIENTO TYPE (GATE)
    -- =========================
    SELECT MovimientoTipo
    INTO v_tipo_mov
    FROM Movimiento
    WHERE MovimientoId = NEW.MovimientoId;

    -- =========================
    -- IF NOT SALIDA → SKIP LOGIC
    -- =========================
    IF v_tipo_mov <> 'SALIDA' THEN

        SET NEW.MovimientoDetallePrecioBase = NULL;
        SET NEW.MovimientoDetalleDescuentoAplicado = NULL;
        SET NEW.MovimientoDetallePrecioUnitario = NULL;
        SET NEW.MovimientoDetallePrecioTotal = NULL;

    ELSE

        -- real units
        SET v_cantidad_real =
            NEW.MovimientoDetalleCantidad *
            IFNULL(NULLIF(NEW.MovimientoDetalleUnidadesPorPaquete, 0), 1);

        -- snapshot price (IVA INCLUDED already in your model)
        SELECT ProductoPrecio
        INTO v_precio_base
        FROM productos
        WHERE ProductoId = NEW.ProductoId;

        -- discount engine (returns total discount for whole line)
        CALL sp_calcular_descuento_producto(
            NEW.ProductoId,
            v_precio_base,
            v_cantidad_real,
            v_descuento_total
        );

        -- safety
        SET v_descuento_total = IFNULL(v_descuento_total, 0);

        -- total after discount (still IVA-included model)
        SET v_total_final =
            (v_precio_base * v_cantidad_real) - v_descuento_total;

        -- prevent negatives
        SET v_total_final = GREATEST(0, v_total_final);

        -- persist base price snapshot
        SET NEW.MovimientoDetallePrecioBase = v_precio_base;

        -- per-unit discount
        SET NEW.MovimientoDetalleDescuentoAplicado =
            CASE
                WHEN v_cantidad_real = 0 THEN 0
                ELSE v_descuento_total / v_cantidad_real
            END;

        -- final unit price (after discount, still IVA-included system)
        SET NEW.MovimientoDetallePrecioUnitario =
            CASE
                WHEN v_cantidad_real = 0 THEN 0
                ELSE v_total_final / v_cantidad_real
            END;

        -- row total
        SET NEW.MovimientoDetallePrecioTotal = v_total_final;

    END IF;

END;

-- =========================================
-- MOVIMIENTO DETALLE CALCULATION (UPDATE)
-- =========================================
CREATE TRIGGER trg_detalle_calculo_update
BEFORE UPDATE ON MovimientoDetalle
FOR EACH ROW
BEGIN
    DECLARE v_tipo_mov VARCHAR(50);

    DECLARE v_precio_base DECIMAL(12,2);
    DECLARE v_descuento_total DECIMAL(12,2);
    DECLARE v_cantidad_real INT;
    DECLARE v_total_final DECIMAL(12,2);

    -- =========================
    -- GET MOVIMIENTO TYPE
    -- =========================
    SELECT MovimientoTipo
    INTO v_tipo_mov
    FROM Movimiento
    WHERE MovimientoId = NEW.MovimientoId;

    -- =========================
    -- NON-SALIDA GATE
    -- =========================
    IF v_tipo_mov <> 'SALIDA' THEN

        SET NEW.MovimientoDetallePrecioBase = NULL;
        SET NEW.MovimientoDetalleDescuentoAplicado = NULL;
        SET NEW.MovimientoDetallePrecioUnitario = NULL;
        SET NEW.MovimientoDetallePrecioTotal = NULL;

    ELSE

        -- real units
        SET v_cantidad_real =
            NEW.MovimientoDetalleCantidad *
            IFNULL(NULLIF(NEW.MovimientoDetalleUnidadesPorPaquete, 0), 1);

        -- base price snapshot
        SELECT ProductoPrecio
        INTO v_precio_base
        FROM productos
        WHERE ProductoId = NEW.ProductoId;

        -- discount engine
        CALL sp_calcular_descuento_producto(
            NEW.ProductoId,
            v_precio_base,
            v_cantidad_real,
            v_descuento_total
        );

        -- safety
        SET v_descuento_total = IFNULL(v_descuento_total, 0);

        -- final row total
        SET v_total_final =
            (v_precio_base * v_cantidad_real) - v_descuento_total;

        SET v_total_final = GREATEST(0, v_total_final);

        -- persist base
        SET NEW.MovimientoDetallePrecioBase = v_precio_base;

        -- per-unit discount
        SET NEW.MovimientoDetalleDescuentoAplicado =
            CASE
                WHEN v_cantidad_real = 0 THEN 0
                ELSE v_descuento_total / v_cantidad_real
            END;

        -- unit price after discount
        SET NEW.MovimientoDetallePrecioUnitario =
            CASE
                WHEN v_cantidad_real = 0 THEN 0
                ELSE v_total_final / v_cantidad_real
            END;

        -- total
        SET NEW.MovimientoDetallePrecioTotal = v_total_final;

    END IF;

END;

