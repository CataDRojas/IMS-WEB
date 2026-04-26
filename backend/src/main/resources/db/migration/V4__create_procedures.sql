-- =========================================
-- V4__procedures.sql
-- BUSINESS LOGIC LAYER (PROCEDURES ONLY)
-- =========================================

DELIMITER $$

-- =========================
-- DISCOUNT ENGINE (UPDATED)
-- =========================
CREATE PROCEDURE sp_calcular_descuento_producto(
    IN p_producto_id BIGINT,
    IN p_precio_base DECIMAL(12,2),
    IN p_cantidad INT,
    OUT p_descuento_total DECIMAL(12,2)
)
BEGIN
    DECLARE v_tipo VARCHAR(20);
    DECLARE v_valor DECIMAL(12,2);
    DECLARE v_valor_sec DECIMAL(12,2);
    DECLARE v_total DECIMAL(12,2);
    DECLARE v_descuento DECIMAL(12,2) DEFAULT 0;

    DECLARE v_grupos INT;
    DECLARE v_restante INT;
    DECLARE v_pagables INT;

    SET v_total = p_precio_base * p_cantidad;

    SELECT d.DescuentoTipo,
           d.DescuentoValor,
           d.DescuentoValorSecundario
    INTO v_tipo, v_valor, v_valor_sec
    FROM productos p
    LEFT JOIN Descuento d ON p.DescuentoId = d.DescuentoId
    WHERE p.ProductoId = p_producto_id
      AND d.DescuentoActivo = TRUE
    LIMIT 1;

    IF v_tipo IS NULL THEN
        SELECT d.DescuentoTipo,
               d.DescuentoValor,
               d.DescuentoValorSecundario
        INTO v_tipo, v_valor, v_valor_sec
        FROM productos p
        JOIN Categoria c ON p.ProductoCategoria = c.CategoriaId
        LEFT JOIN Descuento d ON c.DescuentoId = d.DescuentoId
        WHERE p.ProductoId = p_producto_id
          AND d.DescuentoActivo = TRUE
        LIMIT 1;
    END IF;

    IF v_tipo = 'FLAT' THEN
        SET v_descuento = v_valor * p_cantidad;

    ELSEIF v_tipo = 'PORCENTAJE' THEN
        SET v_descuento = v_total * (v_valor / 100);

    ELSEIF v_tipo = 'MULTIPLICATIVO' THEN

        SET v_valor = IFNULL(v_valor, 1);
        SET v_valor_sec = IFNULL(v_valor_sec, v_valor);

        IF v_valor <= 0 THEN
            SET v_descuento = 0;
        ELSE
            SET v_grupos = FLOOR(p_cantidad / v_valor);
            SET v_restante = MOD(p_cantidad, v_valor);

            SET v_pagables = (v_grupos * v_valor_sec) + v_restante;

            SET v_descuento = (p_cantidad - v_pagables) * p_precio_base;
        END IF;

    END IF;

    SET p_descuento_total = IFNULL(v_descuento, 0);

END$$


