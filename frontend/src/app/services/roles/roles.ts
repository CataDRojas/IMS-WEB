import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// =========================
// DOMAIN MODELS
// =========================

// Authority unit (backend truth mirror)
export interface Permiso {
  permisosId: number;
  permisosNombre: string;
}

// Role is a container of permissions (NOT authority)
export interface Rol {
  rolId?: number;
  rolNombre: string;
  permisos: Permiso[];
}

// =========================
// SERVICE
// =========================
@Injectable({
  providedIn: 'root'
})
export class RolesService {

  private apiUrl = 'http://localhost:8080/api/roles';

  constructor(private http: HttpClient) {}

  // READ ALL ROLES (permission groups)
  getAll(): Observable<Rol[]> {
    return this.http.get<Rol[]>(this.apiUrl);
  }

  // READ ONE ROLE
  getById(id: number): Observable<Rol> {
    return this.http.get<Rol>(`${this.apiUrl}/${id}`);
  }

  // CREATE / UPDATE ROLE (permission grouping config)
  save(rol: Rol): Observable<Rol> {
    return this.http.post<Rol>(this.apiUrl, rol);
  }

  // DELETE ROLE
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}