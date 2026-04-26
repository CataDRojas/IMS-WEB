import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BrowserMultiFormatReader } from '@zxing/browser';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Location } from '@angular/common';
import { PagoTarjeta } from '../../../services/pago-tarjeta';
import { BoletaService } from '../../../services/boleta';
import { ApiError } from '../../../core/errors/api-error';

import { VentasService } from '../../../services/ventas/ventas';

@Component({
  selector: 'app-ventas-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatToolbarModule
  ],
  templateUrl: './ventas-form.html',
  styleUrls: ['./ventas-form.css']
})
export class VentasForm implements OnInit {

  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;

  metodoPago: 'EFECTIVO' | 'TARJETA' = 'EFECTIVO';
  codigo = '';
  cantidad = 1;

  config: any = null;

  productoEncontrado: any = null;
  detalles: any[] = [];

  total = 0;
  iva = 0;
  totalFinal = 0;
  ivaPct = 0;

  escanerAbierto = false;
  codeReader = new BrowserMultiFormatReader();
  controlesCamara: any;
  descuentoHeader: number = 0;

  errorMessage: string | null = null;
  errorCode: string | null = null;
  successMessage: string | null = null;

  constructor(
    private router: Router,
    private ventasService: VentasService,
    private location: Location,
    private pagoTarjetaService: PagoTarjeta,
    private boletaService: BoletaService
  ) {}

  ngOnInit() {
    this.cargarConfiguracion();
  }

  // =========================
  // CONFIG
  // =========================

  cargarConfiguracion() {
    this.ventasService.getConfiguracion().subscribe({
      next: (config: any) => {
          this.config = config;
          this.ivaPct = config?.iva ?? 0;
        },
      error: () => {
        this.ivaPct = 0;
      }
    });
  }

  // =========================
  // PRODUCTO
  // =========================

buscarProducto() {
  if (!this.codigo?.trim()) return;

  const codigoLimpio = this.codigo.trim();

  // optional: clear previous messages on new search
  this.errorMessage = null;
  this.errorCode = null;
  this.successMessage = null;

  this.ventasService.getProductoByCodigo(codigoLimpio).subscribe({
    next: (prod: any) => {

      this.productoEncontrado = prod;
      this.cantidad = 1;

      this.agregarProductoLocal();

      // success feedback (optional but consistent UX)
      this.successMessage = `Producto ${prod.productoNombre} agregado`;
      this.errorMessage = null;
      this.errorCode = null;
    },

    error: (err) => {

      this.productoEncontrado = null;

      this.errorCode = err?.errorCode || 'ERR_UNKNOWN';
      this.errorMessage = err?.message || 'Producto no encontrado';

      // ensure success is cleared on failure
      this.successMessage = null;
    }
  });
}

  // =========================
  // DISCOUNT SIMULATION
  // =========================

private simularDescuento(base: number, cantidad: number, desc: any): number {

  if (!desc) return 0;

  const tipo = desc.descuentoTipo;
  const v1 = Number(desc.descuentoValor ?? 0);
  const v2 = Number(desc.descuentoValorSecundario ?? 0);

  const total = base * cantidad;

  if (tipo === 'FLAT') {
    return v1 * cantidad;
  }

  if (tipo === 'PORCENTAJE') {
    return total * (v1 / 100);
  }

  if (tipo === 'MULTIPLICATIVO') {

    if (v1 <= 0) return 0;

    const groupSize = v1;
    const paidPerGroup = v2 > 0 ? v2 : (groupSize - 1);

    const fullGroups = Math.floor(cantidad / groupSize);
    const remainder = cantidad % groupSize;

    const payableUnits = (fullGroups * paidPerGroup) + remainder;

    return (cantidad - payableUnits) * base;
  }

  return 0;
}

  // =========================
  // ADD PRODUCT
  // =========================

