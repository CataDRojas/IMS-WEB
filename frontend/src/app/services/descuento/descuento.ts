import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Descuento {
  descuentoId?: number;
  descuentoNombre: string;
  descuentoTipo: string;
  descuentoValor: number;
}

@Injectable({
  providedIn: 'root'
})
export class DescuentoService {
  private apiUrl = 'http://localhost:8080/api/descuentos';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const usuarioActual = localStorage.getItem('usuario_email') || 'admin@ims.cl';
    return new HttpHeaders().set('X-User', usuarioActual);
  }

  // Solo traemos los activos, para que no le asignen descuentos viejos a las categorías
  obtenerDescuentosActivos(): Observable<Descuento[]> {
    return this.http.get<Descuento[]>(`${this.apiUrl}/active`, { headers: this.getHeaders() });
  }
}