import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private apiMovimientos = 'http://localhost:8080/api/movimientos';
  private apiDetalles = 'http://localhost:8080/api/movimiento-detalles';
  private apiLugares = 'http://localhost:8080/api/movimiento-lugares';

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
      `${this.apiLugares}/active`,
      { headers: this.getHeaders() }
    );
  }

  // Listar borradores pendientes desde BD
  obtenerBorradores(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiMovimientos}/pendientes`,
      { headers: this.getHeaders() }
    );
  }

  // Guardar borrador en BD (PENDIENTE, no suma stock)
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
    { movimiento, detalles },
    { headers: this.getHeaders() }
  );
}

  // Finalizar = crear + confirmar
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

  // Eliminar borrador desde BD
  eliminarMovimiento(movimientoId: number): Observable<any> {
    return this.http.delete<any>(
      `${this.apiMovimientos}/${movimientoId}`,
      { headers: this.getHeaders() }
    );
  }

  // Obtener todos los movimientos de entrada (historial)
  obtenerTodosMovimientosEntrada(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiMovimientos}`,
      { headers: this.getHeaders() }
    ).pipe(
      map((data: any[]) => data.filter(m => m.movimientoTipo === 'ENTRADA'))
    );
  }

// Anular movimiento
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