  agregarProductoLocal() {

    if (!this.productoEncontrado) return;
    if (this.cantidad <= 0) return;

    const base = this.productoEncontrado.productoPrecio;
    const qty = this.cantidad;

    const desc = this.productoEncontrado?.descuento;

    const descuentoTotal = this.simularDescuento(base, qty, desc);
    const unitario = base - (descuentoTotal / qty);

    const existing = this.detalles.find(d =>
      d.productoId === this.productoEncontrado.productoId
    );

    if (existing) {

      existing.movimientoDetalleCantidad += qty;

      const newQty = existing.movimientoDetalleCantidad;
      const desc = existing.descuento;
      const newDesc = this.simularDescuento(base, newQty, desc);

      existing.movimientoDetalleDescuentoAplicado = newDesc / newQty;
      existing.movimientoDetallePrecioBase = base;
      existing.movimientoDetallePrecioUnitario = base - (newDesc / newQty);
      existing.movimientoDetallePrecioTotal =
        existing.movimientoDetallePrecioUnitario * newQty;

    } else {

      this.detalles.push({
        productoId: this.productoEncontrado.productoId,
        productoNombre: this.productoEncontrado.productoNombre,
        descuento: desc, // 🔥 freeze it
        movimientoDetalleCantidad: qty,
        movimientoDetallePrecioBase: base,
        movimientoDetalleDescuentoAplicado: descuentoTotal / qty,
        movimientoDetallePrecioUnitario: base - (descuentoTotal / qty),
        movimientoDetallePrecioTotal: (base - (descuentoTotal / qty)) * qty
      });
    }

    this.recalcularTotales();
    this.resetProducto();
  }

  // =========================
  // 🔥 FIX: RECALCULAR LÍNEA
  // =========================

  private recalcularLinea(detalle: any) {

    const base = detalle.movimientoDetallePrecioBase;
    const qty = detalle.movimientoDetalleCantidad;

    const desc = detalle.descuento;

    const descuentoTotal = this.simularDescuento(base, qty, desc);

    detalle.movimientoDetalleDescuentoAplicado = descuentoTotal / qty;
    detalle.movimientoDetallePrecioUnitario = base - (descuentoTotal / qty);
    detalle.movimientoDetallePrecioTotal =
      detalle.movimientoDetallePrecioUnitario * qty;
  }

  // =========================
  // UI ACTIONS
  // =========================

  resetProducto() {
    this.codigo = '';
    this.cantidad = 1;
    this.productoEncontrado = null;
  }

  aumentar() { this.cantidad++; }

  disminuir() {
    if (this.cantidad > 1) this.cantidad--;
  }

onDetalleCantidadChange(detalle: any, value: number) {
  const qty = Number(value);

  if (!qty || qty < 1) {
    detalle.movimientoDetalleCantidad = 1;
  } else {
    detalle.movimientoDetalleCantidad = Math.floor(qty);
  }

  this.recalcularLinea(detalle);
  this.recalcularTotales();
}

  eliminarDetalle(detalle: any) {
    this.detalles = this.detalles.filter(d => d !== detalle);
    this.recalcularTotales();
  }

  aumentarDetalle(detalle: any) {
    detalle.movimientoDetalleCantidad++;
    this.recalcularLinea(detalle);
    this.recalcularTotales();
  }

  disminuirDetalle(detalle: any) {

    if (detalle.movimientoDetalleCantidad <= 1) {
      this.eliminarDetalle(detalle);
      return;
    }

    detalle.movimientoDetalleCantidad--;
    this.recalcularLinea(detalle);
    this.recalcularTotales();
  }

  // =========================
  // TOTALS
  // =========================

  recalcularTotales() {

    const subtotal = this.detalles.reduce(
      (sum, d) => sum + d.movimientoDetallePrecioTotal,
      0
    );

    const conDescuento = Math.max(0, subtotal - (this.descuentoHeader || 0));

    this.total = subtotal;
    this.totalFinal = conDescuento;

    const divisor = 1 + (this.ivaPct / 100);
    const neto = conDescuento / divisor;

    this.iva = conDescuento - neto;
  }

