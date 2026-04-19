import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Descuento } from '../descuento/descuento';

// La interfaz para los datos
export interface Categoria {
  categoriaId?: number;
  categoriaNombre: string;
  descuento?: Descuento | null;
}

@Injectable({
  providedIn: 'root'
})
export class CategoriaService { // <--- ASEGÚRATE QUE DIGA CategoriaService

  private apiUrl = 'http://localhost:8080/api/categorias';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const usuarioActual = localStorage.getItem('usuario_email') || 'admin@ims.cl';
    return new HttpHeaders().set('X-User', usuarioActual);
  }

  obtenerCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl);
  }

  crearCategoria(categoria: Categoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, categoria, { headers: this.getHeaders() });
  }

  actualizarCategoria(id: number, categoria: Categoria): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/${id}`, categoria, { headers: this.getHeaders() });
  }

  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}