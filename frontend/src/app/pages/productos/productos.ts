import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BrowserMultiFormatReader } from '@zxing/browser'; // CAMARA 

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

  constructor(private productoService: ProductoService, private router: Router) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  // CARGA DE DATOS

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

  editarProducto(prod: any): void {
    this.productoActual = { 
      ...prod,
      categoriaId: prod.categoria ? prod.categoria.categoriaId : null,
      descuentoId: prod.descuento ? prod.descuento.descuentoId : null
    };
    
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  cancelar(): void {
    this.mostrarFormulario = false;
  }

  // GUARDAR PRODUCTO

  guardarProducto(): void {

    const { 
      categoriaId, categoriaNombre, 
      descuentoId, descuentoNombre, descuentoPorcentaje, 
      productoStockCritico,
      ...productoLimpio 
    } = this.productoActual;

    const payloadEnvio = {
      ...productoLimpio,
      categoria: this.productoActual.categoriaId ? { categoriaId: this.productoActual.categoriaId } : null,
      descuento: this.productoActual.descuentoId ? { descuentoId: this.productoActual.descuentoId } : null
    };

    const request$ = this.productoActual.productoId
      ? this.productoService.actualizarProducto(this.productoActual.productoId, payloadEnvio as any)
      : this.productoService.crearProducto(payloadEnvio as any);

    request$.subscribe({
      next: () => {
        this.mostrarFormulario = false;
        this.cargarDatos();
      },
      error: (err) => {
        console.error('Error del backend:', err);
        this.mensajeError = 'Error al guardar producto. Revisa los datos.';
      }
    });
  }

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


  // EXCEL FLOW

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

  // CAMARA

  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;
  escanerAbierto = false;
  codeReader = new BrowserMultiFormatReader();
  controlesCamara: any;

  abrirEscaner() {
    this.escanerAbierto = true;
    setTimeout(() => {
      this.iniciarCamara();
    }, 100);
  }

  iniciarCamara() {
    this.codeReader.decodeFromVideoDevice(
      undefined, 
      this.videoElement.nativeElement, 
      (result, err) => {
        if (result) {
          this.manejarEscaneoExitoso(result.getText());
        }
      }
    ).then((controles) => {
      this.controlesCamara = controles; 
    }).catch(err => {
      console.error('Error al abrir la cámara:', err);
    });
  }

  cerrarEscaner() {
    this.escanerAbierto = false;
    
    if (this.controlesCamara) {
      this.controlesCamara.stop();
      this.controlesCamara = null;
    }
  }

  manejarEscaneoExitoso(codigo: string) {

    //SONIDO
    const sonidoCajero = new Audio('/sonidos/store-scanner-beep.mp3');

    sonidoCajero.play().catch(e => console.log('No funcó el sonido'));

    this.productoActual.productoCodigo = codigo;
    this.cerrarEscaner(); 
    
    this.mensajeExito = '¡Código escaneado correctamente!';
    setTimeout(() => this.mensajeExito = '', 3000);
  }

  // AQUI TERMINA LA CAMARITA

  goHome() {
    this.router.navigate(['/home']);
  }
}