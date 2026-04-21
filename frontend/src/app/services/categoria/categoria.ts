import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Categoria {
  categoriaId?: number;
  categoriaNombre: string;

  descuento?: any;
}

export interface Descuento {
  descuentoId?: number;
  descuentoNombre: string;
  descuentoPorcentaje?: number;
  descuentoActivo?: boolean;
}

export interface CategoriaUiData {
  categorias: Categoria[];
  descuentos: Descuento[];
}

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {

  private apiUrl = 'http://localhost:8080/api/categorias';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const usuarioActual = localStorage.getItem('usuario_email') || 'admin@ims.cl';
    return new HttpHeaders().set('X-User', usuarioActual);
  }

  obtenerCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl, {
      headers: this.getHeaders()
    });
  }

  obtenerCategoriaById(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  crearCategoria(categoria: Categoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, categoria, {
      headers: this.getHeaders()
    });
  }

  actualizarCategoria(id: number, categoria: Categoria): Observable<Categoria> {
    return this.http.put<Categoria>(
      `${this.apiUrl}/${id}`,
      categoria,
      { headers: this.getHeaders() }
    );
  }

  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  obtenerDescuentos(): Observable<Descuento[]> {
    return this.http.get<Descuento[]>(`${this.apiUrl}/descuentos`, {
      headers: this.getHeaders()
    });
  }

  obtenerDescuentosActivos(): Observable<Descuento[]> {
    return this.http.get<Descuento[]>(`${this.apiUrl}/descuentos/active`, {
      headers: this.getHeaders()
    });
  }

  obtenerDescuentoById(id: number): Observable<Descuento> {
    return this.http.get<Descuento>(`${this.apiUrl}/descuentos/${id}`, {
      headers: this.getHeaders()
    });
  }

  obtenerUiData(): Observable<CategoriaUiData> {
    return this.http.get<CategoriaUiData>(this.apiUrl, {
      headers: this.getHeaders()
    });
  }
}