import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private apiMovimientos = 'http://localhost:8080/api/movimientos';
  private apiDetalles = 'http://localhost:8080/api/movimiento-detalles';

  constructor(private http: HttpClient) { }

  private getHeaders() {
    // Rescatamos tu nombre de administrador desde el LocalStorage
    const currentUser = localStorage.getItem('nombre_ims'); 
    return new HttpHeaders().set('X-User', currentUser ? currentUser : 'USUARIO_NO_AUTENTICADO');
  }

  // 1. Buscar producto real por código
  buscarProductoPorCodigo(codigo: string): Observable<any> {
    return this.http.get<any>(`${this.apiDetalles}/productos/codigo/${codigo}`, { headers: this.getHeaders() });
  }

  // 2. Crear la cabecera del Movimiento
  crearMovimientoCabecera(movimiento: any): Observable<any> {
    return this.http.post<any>(this.apiMovimientos, movimiento, { headers: this.getHeaders() });
  }

  // 3. Crear el detalle (asociado a la cabecera)
  crearDetalle(movimientoId: number, detalle: any): Observable<any> {
    return this.http.post<any>(`${this.apiDetalles}/movimiento/${movimientoId}`, detalle, { headers: this.getHeaders() });
  }
}