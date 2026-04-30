import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { RecepcionService } from '../../../services/recepcion/recepcion';
import { BrowserMultiFormatReader } from '@zxing/browser';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';

@Component({
  selector: 'app-recepcion-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MatInputModule, MatButtonModule,
    MatCardModule, MatToolbarModule, MatSelectModule, MatOptionModule],
  templateUrl: './recepcion-form.html',
  styleUrls: ['./recepcion-form.css'],
})
export class RecepcionForm implements OnInit {
  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;

  estado: 'inicio' | 'agregar' | 'lista' = 'inicio';
  codigo = '';
  cantidad = 1;
  cajas = 0;
  loteEditable = 1;

  productoEncontrado: any = null;
  productoEditando: any = null;

  recepcionesLocales: any[] = [];
  recepcionesBD: any[] = [];

  recepcionActual: any = null;
  lugares: any[] = [];
  lugarSeleccionado: any = null;

  escanerAbierto = false;
  codeReader = new BrowserMultiFormatReader();
  controlesCamara: any;

  constructor(
    private router: Router,
    private location: Location,
    private recepcionService: RecepcionService
  ) {}

  ngOnInit() {
    this.cargarLugares();
    this.cargarBorradoresBD();
    this.cargarLocales();
  }

  cargarLugares() {
    this.recepcionService.obtenerLugaresActivos().subscribe({
      next: (data: any[]) => this.lugares = data,
      error: (err: any) => console.error('Error lugares:', err)
    });
  }

  cargarLocales() {
    const saved = localStorage.getItem('recepciones_ims_local');
    this.recepcionesLocales = saved ? JSON.parse(saved) : [];
  }

  cargarBorradoresBD() {
    this.recepcionService.obtenerBorradores().subscribe({
      next: (movimientos: any[]) => {
        this.recepcionesBD = movimientos.map(m => ({
          nombre: m.movimientoDescripcion?.replace('Recepción: ', '') || 'Sin nombre',
          movimientoId: m.movimientoId,
          esBD: true,
          lista: (m.detalles || []).map((d: any) => ({
            productoId: d.productoId,
            productoNombre: d.productoNombre,
            cantidadAgregada: d.movimientoDetalleCantidad * d.movimientoDetalleUnidadesPorPaquete,
            cajasAgregadas: d.movimientoDetalleCantidad,
            productoCantidadLote: d.movimientoDetalleUnidadesPorPaquete,
            lugarId: d.movimientoLugarId,
            lugarNombre: d.movimientoLugarNombre || '',
            productoPrecio: d.movimientoDetallePrecioBase
          })),
          fecha: m.movimientoFechaCreacion
        }));
      },
      error: (err) => console.error('Error cargando borradores BD:', err)
    });
  }

  iniciarNuevo() {
    const nombre = prompt('Nombre de la recepción (ej: Proveedor Lácteos):');
    if (!nombre) return;

    this.recepcionActual = {
      nombre,
      lista: [],
      esBD: false,
      fecha: new Date()
    };
    this.recepcionesLocales.push(this.recepcionActual);
    this.guardarLocales();
    this.estado = 'agregar';
  }

  seleccionarRecepcion(rec: any) {
    this.recepcionActual = rec;
    this.estado = 'lista';
  }

  buscarProducto() {
    if (!this.codigo) return;
    this.recepcionService.buscarProductoPorCodigo(this.codigo).subscribe({
      next: (producto: any) => {
        this.productoEncontrado = producto;
        this.loteEditable = producto.productoCantidadLote || 1;
        this.cantidad = 1;
        this.cajas = 0;
        this.lugarSeleccionado = null;
        this.estado = 'agregar';
      },
      error: () => alert('❌ Producto no encontrado.')
    });
  }

  calcularPorCajas() {
    if (this.cajas > 0) {
      this.cantidad = this.cajas * this.loteEditable;
    }
  }

  calcularPorUnidades() {
    if (this.loteEditable > 0) {
      this.cajas = Math.floor(this.cantidad / this.loteEditable);
    }
  }

  agregarProducto() {
    if (!this.lugarSeleccionado) return alert('Debes seleccionar una ubicación');

    const indexExistente = this.recepcionActual.lista.findIndex((p: any) =>
      p.productoId === this.productoEncontrado.productoId &&
      p.lugarId === this.lugarSeleccionado.movimientoLugarId
    );

    const itemData = {
      ...this.productoEncontrado,
      cantidadAgregada: this.cantidad,
      cajasAgregadas: 1,
      productoCantidadLote: this.cantidad,
      lugarId: this.lugarSeleccionado.movimientoLugarId,
      lugarNombre: this.lugarSeleccionado.movimientoLugarDescripcion
    };

    if (this.productoEditando) {
      const idx = this.recepcionActual.lista.indexOf(this.productoEditando);
      this.recepcionActual.lista[idx] = itemData;
      this.productoEditando = null;
    } else if (indexExistente !== -1) {
      const existente = this.recepcionActual.lista[indexExistente];
      existente.cantidadAgregada += this.cantidad;
      existente.cajasAgregadas = 1;
      existente.productoCantidadLote = existente.cantidadAgregada;
    } else {
      this.recepcionActual.lista.push(itemData);
    }

    this.guardarLocales();
    this.reset();
    this.estado = 'lista';
  }

