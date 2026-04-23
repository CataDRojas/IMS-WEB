import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Rol {
  rolId: number;
  rolNombre: string;
}

export interface UsuarioRaw {
  usuarioId?: number;
  usuarioEmail: string;
  usuarioNombre: string;
  usuarioRun: string;
  usuarioDV: string;
  usuarioPassword?: string;
  usuarioActivo: boolean;

  rolId?: number;
  rol?: Rol;
}

export interface Usuario {
  usuarioId?: number;
  usuarioEmail: string;
  usuarioNombre: string;
  usuarioRun: string;
  usuarioDV: string;
  usuarioPassword?: string;
  usuarioActivo: boolean;
  rolId: number;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private apiUrl = 'http://localhost:8080/api/usuarios';
  private rolesUrl = 'http://localhost:8080/api/usuarios/roles';

  constructor(private http: HttpClient) {}

  obtenerUsuarios(): Observable<Usuario[]> {
    return this.http.get<UsuarioRaw[]>(this.apiUrl).pipe(
      map(users =>
        users.map(u => ({
          usuarioId: u.usuarioId,
          usuarioEmail: u.usuarioEmail,
          usuarioNombre: u.usuarioNombre,
          usuarioRun: u.usuarioRun,
          usuarioDV: u.usuarioDV,
          usuarioPassword: u.usuarioPassword,
          usuarioActivo: u.usuarioActivo,

          rolId: u.rolId ?? u.rol?.rolId ?? 0
        }))
      )
    );
  }

  crearUsuario(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, usuario);
  }

  actualizarUsuario(id: number, usuario: Usuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, usuario);
  }

  eliminarUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  obtenerRoles(): Observable<Rol[]> {
    return this.http.get<Rol[]>(this.rolesUrl);
  }
}