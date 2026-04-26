-- =========================================
-- V2__constraints.sql
-- DOMAIN RULES / CONSTRAINT LAYER
-- =========================================

-- =========================
-- CONFIGURACION RULES
-- =========================
ALTER TABLE Configuracion
ADD CONSTRAINT chk_configuracion_iva
CHECK (IVA >= 0 AND IVA <= 100);

-- =========================
-- DESCUENTO RULES
-- =========================
ALTER TABLE Descuento
ADD CONSTRAINT chk_descuento_valor
CHECK (
    (DescuentoTipo = 'MULTIPLICATIVO' AND DescuentoValor > 0)
    OR
    (DescuentoTipo = 'FLAT' AND DescuentoValor >= 0)
    OR
    (DescuentoTipo = 'PORCENTAJE' AND DescuentoValor BETWEEN 0 AND 100)
);

ALTER TABLE Descuento
ADD CONSTRAINT chk_descuento_tipo
CHECK (DescuentoTipo IN ('FLAT', 'PORCENTAJE', 'MULTIPLICATIVO'));

-- =========================
-- PRODUCTO RULES
-- =========================
ALTER TABLE productos
ADD CONSTRAINT chk_producto_stock
CHECK (ProductoStock >= 0);

ALTER TABLE productos
ADD CONSTRAINT chk_producto_precio
CHECK (ProductoPrecio >= 0);

-- =========================
-- MOVIMIENTO RULES
-- =========================
ALTER TABLE Movimiento
ADD CONSTRAINT chk_movimiento_tipo
CHECK (MovimientoTipo IN ('ENTRADA', 'SALIDA', 'AJUSTE'));

ALTER TABLE Movimiento
ADD CONSTRAINT chk_movimiento_estado
CHECK (MovimientoEstado IN ('PENDIENTE', 'CONFIRMADO', 'ANULADO'));

ALTER TABLE Movimiento
ADD CONSTRAINT chk_movimiento_metodo_pago
CHECK (MovimientoMetodoPago IN ('EFECTIVO', 'TARJETA'));

-- =========================
-- MOVIMIENTO DETALLE RULES
-- =========================
ALTER TABLE MovimientoDetalle
ADD CONSTRAINT chk_det_cantidad
CHECK (MovimientoDetalleCantidad <> 0);

ALTER TABLE MovimientoDetalle
ADD CONSTRAINT chk_det_unidades_paquete
CHECK (MovimientoDetalleUnidadesPorPaquete >= 1);

ALTER TABLE MovimientoDetalle
ADD CONSTRAINT chk_det_precio_unit
CHECK (MovimientoDetallePrecioUnitario >= 0);

ALTER TABLE MovimientoDetalle
ADD CONSTRAINT chk_det_precio_total
CHECK (MovimientoDetallePrecioTotal >= 0);