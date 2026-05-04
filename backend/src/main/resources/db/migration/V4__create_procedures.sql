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
-- MOVIMIENTO RECALC ENGINE
-- =========================================
CREATE PROCEDURE sp_recalcular_movimiento(IN p_movimiento_id BIGINT)
BEGIN

    DECLARE v_tipo VARCHAR(50);

    DECLARE v_total_bruto DECIMAL(12,2);
    DECLARE v_descuento DECIMAL(12,2);
    DECLARE v_total_con_descuento DECIMAL(12,2);

    DECLARE v_iva_pct DECIMAL(5,2);
    DECLARE v_neto DECIMAL(12,2);

    DECLARE v_precio_base DECIMAL(12,2);
    DECLARE v_stock_total INT;

    SELECT MovimientoTipo, IFNULL(MovimientoDescuento, 0)
    INTO v_tipo, v_descuento
    FROM Movimiento
    WHERE MovimientoId = p_movimiento_id;

    SELECT IFNULL(SUM(
        MovimientoDetallePrecioBase *
        MovimientoDetalleCantidad *
        IFNULL(NULLIF(MovimientoDetalleUnidadesPorPaquete, 0), 1)
    ), 0)
    INTO v_precio_base
    FROM MovimientoDetalle
    WHERE MovimientoId = p_movimiento_id;

    SELECT IFNULL(SUM(MovimientoDetallePrecioTotal), 0)
    INTO v_total_bruto
    FROM MovimientoDetalle
    WHERE MovimientoId = p_movimiento_id;

    SET v_total_con_descuento = GREATEST(0, v_total_bruto - v_descuento);

    SELECT IVA
    INTO v_iva_pct
    FROM Configuracion
    WHERE ConfiguracionId = 1
    LIMIT 1;

    SET v_neto =
        v_total_con_descuento / (1 + (v_iva_pct / 100));

    SELECT IFNULL(SUM(
        MovimientoDetalleCantidad *
        IFNULL(NULLIF(MovimientoDetalleUnidadesPorPaquete, 0), 1)
    ), 0)
    INTO v_stock_total
    FROM MovimientoDetalle
    WHERE MovimientoId = p_movimiento_id;

    UPDATE Movimiento
    SET
        MovimientoPrecioBase = FLOOR(v_precio_base),
        MovimientoPrecioTotal = FLOOR(v_total_con_descuento),
        MovimientoPrecioNeto = FLOOR(v_neto),
        MovimientoStock = v_stock_total
    WHERE MovimientoId = p_movimiento_id;

END$$


