import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Descuento {
  descuentoId?: number;
  descuentoNombre: string;
  descuentoTipo: 'FLAT' | 'PORCENTAJE' | 'MULTIPLICATIVO';
  descuentoValor: number;
  descuentoActivo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class DescuentosService {

  private readonly baseUrl = 'http://localhost:8080/api/descuentos';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const usuarioActual = localStorage.getItem('usuario_email') || 'admin@ims.cl';
    return new HttpHeaders().set('X-User', usuarioActual);
  }

  getAll(): Observable<Descuento[]> {
    return this.http.get<Descuento[]>(this.baseUrl, {
      headers: this.getHeaders()
    });
  }

  getActive(): Observable<Descuento[]> {
    return this.http.get<Descuento[]>(`${this.baseUrl}/active`, {
      headers: this.getHeaders()
    });
  }

  getById(id: number): Observable<Descuento> {
    return this.http.get<Descuento>(`${this.baseUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  create(dto: Descuento): Observable<Descuento> {
    return this.http.post<Descuento>(this.baseUrl, dto, {
      headers: this.getHeaders()
    });
  }

  update(id: number, dto: Descuento): Observable<Descuento> {
    return this.http.put<Descuento>(`${this.baseUrl}/${id}`, dto, {
      headers: this.getHeaders()
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }
}