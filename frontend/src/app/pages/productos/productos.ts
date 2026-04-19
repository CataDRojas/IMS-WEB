import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService, Producto } from '../../services/producto/producto';
import { CategoriaService, Categoria } from '../../services/categoria/categoria';
import { DescuentoService, Descuento } from '../../services/descuento/descuento';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productos.html',
  styleUrls: ['./productos.css']
})
export class ProductosComponent implements OnInit {

  // Referencia al input oculto de archivos para el Excel
  @ViewChild('fileInput') fileInput!: ElementRef;

  productos: Producto[] = [];
  categorias: Categoria[] = [];
  descuentos: Descuento[] = [];

  mostrarFormulario = false;
  mensajeError = '';
  mensajeExito = '';

  // Producto por defecto vacío para el formulario
  productoActual: Producto = this.generarProductoVacio();

  constructor(
    private productoService: ProductoService,
    private categoriaService: CategoriaService,
    private descuentoService: DescuentoService
  ) {}

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.productoService.obtenerProductos().subscribe(res => this.productos = res);
    this.categoriaService.obtenerCategorias().subscribe(res => this.categorias = res);
    this.descuentoService.obtenerDescuentosActivos().subscribe(res => this.descuentos = res);
  }

  generarProductoVacio(): Producto {
    return {
      productoNombre: '', productoDesc: '', productoActivo: true,
      productoStock: 0, productoStockCritico: false, productoCriticoNumero: 0,
      productoPrecio: 0, productoCantidadLote: 1, productoCodigo: '',
      categoria: null, descuento: null
    };
  }

  abrirNuevo() {
    this.productoActual = this.generarProductoVacio();
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  editarProducto(prod: Producto) {
    this.productoActual = { ...prod };
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  compararObjetos(obj1: any, obj2: any): boolean {
    return obj1 && obj2 ? obj1.categoriaId === obj2.categoriaId || obj1.descuentoId === obj2.descuentoId : obj1 === obj2;
  }

  guardarProducto() {
    if (this.productoActual.productoId) {
      this.productoService.actualizarProducto(this.productoActual.productoId, this.productoActual).subscribe({
        next: () => { this.cargarDatos(); this.mostrarFormulario = false; },
        error: (err) => this.mensajeError = 'Error: Revisa que el Código o Nombre no estén repetidos.'
      });
    } else {
      this.productoService.crearProducto(this.productoActual).subscribe({
        next: () => { this.cargarDatos(); this.mostrarFormulario = false; },
        error: (err) => this.mensajeError = 'Error al crear: El Código o Nombre ya existe.'
      });
    }
  }

  cambiarEstado(prod: Producto) {
    prod.productoActivo = !prod.productoActivo; // Alternamos el estado
    if (prod.productoId) {
      this.productoService.actualizarProducto(prod.productoId, prod).subscribe({
        next: () => this.cargarDatos(),
        error: () => this.mensajeError = 'No se pudo cambiar el estado.'
      });
    }
  }

  cancelar() {
    this.mostrarFormulario = false;
  }

  // --- MÉTODOS MÁGICOS DE EXCEL ---
  activarSubidaExcel() {
    this.fileInput.nativeElement.click(); // Simula un clic en el input de archivo oculto
  }

  subirExcel(event: any) {
    const archivo = event.target.files[0];
    if (archivo) {
      this.mensajeExito = 'Importando datos, por favor espera...';
      this.productoService.importarExcel(archivo).subscribe({
        next: () => {
          this.mensajeExito = '¡Excel importado correctamente!';
          this.cargarDatos(); // Recargamos la tabla
          event.target.value = ''; // Limpiamos el input
        },
        error: () => this.mensajeError = 'Hubo un error al procesar el Excel.'
      });
    }
  }

  descargarExcel() {
    this.productoService.exportarExcel().subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'Inventario_IMS.xlsx';
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }
}