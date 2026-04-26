import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { InventarioService } from '../../../services/inventario/inventario';
import { BrowserMultiFormatReader } from '@zxing/browser';

// Angular Material
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';

@Component({
  selector: 'app-inventario-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MatInputModule, MatButtonModule, MatCardModule, MatToolbarModule, MatSelectModule, MatOptionModule],
  templateUrl: './inventario-form.html',
  styleUrls: ['./inventario-form.css'],
})
export class InventarioForm implements OnInit {
  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;

  //  VARIABLES DE ESTADO 
  estado: 'inicio' | 'agregar' | 'lista' = 'inicio';
  codigo = '';
  cantidad = 1;
  cajas = 0;
  loteEditable = 1;

  productoEncontrado: any = null;
  productoEditando: any = null;
  
  //  GESTIÓN MULTI-SESIÓN 
  inventariosPendientes: any[] = [];
  inventarioActual: any = null;
  lugares: any[] = [];
  lugarSeleccionado: any = null;

  // VARIABLES DE CÁMARA 
  escanerAbierto = false;
  codeReader = new BrowserMultiFormatReader();
  controlesCamara: any;

  constructor(private router: Router, private location: Location, private inventarioService: InventarioService) {}

  ngOnInit() {
    this.cargarLugares();
    this.cargarSesiones();
  }

  cargarLugares() {
    this.inventarioService.obtenerLugaresActivos().subscribe({
      next: (data: any[]) => this.lugares = data,
      error: (err: any) => console.error('Error lugares:', err)
    });
  }

  cargarSesiones() {
    const saved = localStorage.getItem('inventarios_ims');
    this.inventariosPendientes = saved ? JSON.parse(saved) : [];
  }

  iniciarNuevo() {
    const nombre = prompt('Nombre del inventario (ej: Lácteos Mañana):');
    if (!nombre) return;
    
    this.inventarioActual = { nombre, lista: [], fecha: new Date() };
    this.inventariosPendientes.push(this.inventarioActual);
    this.guardarEnStorage();
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
    this.cantidad = this.cajas * this.loteEditable;
  }

  agregarProducto() {
    if (!this.lugarSeleccionado) return alert('Debes seleccionar una ubicación');

    const indexExistente = this.inventarioActual.lista.findIndex((p: any) => 
      p.productoId === this.productoEncontrado.productoId && 
      p.lugarId === this.lugarSeleccionado.movimientoLugarId
    );

    const itemData = {
      ...this.productoEncontrado,
      cantidadAgregada: this.cantidad,
      productoCantidadLote: this.loteEditable,
      lugarId: this.lugarSeleccionado.movimientoLugarId,
      lugarNombre: this.lugarSeleccionado.movimientoLugarDescripcion
    };

    if (this.productoEditando) {
      const idx = this.inventarioActual.lista.indexOf(this.productoEditando);
      this.inventarioActual.lista[idx] = itemData;
      this.productoEditando = null;
    } else if (indexExistente !== -1) {
      this.inventarioActual.lista[indexExistente].cantidadAgregada += this.cantidad;
    } else {
      this.inventarioActual.lista.push(itemData);
    }

    this.guardarEnStorage();
    this.reset();
    this.estado = 'lista';
  }

  seleccionarParaEditar(item: any) {
    this.productoEncontrado = { ...item };
    this.cantidad = item.cantidadAgregada;
    this.loteEditable = item.productoCantidadLote;
    this.lugarSeleccionado = this.lugares.find(l => l.movimientoLugarId === item.lugarId);
    this.productoEditando = item;
    this.estado = 'agregar';
  }

  eliminarIndividual(item: any) {
    if (confirm(`¿Eliminar ${item.productoNombre} del conteo?`)) {
      this.inventarioActual.lista = this.inventarioActual.lista.filter((i: any) => i !== item);
      this.guardarEnStorage();
    }
  }

  eliminarInventarioCompleto(inv: any) {
    if (confirm(`¿Borrar todo el inventario "${inv.nombre}"?`)) {
      this.inventariosPendientes = this.inventariosPendientes.filter(i => i !== inv);
      this.guardarEnStorage();
      this.estado = 'inicio';
    }
  }

  guardarEnStorage() {
    localStorage.setItem('inventarios_ims', JSON.stringify(this.inventariosPendientes));
  }

  volver() {
    // 1. Si estamos agregando o editando, volvemos a la lista del inventario actual
    if (this.estado === 'agregar') {
      this.estado = 'lista';
      this.reset();
      return;
    }

    // 2. Si estamos viendo la lista, volvemos al menú de selección (inicio)
    if (this.estado === 'lista') {
      this.estado = 'inicio';
      this.inventarioActual = null;
      return;
    }

    // 3. Si ya estamos en el inicio (el de tu foto), volvemos al Home del sistema
    if (this.estado === 'inicio') {
      this.router.navigate(['/home']);
    }
  }

  aumentar() { this.cantidad++; }
  disminuir() { if (this.cantidad > 1) this.cantidad--; }
  reset() { this.codigo = ''; this.cantidad = 1; this.cajas = 0; this.productoEncontrado = null; this.productoEditando = null; }
  
  // LÓGICA DE CÁMARA 
  abrirEscaner() { 
    this.escanerAbierto = true; 
    setTimeout(() => this.iniciarCamara(), 100); 
  }

  iniciarCamara() {
    this.codeReader.decodeFromVideoDevice(
      undefined, 
      this.videoElement.nativeElement, 
      (result: any) => {
        if (result) { 
          this.codigo = result.getText(); 
          this.cerrarEscaner(); 
          this.buscarProducto(); 
        }
      }
    ).then((c: any) => this.controlesCamara = c)
     .catch(err => console.error('Error cámara:', err));
  }

  cerrarEscaner() { 
    this.escanerAbierto = false; 
    if (this.controlesCamara) {
      this.controlesCamara.stop();
      this.controlesCamara = null;
    } 
  }
  
  irAHistorial() { this.router.navigate(['/inventario/historial']); }
  finalizar() { alert('Enviando inventario "' + this.inventarioActual.nombre + '" a la base de datos...'); }
}