-- =========================================
-- MOVIMIENTO RECALC ENGINE (FINAL + STOCK MIRROR)
-- =========================================
CREATE PROCEDURE sp_recalcular_movimiento(p_movimiento_id BIGINT)
proc: BEGIN

    DECLARE v_tipo VARCHAR(50);

    DECLARE v_total_bruto DECIMAL(12,2);
    DECLARE v_descuento DECIMAL(12,2);
    DECLARE v_total_con_descuento DECIMAL(12,2);

    DECLARE v_iva_pct DECIMAL(5,2);
    DECLARE v_neto DECIMAL(12,2);

    DECLARE v_precio_base DECIMAL(12,2);

    DECLARE v_stock_total INT;

    -- =========================
    -- HEADER DATA
    -- =========================
    SELECT MovimientoTipo, IFNULL(MovimientoDescuento, 0)
    INTO v_tipo, v_descuento
    FROM Movimiento
    WHERE MovimientoId = p_movimiento_id;

    IF v_tipo <> 'SALIDA' THEN
        LEAVE proc;
    END IF;

    -- =========================
    -- BASE VALUE (matches detail model)
    -- =========================
    SELECT IFNULL(SUM(
        MovimientoDetallePrecioBase *
        MovimientoDetalleCantidad *
        IFNULL(NULLIF(MovimientoDetalleUnidadesPorPaquete, 0), 1)
    ), 0)
    INTO v_precio_base
    FROM MovimientoDetalle
    WHERE MovimientoId = p_movimiento_id;

    -- =========================
    -- TOTAL BRUTO (after line-level discounts)
    -- =========================
    SELECT IFNULL(SUM(MovimientoDetallePrecioTotal), 0)
    INTO v_total_bruto
    FROM MovimientoDetalle
    WHERE MovimientoId = p_movimiento_id;

    SET v_total_con_descuento = GREATEST(0, v_total_bruto - v_descuento);

    -- =========================
    -- IVA
    -- =========================
    SELECT IVA
    INTO v_iva_pct
    FROM Configuracion
    WHERE ConfiguracionId = 1
    LIMIT 1;

    SET v_neto =
        v_total_con_descuento / (1 + (v_iva_pct / 100));

    -- =========================
    -- STOCK MIRROR (NEW)
    -- =========================
    SELECT IFNULL(SUM(
        MovimientoDetalleCantidad *
        IFNULL(NULLIF(MovimientoDetalleUnidadesPorPaquete, 0), 1)
    ), 0)
    INTO v_stock_total
    FROM MovimientoDetalle
    WHERE MovimientoId = p_movimiento_id;

    -- =========================
    -- FINAL UPDATE
    -- =========================
    UPDATE Movimiento
    SET
        MovimientoPrecioBase = v_precio_base,
        MovimientoPrecioTotal = v_total_con_descuento,
        MovimientoPrecioNeto = v_neto,
        MovimientoStock = v_stock_total
    WHERE MovimientoId = p_movimiento_id;

END$$


-- =========================
-- STOCK APPLY
-- =========================
CREATE PROCEDURE sp_aplicar_stock(IN p_movimiento_id BIGINT)
BEGIN
    DECLARE v_tipo VARCHAR(20);

    SELECT MovimientoTipo INTO v_tipo
    FROM Movimiento
    WHERE MovimientoId = p_movimiento_id;

    IF v_tipo = 'ENTRADA' THEN
        UPDATE productos p
        JOIN MovimientoDetalle d ON p.ProductoId = d.ProductoId
        SET p.ProductoStock = p.ProductoStock +
            (d.MovimientoDetalleCantidad *
             IFNULL(NULLIF(d.MovimientoDetalleUnidadesPorPaquete, 0), 1))
        WHERE d.MovimientoId = p_movimiento_id;
    ELSE
        UPDATE productos p
        JOIN MovimientoDetalle d ON p.ProductoId = d.ProductoId
        SET p.ProductoStock = p.ProductoStock -
            (d.MovimientoDetalleCantidad *
             IFNULL(NULLIF(d.MovimientoDetalleUnidadesPorPaquete, 0), 1))
        WHERE d.MovimientoId = p_movimiento_id;
    END IF;
END$$


-- =========================
-- STOCK REVERT
-- =========================
CREATE PROCEDURE sp_revertir_stock(IN p_movimiento_id BIGINT)
BEGIN
    DECLARE v_tipo VARCHAR(20);

    SELECT MovimientoTipo INTO v_tipo
    FROM Movimiento
    WHERE MovimientoId = p_movimiento_id;

    IF v_tipo = 'ENTRADA' THEN
        UPDATE productos p
        JOIN MovimientoDetalle d ON p.ProductoId = d.ProductoId
        SET p.ProductoStock = p.ProductoStock -
            (d.MovimientoDetalleCantidad *
             IFNULL(NULLIF(d.MovimientoDetalleUnidadesPorPaquete, 0), 1))
        WHERE d.MovimientoId = p_movimiento_id;
    ELSE
        UPDATE productos p
        JOIN MovimientoDetalle d ON p.ProductoId = d.ProductoId
        SET p.ProductoStock = p.ProductoStock +
            (d.MovimientoDetalleCantidad *
             IFNULL(NULLIF(d.MovimientoDetalleUnidadesPorPaquete, 0), 1))
        WHERE d.MovimientoId = p_movimiento_id;
    END IF;
END$$

DELIMITER ;