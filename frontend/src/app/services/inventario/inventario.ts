import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private apiMovimientos = 'http://localhost:8080/api/movimientos';
  private apiDetalles = 'http://localhost:8080/api/movimiento-detalles';
  private apiLugares = 'http://localhost:8080/api/movimiento-detalles';

  constructor(private http: HttpClient) { }

  buscarProductoPorCodigo(codigo: string): Observable<any> {
    return this.http.get<any>(
      `${this.apiDetalles}/productos/codigo/${codigo}`
    );
  }

  obtenerLugaresActivos(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiLugares}/movimiento-lugares/active`
    );
  }

  obtenerBorradores(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiMovimientos}/pendientes`
    );
  }

  guardarBorrador(nombre: string, items: any[], tipo: string = 'ENTRADA'): Observable<any> {
    const movimiento = {
      movimientoTipo: tipo,
      movimientoEstado: 'PENDIENTE',
      movimientoMetodoPago: null,
      movimientoDescripcion: `Inventario: ${nombre}`
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
        movimientoDetalleDescripcion: `Borrador: ${nombre}`
      };
    });

    return this.http.post<any>(
      `${this.apiMovimientos}/borrador`,
      { movimiento, detalles }
    );
  }

  finalizarInventario(nombre: string, items: any[], tipo: string = 'ENTRADA'): Observable<any> {
    const movimiento = {
      movimientoTipo: tipo,
      movimientoEstado: 'PENDIENTE',
      movimientoMetodoPago: null,
      movimientoDescripcion: `Inventario: ${nombre}`
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
        movimientoDetalleDescripcion: `Carga stock: ${nombre}`
      };
    });

    return this.http.post<any>(
      this.apiMovimientos,
      { movimiento, detalles }
    );
  }

  confirmarMovimiento(movimientoId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiMovimientos}/${movimientoId}/confirmar`,
      {}
    );
  }

  eliminarMovimiento(movimientoId: number): Observable<any> {
    return this.http.delete<any>(
      `${this.apiMovimientos}/${movimientoId}`
    );
  }

  obtenerTodosMovimientosEntrada(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiMovimientos}`
    ).pipe(
      map((data: any[]) => data.filter(m => m.movimientoTipo === 'ENTRADA'))
    );
  }

  anularMovimiento(movimientoId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiMovimientos}/${movimientoId}/anular`,
      {}
    );
  }

  reactivarMovimiento(movimientoId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiMovimientos}/${movimientoId}/reactivar`,
      {}
    );
  }
}