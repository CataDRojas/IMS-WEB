import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BrowserMultiFormatReader } from '@zxing/browser';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';

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

  codigo = '';
  cantidad = 1;

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
  
  constructor(
    private router: Router,
    private ventasService: VentasService
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

    this.ventasService.getProductoByCodigo(codigoLimpio).subscribe({
      next: (prod: any) => {
        this.productoEncontrado = prod;
        this.cantidad = 1;
        this.agregarProductoLocal();
      },
      error: () => {
        this.productoEncontrado = null;
        alert('❌ Producto no encontrado');
      }
    });
  }

  // =========================
  // DISCOUNT SIMULATION (FRONTEND MIRROR)
  // =========================

  private simularDescuento(base: number, cantidad: number): number {

    const desc = this.productoEncontrado?.descuento;

    if (!desc) return 0;

    const tipo = desc.descuentoTipo;
    const v1 = Number(desc.descuentoValor ?? 0);
    const v2 = Number(desc.descuentoValorSecundario ?? 0);

    const total = base * cantidad;

    // FLAT = fixed per unit
    if (tipo === 'FLAT') {
      return v1 * cantidad;
    }

    // PORCENTAJE = % over total
    if (tipo === 'PORCENTAJE') {
      return total * (v1 / 100);
    }

    // MULTIPLICATIVO = buy X get Y free style
    // v1 = threshold (e.g. 2, 4, 10)
    // v2 = paid units per group (e.g. 1 in 2x1, or 3 in 4x3)

    if (tipo === 'MULTIPLICATIVO') {

      if (v1 <= 0) return 0;

      const groupSize = v1;
      const paidPerGroup = v2 > 0 ? v2 : (groupSize - 1);

      const fullGroups = Math.floor(cantidad / groupSize);
      const remainder = cantidad % groupSize;

      const payableUnits =
        (fullGroups * paidPerGroup) + remainder;

      const freeUnits = cantidad - payableUnits;

      return freeUnits * base;
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

    const descuentoTotal = this.simularDescuento(base, qty);
    const unitario = base - (descuentoTotal / qty);

    const existing = this.detalles.find(d =>
      d.productoId === this.productoEncontrado.productoId
    );

    if (existing) {

      existing.movimientoDetalleCantidad += qty;

      const newQty = existing.movimientoDetalleCantidad;
      const newDesc = this.simularDescuento(base, newQty);

      existing.movimientoDetalleDescuentoAplicado = newDesc / newQty;
      existing.movimientoDetallePrecioBase = base;
      existing.movimientoDetallePrecioUnitario = base - (newDesc / newQty);
      existing.movimientoDetallePrecioTotal =
        existing.movimientoDetallePrecioUnitario * newQty;

    } else {

      this.detalles.push({
        productoId: this.productoEncontrado.productoId,
        productoNombre: this.productoEncontrado.productoNombre,

        movimientoDetalleCantidad: qty,

        movimientoDetallePrecioBase: base,

        movimientoDetalleDescuentoAplicado: descuentoTotal / qty,

        movimientoDetallePrecioUnitario: unitario,

        movimientoDetallePrecioTotal: unitario * qty
      });
    }

    this.recalcularTotales();
    this.resetProducto();
  }

  resetProducto() {
    this.codigo = '';
    this.cantidad = 1;
    this.productoEncontrado = null;
  }

  aumentar() { this.cantidad++; }
  disminuir() { if (this.cantidad > 1) this.cantidad--; }

  eliminarDetalle(detalle: any) {
    this.detalles = this.detalles.filter(d => d !== detalle);
    this.recalcularTotales();
  }

  // =========================
  // TOTALS (MATCH BACKEND LOGIC)
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
  // FINALIZE (UNCHANGED FLOW)
  // =========================

  async finalizarVenta() {

    if (this.detalles.length === 0) {
      alert('No hay productos');
      return;
    }

    const payload = {
      movimientoTipo: 'SALIDA',
      movimientoEstado: 'PENDIENTE',
      movimientoMetodoPago: 'EFECTIVO'
    };

    try {

      const mov: any = await this.ventasService
        .createMovimiento(payload)
        .toPromise();

      const movimientoId = mov.movimientoId;

      for (const d of this.detalles) {

        await this.ventasService.createDetalle(movimientoId, {
          productoId: d.productoId,
          movimientoDetalleCantidad: d.movimientoDetalleCantidad,
          movimientoDetalleUnidadesPorPaquete: 1,
          movimientoDetalleDescripcion: d.movimientoDetalleDescripcion || null,
          movimientoDetallePrecioBase: d.movimientoDetallePrecioBase,
          movimientoDetallePrecioUnitario: d.movimientoDetallePrecioUnitario,
          movimientoDetallePrecioTotal: d.movimientoDetallePrecioTotal,
          movimientoDetalleDescuentoAplicado: d.movimientoDetalleDescuentoAplicado ?? 0
        }).toPromise();
      }

      await this.ventasService
        .confirmarMovimiento(movimientoId)
        .toPromise();

      alert('Venta registrada correctamente');
      this.router.navigate(['/ventas']);

    } catch (err) {
      console.error(err);
      alert('Error procesando venta');
    }
  }

  volver() {
    this.router.navigate(['/ventas']);
  }

  // =========================
  // CAMERA (UNCHANGED)
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

  aumentarDetalle(detalle: any) {
    detalle.movimientoDetalleCantidad++;
    this.recalcularTotales();
  }

  disminuirDetalle(detalle: any) {
    if (detalle.movimientoDetalleCantidad <= 1) {
      this.eliminarDetalle(detalle);
      return;
    }

    detalle.movimientoDetalleCantidad--;
    this.recalcularTotales();
  }
}