-- =========================================
-- STOCK APPLY
-- =========================================
CREATE PROCEDURE sp_aplicar_stock(IN p_movimiento_id BIGINT)
BEGIN

    DECLARE v_tipo VARCHAR(20);
    DECLARE v_lugar_prioridad BIGINT;

    DECLARE done INT DEFAULT FALSE;
    DECLARE v_producto_id BIGINT;
    DECLARE v_cantidad INT;
    DECLARE v_stock_prioridad INT;

    DECLARE cur CURSOR FOR
        SELECT ProductoId,
               SUM(MovimientoDetalleCantidad *
                   IFNULL(NULLIF(MovimientoDetalleUnidadesPorPaquete, 0), 1))
        FROM MovimientoDetalle
        WHERE MovimientoId = p_movimiento_id
        GROUP BY ProductoId;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    SELECT MovimientoTipo INTO v_tipo
    FROM Movimiento
    WHERE MovimientoId = p_movimiento_id;

    IF v_tipo = 'ENTRADA' THEN

        INSERT INTO MovimientoLugarProducto (
            MovimientoLugarId,
            ProductoId,
            MovimientoLugarProductoStock
        )
        SELECT
            md.MovimientoLugarId,
            md.ProductoId,
            SUM(md.MovimientoDetalleCantidad *
                IFNULL(NULLIF(md.MovimientoDetalleUnidadesPorPaquete, 0), 1))
        FROM MovimientoDetalle md
        WHERE md.MovimientoId = p_movimiento_id
        GROUP BY md.MovimientoLugarId, md.ProductoId
        ON DUPLICATE KEY UPDATE
            MovimientoLugarProductoStock =
                MovimientoLugarProductoStock + VALUES(MovimientoLugarProductoStock);

    ELSEIF v_tipo = 'SALIDA' THEN

        SELECT MovimientoLugarId INTO v_lugar_prioridad
        FROM MovimientoLugar
        WHERE MovimientoLugarPrioridad = TRUE
        LIMIT 1;

        OPEN cur;

        read_loop: LOOP
            FETCH cur INTO v_producto_id, v_cantidad;
            IF done THEN
                LEAVE read_loop;
            END IF;

            SELECT MovimientoLugarProductoStock
            INTO v_stock_prioridad
            FROM MovimientoLugarProducto
            WHERE MovimientoLugarId = v_lugar_prioridad
              AND ProductoId = v_producto_id
            FOR UPDATE;

            IF v_stock_prioridad >= v_cantidad THEN

                UPDATE MovimientoLugarProducto
                SET MovimientoLugarProductoStock =
                    MovimientoLugarProductoStock - v_cantidad
                WHERE MovimientoLugarId = v_lugar_prioridad
                  AND ProductoId = v_producto_id;

            ELSE

                UPDATE MovimientoLugarProducto
                SET MovimientoLugarProductoStock = 0
                WHERE MovimientoLugarId = v_lugar_prioridad
                  AND ProductoId = v_producto_id;

                SET v_cantidad = v_cantidad - v_stock_prioridad;

                UPDATE MovimientoLugarProducto
                SET MovimientoLugarProductoStock =
                    GREATEST(0, MovimientoLugarProductoStock - v_cantidad)
                WHERE ProductoId = v_producto_id
                  AND MovimientoLugarId <> v_lugar_prioridad;

            END IF;

        END LOOP;

        CLOSE cur;

    ELSEIF v_tipo = 'AJUSTE' THEN

        INSERT INTO MovimientoLugarProducto (
            MovimientoLugarId,
            ProductoId,
            MovimientoLugarProductoStock
        )
        SELECT
            md.MovimientoLugarId,
            md.ProductoId,
            SUM(md.MovimientoDetalleCantidad *
                IFNULL(NULLIF(md.MovimientoDetalleUnidadesPorPaquete, 0), 1))
        FROM MovimientoDetalle md
        WHERE md.MovimientoId = p_movimiento_id
        GROUP BY md.MovimientoLugarId, md.ProductoId
        ON DUPLICATE KEY UPDATE
            MovimientoLugarProductoStock =
                VALUES(MovimientoLugarProductoStock);

    END IF;

END$$


-- =========================================
-- STOCK REVERT
-- =========================================
CREATE PROCEDURE sp_revertir_stock(IN p_movimiento_id BIGINT)
BEGIN

    DECLARE v_tipo VARCHAR(20);

    SELECT MovimientoTipo INTO v_tipo
    FROM Movimiento
    WHERE MovimientoId = p_movimiento_id;

    IF v_tipo = 'ENTRADA' THEN

        UPDATE MovimientoLugarProducto mlp
        JOIN (
            SELECT MovimientoLugarId,
                   ProductoId,
                   SUM(MovimientoDetalleCantidad *
                       IFNULL(NULLIF(MovimientoDetalleUnidadesPorPaquete, 0), 1)) AS total
            FROM MovimientoDetalle
            WHERE MovimientoId = p_movimiento_id
            GROUP BY MovimientoLugarId, ProductoId
        ) r
        ON mlp.MovimientoLugarId = r.MovimientoLugarId
        AND mlp.ProductoId = r.ProductoId
        SET mlp.MovimientoLugarProductoStock =
            mlp.MovimientoLugarProductoStock - r.total;

    ELSEIF v_tipo = 'SALIDA' THEN

        UPDATE MovimientoLugarProducto mlp
        JOIN (
            SELECT MovimientoLugarId,
                   ProductoId,
                   SUM(MovimientoDetalleCantidad *
                       IFNULL(NULLIF(MovimientoDetalleUnidadesPorPaquete, 0), 1)) AS total
            FROM MovimientoDetalle
            WHERE MovimientoId = p_movimiento_id
            GROUP BY MovimientoLugarId, ProductoId
        ) r
        ON mlp.MovimientoLugarId = r.MovimientoLugarId
        AND mlp.ProductoId = r.ProductoId
        SET mlp.MovimientoLugarProductoStock =
            mlp.MovimientoLugarProductoStock + r.total;

    END IF;

END$$


-- =========================================
-- PRODUCTO STOCK SYNC
-- =========================================
CREATE PROCEDURE sp_sync_producto_stock(IN p_producto_id BIGINT)
BEGIN

    DECLARE v_total INT;

    SELECT COALESCE(SUM(MovimientoLugarProductoStock), 0)
    INTO v_total
    FROM MovimientoLugarProducto
    WHERE ProductoId = p_producto_id;

    UPDATE productos
    SET ProductoStock = v_total
    WHERE ProductoId = p_producto_id;

END$$

DELIMITER ;