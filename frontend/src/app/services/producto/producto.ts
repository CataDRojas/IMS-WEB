import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Producto {
  productoId?: number;
  productoNombre: string;
  productoDesc: string;
  productoActivo: boolean;

  // READ-ONLY (derived from backend MLP sync)
  productoStock: number;

  productoStockCritico: boolean;
  productoCriticoNumero: number;
  productoPrecio: number;
  productoCantidadLote: number;
  productoCodigo: string;

  categoriaId: number | null;
  categoriaNombre?: string | null;

  descuentoId?: number | null;
  descuentoNombre?: string | null;
  descuentoPorcentaje?: number | null;

  categoria?: any;
  descuento?: any;
}

// NEW: stock per location DTO
export interface ProductoStockLugar {
  movimientoLugarId: number;
  movimientoLugarDescripcion: string;
  stock: number;
  prioridad: boolean;
}

export interface ProductoUiData {
  productos: Producto[];
  categorias: any[];
  descuentos: any[];
}

// NEW: full product detail response (matches backend DTO)
export interface ProductoDetalle {
  productoId: number;
  productoNombre: string;
  productoCodigo: string;
  productoPrecio: number;

  productoStock: number;
  stockPorLugar: ProductoStockLugar[];
}

export interface ProductoList {
  productoId: number;
  productoNombre: string;
  productoCodigo: string;

  productoPrecio: number;
  productoStock: number;
  productoActivo: boolean;

  categoriaNombre?: string | null;

  // resolved backend field (product OR category OR null)
  descuentoNombre?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ProductoService {

  private apiUrl = 'http://localhost:8080/api/productos';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const usuarioActual = localStorage.getItem('usuario_email') || 'admin@ims.cl';
    return new HttpHeaders().set('X-User', usuarioActual);
  }

  obtenerUiData(): Observable<ProductoUiData> {
    return this.http.get<ProductoUiData>(
      `${this.apiUrl}/ui-data`,
      { headers: this.getHeaders() }
    );
  }

  obtenerProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl, {
      headers: this.getHeaders()
    });
  }

  obtenerProductoPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  obtenerPorCodigo(codigo: string): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/codigo/${codigo}`, {
      headers: this.getHeaders()
    });
  }

  crearProducto(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto, {
      headers: this.getHeaders()
    });
  }

  actualizarProducto(id: number, producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto, {
      headers: this.getHeaders()
    });
  }

  // NEW: product + stock per location
  obtenerDetalleProducto(id: number): Observable<ProductoDetalle> {
    return this.http.get<ProductoDetalle>(
      `${this.apiUrl}/${id}/detalle`,
      { headers: this.getHeaders() }
    );
  }

  // EXCEL
  importarExcel(archivo: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', archivo);

    return this.http.post(
      `${this.apiUrl}/import-excel`,
      formData,
      {
        headers: this.getHeaders(),
        responseType: 'text'
      }
    );
  }

  exportarExcel(): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/export-excel`,
      {
        headers: this.getHeaders(),
        responseType: 'blob'
      }
    );
  }
}