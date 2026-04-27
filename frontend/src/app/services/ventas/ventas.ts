import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class VentasService {

  private baseUrl = 'http://localhost:8080';

  private baseMovimientos = `${this.baseUrl}/api/movimientos`;
  private baseDetalles = `${this.baseUrl}/api/movimiento-detalles`;

  constructor(private http: HttpClient) {}

  // =========================
  // CONFIGURACION (FIXED)
  // =========================

  getConfiguracion(): Observable<any> {
    return this.http.get(`${this.baseMovimientos}/configuracion`);
  }

  // =========================
  // PRODUCTOS (FIXED ROUTE SOURCE)
  // =========================

  getProductoByCodigo(codigo: string): Observable<any> {
    return this.http.get(`${this.baseMovimientos}/productos/codigo/${codigo}`);
  }

  getProductoById(id: number): Observable<any> {
    return this.http.get(`${this.baseMovimientos}/productos/${id}`);
  }

  getAllProductos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseMovimientos}/productos`);
  }

  // =========================
  // MOVIMIENTO (UNCHANGED CONTRACT)
  // =========================

  createMovimiento(payload: {
    movimiento: {
      movimientoTipo: string;
      movimientoEstado: string;
      movimientoMetodoPago?: string;
      movimientoDescripcion?: string;
      movimientoDescuento?: number;
    };
    detalles: Array<{
      productoId: number;
      movimientoDetalleCantidad: number;
      movimientoDetalleUnidadesPorPaquete?: number;
      movimientoDetalleDescripcion?: string;

      movimientoDetallePrecioBase: number;
      movimientoDetallePrecioUnitario: number;
      movimientoDetallePrecioTotal: number;

      movimientoDetalleDescuentoAplicado?: number;
    }>;
  }): Observable<any> {

    const user = localStorage.getItem('user') || 'system';

    return this.http.post(`${this.baseMovimientos}`, payload, {
      headers: { 'X-User': user }
    });
  }

  getMovimientoById(id: number): Observable<any> {
    return this.http.get(`${this.baseMovimientos}/${id}`);
  }

  getMovimientos(): Observable<any[]> {
    return this.http.get<any[]>(this.baseMovimientos);
  }

  confirmarMovimiento(id: number): Observable<any> {

    const user = localStorage.getItem('user') || 'system';

    return this.http.post(
      `${this.baseMovimientos}/${id}/confirmar`,
      {},
      { headers: { 'X-User': user } }
    );
  }
// =========================
// SEARCH / FILTER + PAGINATION (NEW)
// =========================

getMovimientosPaginados(filtros: {
  tipo?: string;
  estado?: string;
  usuario?: string;
  desde?: string;
  hasta?: string;
  page?: number;
  size?: number;
}): Observable<any> {

  const params: any = {
    page: filtros.page ?? 0,
    size: filtros.size ?? 10
  };

  if (filtros.tipo) params.tipo = filtros.tipo;
  if (filtros.estado) params.estado = filtros.estado;
  if (filtros.usuario) params.usuario = filtros.usuario;
  if (filtros.desde) params.desde = filtros.desde;
  if (filtros.hasta) params.hasta = filtros.hasta;

  return this.http.get(`${this.baseMovimientos}/search`, { params });
}
  // =========================
  // DETALLES SINGLE (OK)
  // =========================

  createDetalle(movimientoId: number, detalle: any): Observable<any> {
    return this.http.post(
      `${this.baseDetalles}/movimiento/${movimientoId}`,
      detalle
    );
  }

  updateDetalle(id: number, detalle: any): Observable<any> {
    return this.http.put(`${this.baseDetalles}/${id}`, detalle);
  }

  deleteDetalle(id: number): Observable<any> {
    return this.http.delete(`${this.baseDetalles}/${id}`);
  }

  // =========================
  // BATCH (OK)
  // =========================

  createDetallesBatch(movimientoId: number, detalles: any[]): Observable<any> {
    return this.http.post(
      `${this.baseDetalles}/movimiento/${movimientoId}`,
      detalles
    );
  }
}