  seleccionarParaEditar(item: any) {
    this.productoEncontrado = { ...item };
    this.cantidad = item.cantidadAgregada;
    this.loteEditable = item.productoCantidadLote;
    this.cajas = Math.floor(this.cantidad / this.loteEditable);
    this.lugarSeleccionado = this.lugares.find(l => l.movimientoLugarId === item.lugarId);
    this.productoEditando = item;
    this.estado = 'agregar';
  }

  eliminarIndividual(item: any) {
    if (confirm(`¿Eliminar ${item.productoNombre}?`)) {
      this.recepcionActual.lista = this.recepcionActual.lista.filter((i: any) => i !== item);
      this.guardarLocales();
    }
  }

  eliminarRecepcionLocal(rec: any) {
    if (confirm(`¿Borrar el borrador "${rec.nombre}"?`)) {
      this.recepcionesLocales = this.recepcionesLocales.filter(i => i !== rec);
      this.guardarLocales();
      if (this.recepcionActual === rec) {
        this.recepcionActual = null;
        this.estado = 'inicio';
      }
    }
  }

  eliminarRecepcionBD(rec: any) {
    if (!confirm(`¿Borrar la recepción "${rec.nombre}" de la base de datos?`)) return;

    this.recepcionService.eliminarMovimiento(rec.movimientoId).subscribe({
      next: () => {
        this.recepcionesBD = this.recepcionesBD.filter(i => i !== rec);
        if (this.recepcionActual === rec) {
          this.recepcionActual = null;
          this.estado = 'inicio';
        }
      },
      error: (err) => {
        console.error('Error eliminando:', err);
        alert('❌ Error al eliminar.');
      }
    });
  }

  guardarLocales() {
    localStorage.setItem('recepciones_ims_local', JSON.stringify(this.recepcionesLocales));
  }

  async guardarBorrador() {
    if (!this.recepcionActual || this.recepcionActual.lista.length === 0) {
      alert('❌ No hay productos para guardar.');
      return;
    }

    if (!confirm(`¿Guardar "${this.recepcionActual.nombre}" como borrador?`)) return;

    try {
      await this.recepcionService.guardarBorrador(
        this.recepcionActual.nombre,
        this.recepcionActual.lista
      ).toPromise();

      alert('💾 Borrador guardado.');
      this.recepcionesLocales = this.recepcionesLocales.filter(i => i !== this.recepcionActual);
      this.guardarLocales();
      this.recepcionActual = null;
      this.estado = 'inicio';
      this.cargarBorradoresBD();

    } catch (err: any) {
      console.error('Error:', err);
      alert('❌ Error al guardar.');
    }
  }

  async finalizar() {
    if (!this.recepcionActual || this.recepcionActual.lista.length === 0) {
      alert('❌ No hay productos en la recepción actual.');
      return;
    }

    if (!confirm(`¿Confirmar la recepción "${this.recepcionActual.nombre}"? Esto sumará el stock.`)) return;

    try {
      let movimientoId: number;

      if (this.recepcionActual.esBD) {
        movimientoId = this.recepcionActual.movimientoId;
      } else {
        const res: any = await this.recepcionService.finalizarRecepcion(
          this.recepcionActual.nombre,
          this.recepcionActual.lista
        ).toPromise();
        movimientoId = res.movimientoId;
      }

      await this.recepcionService.confirmarMovimiento(movimientoId).toPromise();

      alert('✅ Recepción confirmada y stock actualizado.');

      if (this.recepcionActual.esBD) {
        this.recepcionesBD = this.recepcionesBD.filter(i => i !== this.recepcionActual);
      } else {
        this.recepcionesLocales = this.recepcionesLocales.filter(i => i !== this.recepcionActual);
        this.guardarLocales();
      }

      this.recepcionActual = null;
      this.estado = 'inicio';

    } catch (err: any) {
      console.error('Error:', err);
      alert('❌ Error al procesar.');
    }
  }

  volver() {
    if (this.estado === 'agregar') { this.estado = 'lista'; this.reset(); return; }
    if (this.estado === 'lista') { this.estado = 'inicio'; this.recepcionActual = null; return; }
    if (this.estado === 'inicio') { this.router.navigate(['/home']); }
  }

  aumentar() { this.cantidad++; this.calcularPorUnidades(); }
  disminuir() { if (this.cantidad > 1) { this.cantidad--; this.calcularPorUnidades(); } }

  reset() {
    this.codigo = '';
    this.cantidad = 1;
    this.cajas = 0;
    this.productoEncontrado = null;
    this.productoEditando = null;
  }

  abrirEscaner() { this.escanerAbierto = true; setTimeout(() => this.iniciarCamara(), 100); }

  iniciarCamara() {
    this.codeReader.decodeFromVideoDevice(
      undefined,
      this.videoElement.nativeElement,
      (result: any) => {
        if (result) { this.codigo = result.getText(); this.cerrarEscaner(); this.buscarProducto(); }
      }
    ).then((c: any) => this.controlesCamara = c)
     .catch(err => console.error('Error cámara:', err));
  }

  cerrarEscaner() {
    this.escanerAbierto = false;
    if (this.controlesCamara) { this.controlesCamara.stop(); this.controlesCamara = null; }
  }

  irAHistorial() { this.router.navigate(['/recepcion/historial']); }
}