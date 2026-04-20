import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ProductoService, Producto } from '../../services/producto/producto';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productos.html',
  styleUrls: ['./productos.css']
})
export class ProductosComponent implements OnInit {

  @ViewChild('fileInput') fileInput!: ElementRef;

  productos: Producto[] = [];
  categorias: any[] = [];
  descuentos: any[] = [];

  mostrarFormulario = false;

  mensajeError = '';
  mensajeExito = '';

  productoActual: Producto = this.generarProductoVacio();

  constructor(private productoService: ProductoService) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  // =========================
  // DATA LOADING
  // =========================
  cargarDatos(): void {
    this.productoService.obtenerUiData().subscribe({
      next: (data) => {
        this.productos = data.productos;
        this.categorias = data.categorias;
        this.descuentos = data.descuentos;
      },
      error: () => {
        this.mensajeError = 'No se pudo cargar la información del sistema.';
      }
    });
  }

  // =========================
  // FORM STATE
  // =========================
  generarProductoVacio(): Producto {
    return {
      productoNombre: '',
      productoDesc: '',
      productoActivo: true,
      productoStock: 0,
      productoStockCritico: false,
      productoCriticoNumero: 0,
      productoPrecio: 0,
      productoCantidadLote: 1,
      productoCodigo: '',

      // flattened API contract
      categoriaId: null,
      categoriaNombre: null,

      descuentoId: null,
      descuentoNombre: null,
      descuentoPorcentaje: null
    };
  }

  abrirNuevo(): void {
    this.productoActual = this.generarProductoVacio();
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  editarProducto(prod: Producto): void {
    this.productoActual = { ...prod };
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  cancelar(): void {
    this.mostrarFormulario = false;
  }

  // =========================
  // SAVE FLOW
  // =========================
  guardarProducto(): void {

    const request$ = this.productoActual.productoId
      ? this.productoService.actualizarProducto(
          this.productoActual.productoId,
          this.productoActual
        )
      : this.productoService.crearProducto(this.productoActual);

    request$.subscribe({
      next: () => {
        this.mostrarFormulario = false;
        this.cargarDatos();
      },
      error: () => {
        this.mensajeError = 'Error al guardar producto.';
      }
    });
  }

  // =========================
  // STATUS TOGGLE
  // =========================
  cambiarEstado(prod: Producto): void {

    if (!prod.productoId) return;

    const updated: Producto = {
      ...prod,
      productoActivo: !prod.productoActivo
    };

    this.productoService.actualizarProducto(prod.productoId, updated).subscribe({
      next: () => this.cargarDatos(),
      error: () => {
        this.mensajeError = 'No se pudo cambiar el estado.';
      }
    });
  }

  // =========================
  // EXCEL FLOW
  // =========================
  activarSubidaExcel(): void {
    this.fileInput.nativeElement.click();
  }

  subirExcel(event: any): void {

    const archivo: File = event.target.files?.[0];
    if (!archivo) return;

    this.mensajeExito = 'Importando...';
    this.mensajeError = '';

    this.productoService.importarExcel(archivo).subscribe({
      next: () => {
        this.mensajeExito = 'Importación exitosa';
        this.cargarDatos();
        event.target.value = '';
      },
      error: () => {
        this.mensajeError = 'Error al importar Excel.';
        this.mensajeExito = '';
      }
    });
  }

  descargarExcel(): void {

    this.productoService.exportarExcel().subscribe({
      next: (blob) => {

        const url = window.URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = 'Inventario_IMS.xlsx';
        a.click();

        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.mensajeError = 'Error al exportar Excel.';
      }
    });
  }
}