import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { InventarioService } from '../../../services/inventario/inventario';
import { BrowserMultiFormatReader } from '@zxing/browser';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';

@Component({
  selector: 'app-inventario-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MatInputModule, MatButtonModule,
    MatCardModule, MatToolbarModule, MatSelectModule, MatOptionModule],
  templateUrl: './inventario-form.html',
  styleUrls: ['./inventario-form.css'],
})
export class InventarioForm implements OnInit {
  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;

  estado: 'inicio' | 'agregar' | 'lista' = 'inicio';
  codigo = '';
  cantidad = 1;
  cajas = 0;
  loteEditable = 1;

  productoEncontrado: any = null;
  productoEditando: any = null;

  // Borradores locales (solo en memoria, no guardados en BD)
  inventariosLocales: any[] = [];
  // Borradores desde BD
  inventariosBD: any[] = [];

  inventarioActual: any = null;
  lugares: any[] = [];
  lugarSeleccionado: any = null;

  escanerAbierto = false;
  codeReader = new BrowserMultiFormatReader();
  controlesCamara: any;

  constructor(
    private router: Router,
    private location: Location,
    private inventarioService: InventarioService
  ) {}

  ngOnInit() {
    this.cargarLugares();
    this.cargarBorradoresBD();
    this.cargarLocales();
  }

  cargarLugares() {
    this.inventarioService.obtenerLugaresActivos().subscribe({
      next: (data: any[]) => this.lugares = data,
      error: (err: any) => console.error('Error lugares:', err)
    });
  }

  cargarLocales() {
    const saved = localStorage.getItem('inventarios_ims_local');
    this.inventariosLocales = saved ? JSON.parse(saved) : [];
  }

