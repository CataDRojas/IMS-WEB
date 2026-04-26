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
  // CONFIGURACION (NOW FROM DETALLES)
  // =========================

  getConfiguracion(): Observable<any> {
    return this.http.get(`${this.baseDetalles}/configuracion`);
  }

  // =========================
  // PRODUCTOS (NOW FROM DETALLES)
  // =========================

  getProductoByCodigo(codigo: string): Observable<any> {
    return this.http.get(`${this.baseDetalles}/productos/codigo/${codigo}`);
  }

  getProductoById(id: number): Observable<any> {
    return this.http.get(`${this.baseDetalles}/productos/${id}`);
  }

  getAllProductos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseDetalles}/productos`);
  }

  // =========================
  // MOVIMIENTO (HEADER - STILL OWNED BY MOVIMIENTOS)
  // =========================

  createMovimiento(payload: {
    movimientoTipo: string;
    movimientoEstado: string;
    movimientoMetodoPago?: string;
    movimientoDescripcion?: string;
    movimientoDescuento?: number;
  }): Observable<any> {

    const user = localStorage.getItem('user') || 'system';

    return this.http.post(`${this.baseMovimientos}`, payload, {
      headers: { 'X-User': user }
    });
  }

  getMovimientoById(id: number): Observable<any> {
    return this.http.get(`${this.baseMovimientos}/${id}`);
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
  // DETALLES (SINGLE)
  // =========================

  createDetalle(
    movimientoId: number,
    detalle: {
      productoId: number;
      movimientoDetalleCantidad: number;
      movimientoDetalleUnidadesPorPaquete?: number;
      movimientoDetalleDescripcion?: string;

      movimientoDetallePrecioBase: number;
      movimientoDetallePrecioUnitario: number;
      movimientoDetallePrecioTotal: number;

      movimientoDetalleDescuentoAplicado?: number;
    }
  ): Observable<any> {

    return this.http.post(
      `${this.baseDetalles}/movimiento/${movimientoId}`,
      detalle
    );
  }

  // =========================
  // DETALLES (BATCH)
  // =========================

  createDetallesBatch(
    movimientoId: number,
    detalles: Array<{
      productoId: number;
      movimientoDetalleCantidad: number;
      movimientoDetalleUnidadesPorPaquete?: number;
      movimientoDetalleDescripcion?: string;

      movimientoDetallePrecioBase: number;
      movimientoDetallePrecioUnitario: number;
      movimientoDetallePrecioTotal: number;

      movimientoDetalleDescuentoAplicado?: number;
    }>
  ): Observable<any> {

    return this.http.post(
      `${this.baseDetalles}/movimiento/${movimientoId}/batch`,
      detalles
    );
  }

  // =========================
  // UPDATE DETALLE
  // =========================

  updateDetalle(
    id: number,
    detalle: {
      movimientoDetalleCantidad: number;
      movimientoDetalleUnidadesPorPaquete?: number;
      movimientoDetalleDescripcion?: string;
    }
  ): Observable<any> {

    return this.http.put(
      `${this.baseDetalles}/${id}`,
      detalle
    );
  }

  // =========================
  // DELETE DETALLE
  // =========================

  deleteDetalle(id: number): Observable<any> {
    return this.http.delete(`${this.baseDetalles}/${id}`);
  }

  // =========================
  // LUGARES
  // =========================

  getLugaresActivos(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseDetalles}/lugares/active`
    );
  }
}