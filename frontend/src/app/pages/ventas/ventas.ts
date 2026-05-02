import { Component, ViewChild, ElementRef, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BrowserMultiFormatReader } from '@zxing/browser';

//angular materials
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Location } from '@angular/common';

import { PagoTarjeta } from '../../services/pago-tarjeta';
import { VentasService } from '../../services/ventas/ventas';
import { BoletaService } from '../../services/boleta';
import { BoletaPrintComponent } from '../../components/boleta-print/boleta-print';
import { ApiError } from '../../core/errors/api-error';


@Component({
  selector: 'app-ventas-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatToolbarModule,
    BoletaPrintComponent
  ],
  templateUrl: './ventas.html',
  styleUrls: ['./ventas.css']
})
export class VentasForm implements OnInit, AfterViewInit {

  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;

  // 🔥 MÁQUINA DE ESTADOS (Fase 1: Solo usaremos ESCANEAR por ahora)
  estadoCaja: 'ESCANEAR' | 'CHECKOUT' | 'TARJETA' | 'EFECTIVO' | 'BOLETA' = 'ESCANEAR';

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
  descuentoHeader: number = 0;
  montoEntregado: number | null = null;
  vuelto: number = 0;
  datosBoleta: any = null;
  ventaProcesando = false; // Para evitar doble clic mientras guarda en la BD


  // Variables de la cámara
  codeReader = new BrowserMultiFormatReader();
  controlesCamara: any;
  escaneandoBloqueado = false; // 🔥 Para evitar escaneos duplicados en 1 segundo

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

