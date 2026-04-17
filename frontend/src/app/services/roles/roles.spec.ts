import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Permiso {
  permisosId: number;
  permisosNombre: string;
}

export interface Rol {
  rolId?: number;
  rolNombre: string;
  permisos: Permiso[];
}

@Injectable({
  providedIn: 'root'
})
export class RolesService {

  private apiUrl = 'http://localhost:8080/api/roles';

  constructor(private http: HttpClient) {}

  // =========================
  // GET ALL
  // =========================
  getAll(): Observable<Rol[]> {
    return this.http.get<Rol[]>(this.apiUrl);
  }

  // =========================
  // GET BY ID
  // =========================
  getById(id: number): Observable<Rol> {
    return this.http.get<Rol>(`${this.apiUrl}/${id}`);
  }

  // =========================
  // CREATE / UPDATE
  // (same endpoint in backend)
  // =========================
  save(rol: Rol): Observable<Rol> {
    return this.http.post<Rol>(this.apiUrl, rol);
  }

  // =========================
  // DELETE
  // =========================
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}