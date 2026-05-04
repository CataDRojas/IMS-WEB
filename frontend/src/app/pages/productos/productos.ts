import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BrowserMultiFormatReader } from '@zxing/browser'; // CAMARA 

//Angular Materials
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatExpansionPanel } from '@angular/material/expansion';
import { MatButtonModule } from '@angular/material/button';

import { ProductoService, Producto } from '../../services/producto/producto';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, MatExpansionModule, MatExpansionPanel, MatIconModule, MatButtonModule],
  templateUrl: './productos.html',
  styleUrls: ['./productos.css']
})
export class ProductosComponent implements OnInit {

  @ViewChild('fileInput') fileInput!: ElementRef;

  productos: Producto[] = [];
  productosBase: Producto[] = [];
  categorias: any[] = [];
  descuentos: any[] = [];
  rolUsuario = localStorage.getItem('rol_ims') ?? 'Invitado';

  mostrarFormulario = false;

  mensajeError = '';
  mensajeExito = '';

  productoActual: Producto = this.generarProductoVacio();

  stockDetalleMap: Record<number, any[]> = {};
  productoExpandido: Record<number, boolean> = {};
  cargandoStock: Record<number, boolean> = {};

// =========================
// LIST CONTROL (ONLY UI STATE)
// =========================

// pagination
paginaActual = 1;
tamanoPagina = 10;
totalItems = 0;

filtros = {
  nombre: '',
  categoriaId: null as number | null,
  activo: '', // '' | 'true' | 'false'
  critico: false
};
showFilters: boolean = false;

  constructor(private productoService: ProductoService, private router: Router) {}

ngOnInit(): void {
  this.cargarDatos();
  this.cargarListaProductos();
}

  // Función rápida para chequear si es admin
  esAdmin(): boolean {
    return this.rolUsuario === 'ADMIN';
  }

  tienePermiso(permiso: string): boolean {
    const stored = localStorage.getItem('permisos_ims');
    const permisos = stored ? JSON.parse(stored) : [];
    return permisos.includes(permiso);
  }