  // 🔥 Encendemos la cámara automáticamente apenas carga la pantalla
  ngAfterViewInit() {
    setTimeout(() => {
      this.iniciarCamara();
    }, 500);
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
  // CAMERA CONTINUA
  // =========================
  iniciarCamara() {
    this.codeReader.decodeFromVideoDevice(
      undefined,
      this.videoElement.nativeElement,
      (result: any) => {
        if (result && !this.escaneandoBloqueado) {
          this.manejarEscaneo(result.getText());
        }
      }
    ).then((c: any) => this.controlesCamara = c)
     .catch((err) => console.log('Error de cámara', err));
  }

  manejarEscaneo(codigoEscaneado: string) {
    this.escaneandoBloqueado = true; // Bloqueamos para no leer 10 veces seguidas

    const beep = new Audio('/sonidos/store-scanner-beep.mp3');
    beep.play().catch(() => {});

    this.codigo = codigoEscaneado.trim();
    this.buscarProducto();

    // Desbloqueamos la cámara después de 1.5 segundos
    setTimeout(() => {
      this.escaneandoBloqueado = false;
      this.codigo = ''; // Limpiamos el input visualmente
    }, 1500);
  }

  cerrarCamara() {
    if (this.controlesCamara) {
      this.controlesCamara.stop();
      this.controlesCamara = null;
    }
  }

  // =========================
  // PRODUCTO & CARRITO
  // =========================
  buscarProducto() {
    if (!this.codigo?.trim()) return;

    const codigoLimpio = this.codigo.trim();
    this.errorMessage = null;
    this.successMessage = null;

    this.ventasService.getProductoByCodigo(codigoLimpio).subscribe({
      next: (prod: any) => {
        this.productoEncontrado = prod;
        this.cantidad = 1;
        this.agregarProductoLocal(); // Lo agregamos automático al carrito
      },
      error: (err) => {
        this.productoEncontrado = null;
        this.errorMessage = `Producto no encontrado: ${codigoLimpio}`;
        const errorBeep = new Audio('/assets/sonidos/error-beep.mp3'); // Opcional si tienes un sonido de error
        errorBeep.play().catch(() => {});
      }
    });
  }

  // (Mantuve intacta toda tu lógica de simularDescuento, agregarProductoLocal, etc.)
  private simularDescuento(base: number, cantidad: number, desc: any): number {
    if (!desc || desc.descuentoActivo === false) return 0;
    const tipo = desc.descuentoTipo;
    const v1 = Number(desc.descuentoValor ?? 0);
    const v2 = Number(desc.descuentoValorSecundario ?? 0);
    const total = base * cantidad;

    if (tipo === 'FLAT') return v1 * cantidad;
    if (tipo === 'PORCENTAJE') return total * (v1 / 100);
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

  agregarProductoLocal() {
    if (!this.productoEncontrado) return;
    
    const base = this.productoEncontrado.productoPrecio;
    const qty = this.cantidad;
    const desc = this.productoEncontrado?.descuento;
    const descuentoTotal = this.simularDescuento(base, qty, desc);
    
    const existing = this.detalles.find(d => d.productoId === this.productoEncontrado.productoId);

    if (existing) {
      existing.movimientoDetalleCantidad += qty;
      const newQty = existing.movimientoDetalleCantidad;
      const newDesc = this.simularDescuento(base, newQty, existing.descuento);

      existing.movimientoDetalleDescuentoAplicado = newDesc / newQty;
      existing.movimientoDetallePrecioBase = base;
      existing.movimientoDetallePrecioUnitario = Math.floor( base - (newDesc / newQty));
      existing.movimientoDetallePrecioTotal = Math.floor(
        existing.movimientoDetallePrecioUnitario * newQty
      );
    } else {
      this.detalles.unshift({ // 🔥 unshift lo pone arriba de la lista para que lo veas de inmediato
        productoId: this.productoEncontrado.productoId,
        productoNombre: this.productoEncontrado.productoNombre,
        descuento: desc, 
        movimientoDetalleCantidad: qty,
        movimientoDetallePrecioBase: base,
        movimientoDetalleDescuentoAplicado: descuentoTotal / qty,
        movimientoDetallePrecioUnitario: Math.floor(  base - (descuentoTotal / qty)),
        movimientoDetallePrecioTotal: Math.floor(
  (base - (descuentoTotal / qty)) * qty
)
      });
    }

    this.recalcularTotales();
    this.productoEncontrado = null; // Limpiamos para el siguiente
  }

  private recalcularLinea(detalle: any) {
    const base = detalle.movimientoDetallePrecioBase;
    const qty = detalle.movimientoDetalleCantidad;
    const desc = detalle.descuento;
    const descuentoTotal = this.simularDescuento(base, qty, desc);

    detalle.movimientoDetalleDescuentoAplicado = descuentoTotal / qty;
    detalle.movimientoDetallePrecioUnitario = base - (descuentoTotal / qty);
    detalle.movimientoDetallePrecioTotal = Math.floor(
      detalle.movimientoDetallePrecioUnitario * qty
      );
  }

  onDetalleCantidadChange(detalle: any, value: number) {
    const qty = Number(value);
    detalle.movimientoDetalleCantidad = (!qty || qty < 1) ? 1 : Math.floor(qty);
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

  recalcularTotales() {
    const subtotal = this.detalles.reduce((sum, d) => sum + d.movimientoDetallePrecioTotal, 0);
    const conDescuento = Math.max(0, subtotal - (this.descuentoHeader || 0));

    this.total = Math.floor(subtotal);
    this.totalFinal = Math.floor(conDescuento);

    const divisor = 1 + (this.ivaPct / 100);
    const neto = conDescuento / divisor;

    this.iva = Math.floor(conDescuento - neto);
  }

  // =========================
  // FASE 2: IR AL CHECKOUT
  // =========================
  volver() {
    this.cerrarCamara();
    this.location.back();
  }

  irACheckout() {
    if (this.detalles.length === 0) {
      this.errorMessage = "Debes agregar al menos un producto.";
      return;
    }
    this.cerrarCamara(); // 📷 Apagamos la cámara al pagar
    this.estadoCaja = 'CHECKOUT';
    this.errorMessage = null;
  }

  volverAEscanear() {
    this.estadoCaja = 'ESCANEAR';
    // Re-encendemos la cámara después de un breve delay para que el HTML se renderice
    setTimeout(() => {
      this.iniciarCamara();
    }, 100);
  }

  seleccionarEfectivo() {
    // Aquí saltaremos a la Fase 3: Calculadora
    this.metodoPago = 'EFECTIVO';
    this.estadoCaja = 'EFECTIVO';
  }

  seleccionarTarjeta() {
    // Aquí saltaremos a la Fase 3: Transbank
    this.metodoPago = 'TARJETA';
    this.estadoCaja = 'TARJETA';
  }

  // =========================
  // FASE 3: LÓGICA DE PAGO
  // =========================

  calcularVuelto() {
    const entregado = Number(this.montoEntregado) || 0;
    this.vuelto = entregado - this.totalFinal;
  }

  pagarEfectivo() {
    if ((this.montoEntregado || 0) < this.totalFinal) {
      this.errorMessage = "El monto entregado es menor al total.";
      return;
    }
    this.errorMessage = null;
    this.finalizarVenta(); // 🔥 Llamamos a la BD
  }

  simularPagoAprobado() {
    this.errorMessage = null;
    this.finalizarVenta(); // 🔥 Llamamos a la BD
  }

  simularPagoRechazado() {
    this.errorMessage = "❌ El pago fue rechazado por el banco. Intente con otro medio de pago.";
    this.estadoCaja = 'CHECKOUT'; 
  }

  cancelarPago() {
    this.estadoCaja = 'CHECKOUT';
    this.montoEntregado = null;
    this.vuelto = 0;
    this.errorMessage = null;
  }

  // =========================
  // FASE 4: BASE DE DATOS Y BOLETA
  // =========================
  async finalizarVenta() {
    this.ventaProcesando = true; // Bloqueamos botones

    const payload = {
      movimiento: {
        movimientoTipo: 'SALIDA',
        movimientoEstado: 'PENDIENTE',
        movimientoMetodoPago: this.metodoPago,
        movimientoDescuento: this.descuentoHeader || 0
      },
      detalles: this.detalles.map(d => ({
        productoId: d.productoId,
        movimientoDetalleCantidad: d.movimientoDetalleCantidad,
        movimientoDetalleUnidadesPorPaquete: 1,
        movimientoDetalleDescripcion: 'Venta POS',
        movimientoDetallePrecioBase: d.movimientoDetallePrecioBase,
        movimientoDetallePrecioUnitario: d.movimientoDetallePrecioUnitario,
        movimientoDetallePrecioTotal: d.movimientoDetallePrecioTotal,
        movimientoDetalleDescuentoAplicado: d.movimientoDetalleDescuentoAplicado ?? 0
      }))
    };

    try {
      // 1. Guardar en Base de Datos (Cabecera y Detalles)
      const mov: any = await this.ventasService.createMovimiento(payload).toPromise();
      const movimientoId = mov.movimientoId;

      // 2. Confirmar el movimiento para restar el stock real
      await this.ventasService.confirmarMovimiento(movimientoId).toPromise();

      // 3. Armar los datos para el componente de la Boleta
      this.datosBoleta = {
        movimientoId: movimientoId,
        folio: movimientoId,
        fecha: new Date(),
        metodoPago: this.metodoPago,
        empresa: {
          nombre: this.config?.empresaNombre || 'Mi Minimarket',
          rut: `${this.config?.empresaRun || ''}-${this.config?.empresaDV || ''}`,
          direccion: this.config?.empresaDireccion || 'Dirección no configurada'
        },
        items: this.detalles.map(d => ({
          nombre: d.productoNombre,
          cantidad: d.movimientoDetalleCantidad,
          precioUnitario: d.movimientoDetallePrecioUnitario,
          descuento: d.movimientoDetalleDescuentoAplicado,
          total: d.movimientoDetallePrecioTotal
        })),
        subtotal: this.total,
        descuentoGlobal: this.descuentoHeader || 0,
        iva: this.iva,
        total: this.totalFinal,
        vuelto: this.vuelto
      };

      // 4. Mostrar pantalla de éxito
      this.ventaProcesando = false;
      this.estadoCaja = 'BOLETA';

    } catch (err: any) {
      console.error(err);
      this.ventaProcesando = false;
      this.errorMessage = err?.message || 'Error al guardar la venta en la Base de Datos.';
      this.estadoCaja = 'CHECKOUT'; // Devolvemos para intentar de nuevo
    }
  }

  imprimirBoletaFisica() {
    if (this.datosBoleta) {
      // Tu servicio que genera el PDF o llama a la impresora térmica
      this.boletaService.print(this.datosBoleta); 
    }
  }

  nuevaVenta() {
    // Reseteamos absolutamente todo a cero
    this.detalles = [];
    this.total = 0;
    this.iva = 0;
    this.totalFinal = 0;
    this.montoEntregado = null;
    this.vuelto = 0;
    this.datosBoleta = null;
    this.codigo = '';
    
    // Encendemos la cámara de nuevo
    this.volverAEscanear();
  }
}
