import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// =========================
// DOMAIN MODEL (AUTHORITY UNIT)
// =========================
export interface Permiso {
  permisosId: number;
  permisosNombre: string;
}

// =========================
// SERVICE
// =========================
@Injectable({
  providedIn: 'root'
})
export class PermisosService {

  private apiUrl = 'http://localhost:8080/api/permisos';

  constructor(private http: HttpClient) {}

  // GET ALL (used for role assignment UI)
  getAll(): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(this.apiUrl);
  }

  // GET ONE (rarely used, but valid)
  getById(id: number): Observable<Permiso> {
    return this.http.get<Permiso>(`${this.apiUrl}/${id}`);
  }
}