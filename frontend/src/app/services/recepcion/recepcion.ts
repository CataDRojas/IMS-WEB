import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RecepcionService {
  private apiMovimientos = 'http://localhost:8080/api/movimientos';
  private apiDetalles = 'http://localhost:8080/api/movimiento-detalles';
  private apiLugares = 'http://localhost:8080/api/movimiento-detalles';

  constructor(private http: HttpClient) { }

  private getHeaders() {
    const currentUser = localStorage.getItem('nombre_ims');
    return new HttpHeaders().set('X-User', currentUser ? currentUser : 'USUARIO_NO_AUTENTICADO');
  }

  buscarProductoPorCodigo(codigo: string): Observable<any> {
    return this.http.get<any>(
      `${this.apiDetalles}/productos/codigo/${codigo}`,
      { headers: this.getHeaders() }
    );
  }

  obtenerLugaresActivos(): Observable<any[]> {
  return this.http.get<any[]>(
    `${this.apiDetalles}/movimiento-lugares/active`,
    { headers: this.getHeaders() }
  );
}

  obtenerBorradores(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiMovimientos}/pendientes`,
      { headers: this.getHeaders() }
    ).pipe(
      map((data: any[]) => data.filter(m => m.movimientoTipo === 'ENTRADA'))
    );
  }

  guardarBorrador(nombre: string, items: any[]): Observable<any> {
    const movimiento = {
      movimientoTipo: 'ENTRADA',
      movimientoEstado: 'PENDIENTE',
      movimientoMetodoPago: null,
      movimientoDescripcion: `Recepción: ${nombre}`
    };

    const detalles = items.map((item: any) => {
      const precioReal = Number(item.productoPrecio || 0);
      const cajas = Number(item.cajasAgregadas || 1);
      const lote = Number(item.productoCantidadLote || 1);
      const unidades = cajas * lote;
      return {
        productoId: Number(item.productoId),
        movimientoLugarId: Number(item.lugarId),
        movimientoDetalleCantidad: cajas,
        movimientoDetalleUnidadesPorPaquete: lote,
        movimientoDetallePrecioBase: precioReal,
        movimientoDetallePrecioUnitario: precioReal,
        movimientoDetallePrecioTotal: precioReal * unidades,
        movimientoDetalleDescuentoAplicado: 0,
        movimientoDetalleDescripcion: `Recepción: ${nombre}`
      };
    });

    return this.http.post<any>(
      `${this.apiMovimientos}/borrador`,
      { movimiento, detalles },
      { headers: this.getHeaders() }
    );
  }

  finalizarRecepcion(nombre: string, items: any[]): Observable<any> {
    const movimiento = {
      movimientoTipo: 'ENTRADA',
      movimientoEstado: 'PENDIENTE',
      movimientoMetodoPago: null,
      movimientoDescripcion: `Recepción: ${nombre}`
    };

    const detalles = items.map((item: any) => {
      const precioReal = Number(item.productoPrecio || 0);
      const cajas = Number(item.cajasAgregadas || 1);
      const lote = Number(item.productoCantidadLote || 1);
      const unidades = cajas * lote;
      return {
        productoId: Number(item.productoId),
        movimientoLugarId: Number(item.lugarId),
        movimientoDetalleCantidad: cajas,
        movimientoDetalleUnidadesPorPaquete: lote,
        movimientoDetallePrecioBase: precioReal,
        movimientoDetallePrecioUnitario: precioReal,
        movimientoDetallePrecioTotal: precioReal * unidades,
        movimientoDetalleDescuentoAplicado: 0,
        movimientoDetalleDescripcion: `Recepción: ${nombre}`
      };
    });

    return this.http.post<any>(
      this.apiMovimientos,
      { movimiento, detalles },
      { headers: this.getHeaders() }
    );
  }

  confirmarMovimiento(movimientoId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiMovimientos}/${movimientoId}/confirmar`,
      {},
      { headers: this.getHeaders() }
    );
  }

  eliminarMovimiento(movimientoId: number): Observable<any> {
    return this.http.delete<any>(
      `${this.apiMovimientos}/${movimientoId}`,
      { headers: this.getHeaders() }
    );
  }

  obtenerTodosMovimientosEntrada(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiMovimientos}`,
      { headers: this.getHeaders() }
    ).pipe(
      map((data: any[]) => data.filter(m => m.movimientoTipo === 'ENTRADA'))
    );
  }

  anularMovimiento(movimientoId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiMovimientos}/${movimientoId}/anular`,
      {},
      { headers: this.getHeaders() }
    );
  }

  reactivarMovimiento(movimientoId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiMovimientos}/${movimientoId}/reactivar`,
      {},
      { headers: this.getHeaders() }
    );
  }
}