import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// =========================
// INTERFACE
// =========================
export interface Permisos {
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

  getAll(): Observable<Permisos[]> {
    return this.http.get<Permisos[]>(this.apiUrl);
  }

  getById(id: number): Observable<Permisos> {
    return this.http.get<Permisos>(`${this.apiUrl}/${id}`);
  }
}