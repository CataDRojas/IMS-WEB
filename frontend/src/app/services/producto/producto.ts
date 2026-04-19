import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
// Importamos las "cajas" que ya existen para armar el Producto
import { Categoria } from '../categoria/categoria';
import { Descuento } from '../descuento/descuento';

// Definimos exactamente la misma estructura que tiene Javier en Java
export interface Producto {
  productoId?: number;
  productoNombre: string;
  productoDesc: string;
  productoActivo: boolean;      // No borramos, solo apagamos
  productoStock: number;        // Cantidad actual
  productoStockCritico: boolean;// RF007: ¿Tiene alerta?
  productoCriticoNumero: number;// RF007: ¿En qué número avisa?
  productoPrecio: number;
  productoCantidadLote: number;
  productoCodigo: string;       // Código de barras
  categoria: Categoria | null;  // Obligatorio en BD
  descuento?: Descuento | null; // Opcional
}

@Injectable({
  providedIn: 'root'
})
export class ProductoService {

  private apiUrl = 'http://localhost:8080/api/productos'; // Asumimos que Javier usará esta ruta

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const usuarioActual = localStorage.getItem('usuario_email') || 'admin@ims.cl';
    return new HttpHeaders().set('X-User', usuarioActual);
  }

  obtenerProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  crearProducto(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto, { headers: this.getHeaders() });
  }

  actualizarProducto(id: number, producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto, { headers: this.getHeaders() });
  }

  // Desactivar en vez de eliminar
  desactivarProducto(id: number): Observable<void> {
    // Usamos PATCH o PUT dependiendo de cómo lo haga Javier. Usaremos una ruta especial o el mismo PUT.
    // Por ahora lo preparamos como un DELETE lógico si Javier hace un endpoint /deactivate
    return this.http.patch<void>(`${this.apiUrl}/${id}/deactivate`, {}, { headers: this.getHeaders() });
  }

  // --- MÉTODOS DE EXCEL ---
  importarExcel(archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', archivo);
    // Nota: No enviamos X-User aquí porque el controlador de Javier usa multipart/form-data
    return this.http.post(`${this.apiUrl}/import-excel`, formData, { responseType: 'text' });
  }

  exportarExcel(): Observable<Blob> {
    // Pedimos el archivo Excel como un "Blob" (datos binarios)
    return this.http.get(`${this.apiUrl}/export-excel`, { responseType: 'blob' });
  }
}