  // CARGA DE DATOS

cargarDatos(): void {
  this.productoService.obtenerUiData().subscribe({
    next: (data) => {
      this.productosBase = data.productos;
      this.categorias = data.categorias;
      this.descuentos = data.descuentos;

      this.cargarListaProductos();
    },
    error: () => {
      this.mensajeError = 'No se pudo cargar la información del sistema.';
    }
  });
}

cargarListaProductos(): void {

  const productos = [...this.productosBase];

  let filtrados = productos;

  // nombre filter
  if (this.filtros.nombre?.trim()) {
    filtrados = filtrados.filter(p =>
      p.productoNombre
        .toLowerCase()
        .includes(this.filtros.nombre.toLowerCase())
    );
  }
console.log('FILTER:', this.filtros.categoriaId);
console.log('SAMPLE PRODUCT CATEGORY:', filtrados[0]?.categoria);
  // categoria filter
  if (this.filtros.categoriaId) {
    filtrados = filtrados.filter(p =>
      p.categoriaId === this.filtros.categoriaId
    );
  }

  // activo filter
  if (this.filtros.activo !== '') {
    const activo = this.filtros.activo === 'true';
    filtrados = filtrados.filter(p =>
      p.productoActivo === activo
    );
  }

  if (this.filtros.critico) {
    filtrados = filtrados.filter(p => p.productoStockCritico === true);
  }

  // IMPORTANT: update total BEFORE pagination math
  this.totalItems = filtrados.length;

  // clamp page if filters reduced dataset
  const maxPage = Math.max(1, Math.ceil(this.totalItems / this.tamanoPagina));

  if (this.paginaActual > maxPage) {
    this.paginaActual = maxPage;
  }

  // pagination
  const start = (this.paginaActual - 1) * this.tamanoPagina;
  const end = start + this.tamanoPagina;

  this.productos = filtrados.slice(start, end);
}



toggleDetalleStock(prod: Producto): void {

  if (!prod.productoId) return;

  const id = prod.productoId;

  // toggle close
  if (this.productoExpandido[id]) {
    this.productoExpandido[id] = false;
    return;
  }

  this.productoExpandido[id] = true;

  // already loaded → no refetch
  if (this.stockDetalleMap[id]) return;

  this.cargandoStock[id] = true;

  this.productoService.obtenerDetalleProducto(id).subscribe({
    next: (detalle) => {
      this.stockDetalleMap[id] = detalle.stockPorLugar;
      this.cargandoStock[id] = false;
    },
    error: () => {
      this.mensajeError = 'Error al cargar stock por lugar.';
      this.cargandoStock[id] = false;
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

getDescuentoNombre(prod: any): string {

  // product-level discount (DTO)
  if (prod.descuentoNombre) {
    return prod.descuentoNombre;
  }

  return '—';
}

editarProducto(prod: any): void {

  if (!prod.productoId) return;

  this.productoService.obtenerProductoPorId(prod.productoId).subscribe({
    next: (base) => {

      // resolve categoriaId
      let categoriaId = null;
      if (base.categoria?.categoriaId) {
        categoriaId = base.categoria.categoriaId;
      }

      // resolve descuentoId (ONLY real product discount)
      let descuentoId = null;
      if (base.descuento?.descuentoId) {
        descuentoId = base.descuento.descuentoId;
      }

      this.productoActual = {
        ...base,
        categoriaId: categoriaId,
        descuentoId: descuentoId
      };

      this.mensajeError = '';
      this.mostrarFormulario = true;
    },
    error: () => {
      this.mensajeError = 'Producto no encontrado.';
    }
  });
}

  cancelar(): void {
    this.mostrarFormulario = false;
  }

  // GUARDAR PRODUCTO

guardarProducto(): void {

  const {
    categoriaId,
    categoriaNombre,
    descuentoId,
    descuentoNombre,
    descuentoPorcentaje,
    productoStock,
    productoStockCritico,
    ...productoLimpio
  } = this.productoActual;

  const payloadEnvio = {
    ...productoLimpio,
    productoStock: null,

    categoria: this.productoActual.categoriaId
      ? { categoriaId: this.productoActual.categoriaId }
      : null,

    descuento: this.productoActual.descuentoId
      ? { descuentoId: this.productoActual.descuentoId }
      : null
  };

  const request$ = this.productoActual.productoId
    ? this.productoService.actualizarProducto(this.productoActual.productoId, payloadEnvio as any)
    : this.productoService.crearProducto(payloadEnvio as any);

  request$.subscribe({
    next: () => {
      this.mostrarFormulario = false;
      this.cargarDatos();
      this.cargarListaProductos();
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
      next: () => {
  this.cargarDatos();
  this.cargarListaProductos();
},
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
        this.cargarListaProductos();
        event.target.value = '';
      },
      error: () => {
        this.mensajeError = 'Error al importar Excel.';
        this.mensajeExito = '';
      }
    });
  }

descargarExcel(): void {
  const filtrosEnvio = {
    nombre: this.filtros.nombre || undefined,
    categoriaId: this.filtros.categoriaId || undefined,
    activo: this.filtros.activo && this.filtros.activo !== '' ? this.filtros.activo : undefined,
    critico: this.filtros.critico || undefined
  };

  console.log('Filtros al exportar:', filtrosEnvio);

  this.productoService.exportarExcel(filtrosEnvio).subscribe({
    next: (blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Inventario_IMS_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
    },
    error: () => { this.mensajeError = 'Error al exportar Excel.'; }
  });
}


// =========================
// LIST FILTERING
// =========================

aplicarFiltros(): void {
  this.paginaActual = 1;
  this.cargarListaProductos();
}

limpiarFiltros(): void {
  this.filtros = {
    nombre: '',
    categoriaId: null,
    activo: '',
    critico: false
  };

  this.paginaActual = 1;
  this.cargarListaProductos();
}



// =========================
// PAGINATION
// =========================

cambiarPagina(delta: number): void {
  const nueva = this.paginaActual + delta;

  if (nueva < 1) return;

  this.paginaActual = nueva;
  this.cargarListaProductos();
}

totalPaginas(): number {
  return Math.ceil(this.totalItems / this.tamanoPagina) || 1;
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