  // =========================
  // FINALIZE
  // =========================

async finalizarVenta() {

if (!this.metodoPago) {
  this.errorCode = 'ERR_VALIDATION';
  this.errorMessage = 'Seleccione método de pago';
  return;
}

if (this.detalles.length === 0) {
  this.errorCode = 'ERR_VALIDATION';
  this.errorMessage = 'No hay productos';
  return;
}

  // =========================
  // 💳 TARJETA FLOW (SIMULATED)
  // =========================
  if (this.metodoPago === 'TARJETA') {

    this.pagoTarjetaService.procesarPago({
      detalles: this.detalles,
      totalFinal: this.totalFinal,
      metodoPago: this.metodoPago
    });

    return;
  }

  // =========================
  // CREATE MOVIMIENTO (FULL ATOMIC PAYLOAD)
  // =========================
  const payload = {
    movimiento: {
      movimientoTipo: 'SALIDA',
      movimientoEstado: 'PENDIENTE',
      movimientoMetodoPago: this.metodoPago
    },
    detalles: this.detalles.map(d => ({
      productoId: d.productoId,
      movimientoDetalleCantidad: d.movimientoDetalleCantidad,
      movimientoDetalleUnidadesPorPaquete: 1,
      movimientoDetalleDescripcion: d.movimientoDetalleDescripcion || null,
      movimientoDetallePrecioBase: d.movimientoDetallePrecioBase,
      movimientoDetallePrecioUnitario: d.movimientoDetallePrecioUnitario,
      movimientoDetallePrecioTotal: d.movimientoDetallePrecioTotal,
      movimientoDetalleDescuentoAplicado: d.movimientoDetalleDescuentoAplicado ?? 0
    }))
  };

  try {

    // 🔹 1. Create movimiento + detalles (atomic backend)
    const mov: any = await this.ventasService
      .createMovimiento(payload)
      .toPromise();

    const movimientoId = mov.movimientoId;

    // 🔹 2. Confirm movimiento
    await this.ventasService
      .confirmarMovimiento(movimientoId)
      .toPromise();

    // =========================
    // 🧾 BUILD BOLETA
    // =========================
    const boleta = {
      movimientoId: movimientoId,
      fecha: new Date(),
      metodoPago: this.metodoPago,

      empresa: {
        nombre: this.config?.empresaNombre || '',
        run: `${this.config?.empresaRun || ''}-${this.config?.empresaDV || ''}`,
        direccion: this.config?.empresaDireccion || ''
      },

      detalles: this.detalles.map(d => ({
        nombre: d.productoNombre,
        cantidad: d.movimientoDetalleCantidad,
        precioUnitario: d.movimientoDetallePrecioUnitario,
        descuento: d.movimientoDetalleDescuentoAplicado,
        total: d.movimientoDetallePrecioTotal
      })),

      subtotal: this.total,
      descuentoGlobal: this.descuentoHeader || 0,
      iva: this.iva,
      total: this.totalFinal
    };

    // 🔹 3. Print boleta
    this.boletaService.print(boleta);

    this.errorMessage = null;
    this.errorCode = null;

    this.successMessage = 'Venta registrada correctamente';
    this.errorMessage = null;
    this.errorCode = null;

    // 🔹 4. Reset UI
    this.detalles = [];
    this.total = 0;
    this.iva = 0;
    this.totalFinal = 0;
    this.descuentoHeader = 0;

    this.router.navigate(['/ventas']);

} catch (err: any) {

  console.error(err);

  // store error for UI
  this.errorMessage = err?.message || null;
  this.errorCode = err?.errorCode || null;

  switch (err.errorCode) {

    case 'ERR_STOCK_NEGATIVE':
      this.errorMessage = 'No hay suficiente stock para completar la operación';
      break;

    case 'ERR_DUPLICATE':
      this.errorMessage = 'Registro duplicado';
      break;

    case 'ERR_FK_CONSTRAINT':
    case 'ERR_FK_MOVIMIENTO_DETALLE':
    case 'ERR_FK_PRODUCTO':
      this.errorMessage = 'Error de referencia (producto o movimiento inválido)';
      break;

    case 'ERR_MOVIMIENTO_ESTADO_INVALID':
      this.errorMessage = 'Estado de movimiento inválido';
      break;

    case 'ERR_INTERNAL':
      this.errorMessage = 'Error interno del sistema';
      break;

    default:
      this.errorMessage = err?.message || 'Error inesperado';
      break;
  }
}
}


volver() {
  this.location.back();
}

  // =========================
  // CAMERA
  // =========================

  abrirEscaner() {
    this.escanerAbierto = true;
    setTimeout(() => this.iniciarCamara(), 100);
  }

  iniciarCamara() {
    this.codeReader.decodeFromVideoDevice(
      undefined,
      this.videoElement.nativeElement,
      (result: any) => {
        if (result) this.manejarEscaneo(result.getText());
      }
    ).then((c: any) => this.controlesCamara = c)
     .catch(() => {});
  }

  manejarEscaneo(codigo: string) {
    const beep = new Audio('/sonidos/store-scanner-beep.mp3');
    beep.play().catch(() => {});

    this.codigo = codigo.trim();
    this.cerrarEscaner();
    this.buscarProducto();
  }

  cerrarEscaner() {
    this.escanerAbierto = false;

    if (this.controlesCamara) {
      this.controlesCamara.stop();
      this.controlesCamara = null;
    }
  }

}