  cargarBorradoresBD() {
    this.inventarioService.obtenerBorradores().subscribe({
      next: (movimientos: any[]) => {
        this.inventariosBD = movimientos.map(m => ({
          nombre: m.movimientoDescripcion?.replace('Inventario: ', '') || 'Sin nombre',
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
    const nombre = prompt('Nombre del inventario (ej: Lácteos Mañana):');
    if (!nombre) return;

    this.inventarioActual = {
      nombre,
      lista: [],
      esBD: false,
      fecha: new Date()
    };
    this.inventariosLocales.push(this.inventarioActual);
    this.guardarLocales();
    this.estado = 'agregar';
  }

  seleccionarInventario(inv: any) {
    this.inventarioActual = inv;
    this.estado = 'lista';
  }

  buscarProducto() {
    if (!this.codigo) return;
    this.inventarioService.buscarProductoPorCodigo(this.codigo).subscribe({
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

    const indexExistente = this.inventarioActual.lista.findIndex((p: any) =>
      p.productoId === this.productoEncontrado.productoId &&
      p.lugarId === this.lugarSeleccionado.movimientoLugarId
    );

    const lote = this.cantidad

    const itemData = {
      ...this.productoEncontrado,
      cantidadAgregada: this.cantidad,
      cajasAgregadas: 1,
      productoCantidadLote: this.cantidad,
      lugarId: this.lugarSeleccionado.movimientoLugarId,
      lugarNombre: this.lugarSeleccionado.movimientoLugarDescripcion
    };

    if (this.productoEditando) {
      const idx = this.inventarioActual.lista.indexOf(this.productoEditando);
      this.inventarioActual.lista[idx] = itemData;
      this.productoEditando = null;
    } else if (indexExistente !== -1) {
      const existente = this.inventarioActual.lista[indexExistente];
      existente.cantidadAgregada += this.cantidad;
      existente.cajasAgregadas = 1;
      existente.productoCantidadLote = existente.cantidadAgregada;
    } else {
      this.inventarioActual.lista.push(itemData);
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
    if (confirm(`¿Eliminar ${item.productoNombre} del conteo?`)) {
      this.inventarioActual.lista = this.inventarioActual.lista.filter((i: any) => i !== item);
      this.guardarLocales();
    }
  }

  eliminarInventarioLocal(inv: any) {
    if (confirm(`¿Borrar el borrador "${inv.nombre}"?`)) {
      this.inventariosLocales = this.inventariosLocales.filter(i => i !== inv);
      this.guardarLocales();
      if (this.inventarioActual === inv) {
        this.inventarioActual = null;
        this.estado = 'inicio';
      }
    }
  }

  eliminarInventarioBD(inv: any) {
    if (!confirm(`¿Borrar el inventario guardado "${inv.nombre}"? Esto lo eliminará de la base de datos.`)) return;

    this.inventarioService.eliminarMovimiento(inv.movimientoId).subscribe({
      next: () => {
        this.inventariosBD = this.inventariosBD.filter(i => i !== inv);
        if (this.inventarioActual === inv) {
          this.inventarioActual = null;
          this.estado = 'inicio';
        }
      },
      error: (err) => {
        console.error('Error eliminando borrador BD:', err);
        alert('❌ Error al eliminar.');
      }
    });
  }

  guardarLocales() {
    localStorage.setItem('inventarios_ims_local', JSON.stringify(this.inventariosLocales));
  }

  async guardarBorrador() {
    if (!this.inventarioActual || this.inventarioActual.lista.length === 0) {
      alert('❌ No hay productos para guardar.');
      return;
    }

    if (!confirm(`¿Guardar "${this.inventarioActual.nombre}" como borrador? Podrás continuarlo después desde cualquier dispositivo.`)) return;

    try {
      await this.inventarioService.guardarBorrador(
        this.inventarioActual.nombre,
        this.inventarioActual.lista
      ).toPromise();

      alert('💾 Borrador guardado. Puedes continuarlo desde cualquier dispositivo.');

      // Eliminar de locales y recargar BD
      this.inventariosLocales = this.inventariosLocales.filter(i => i !== this.inventarioActual);
      this.guardarLocales();
      this.inventarioActual = null;
      this.estado = 'inicio';
      this.cargarBorradoresBD();

    } catch (err: any) {
      console.error('Error:', err);
      alert('❌ Error al guardar el borrador.');
    }
  }

  async finalizar() {
    if (!this.inventarioActual || this.inventarioActual.lista.length === 0) {
      alert('❌ No hay productos en el inventario actual.');
      return;
    }

    if (!confirm(`¿Finalizar y confirmar el inventario "${this.inventarioActual.nombre}"? Esto actualizará el stock.`)) return;

    try {
      let movimientoId: number;

      if (this.inventarioActual.esBD) {
        // Ya está en BD como PENDIENTE, solo confirmar
        movimientoId = this.inventarioActual.movimientoId;
      } else {
        // Está local, crear en BD y confirmar
        const res: any = await this.inventarioService.finalizarInventario(
          this.inventarioActual.nombre,
          this.inventarioActual.lista
        ).toPromise();
        movimientoId = res.movimientoId;
      }

      await this.inventarioService.confirmarMovimiento(movimientoId).toPromise();

      alert('✅ Inventario confirmado y stock actualizado.');

      if (this.inventarioActual.esBD) {
        this.inventariosBD = this.inventariosBD.filter(i => i !== this.inventarioActual);
      } else {
        this.inventariosLocales = this.inventariosLocales.filter(i => i !== this.inventarioActual);
        this.guardarLocales();
      }

      this.inventarioActual = null;
      this.estado = 'inicio';

    } catch (err: any) {
      console.error('Error:', err);
      alert('❌ Error al procesar. Revisa la consola.');
    }
  }

  volver() {
    if (this.estado === 'agregar') {
      this.estado = 'lista';
      this.reset();
      return;
    }
    if (this.estado === 'lista') {
      this.estado = 'inicio';
      this.inventarioActual = null;
      return;
    }
    if (this.estado === 'inicio') {
      this.router.navigate(['/home']);
    }
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

  abrirEscaner() {
    this.escanerAbierto = true;
    setTimeout(() => this.iniciarCamara(), 100);
  }

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

  irAHistorial() { this.router.navigate(['/inventario/historial']); }
}