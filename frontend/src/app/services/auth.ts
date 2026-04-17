import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class Auth {

  private apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(private http: HttpClient, private router: Router) {}

  iniciarSesion(credenciales: any) {
    return this.http.post(this.apiUrl, credenciales);
  }

  // =========================
  // STORAGE
  // =========================

  getToken(): string | null {
    return localStorage.getItem('token_ims');
  }

  getNombre(): string | null {
    return localStorage.getItem('nombre_ims');
  }

  getPermisos(): string[] {
    const raw = localStorage.getItem('permisos_ims');
    return raw ? JSON.parse(raw) : [];
  }

  // =========================
  // AUTH STATE
  // =========================

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // =========================
  // AUTHORIZATION CORE (PERMISSIONS ONLY)
  // =========================

  hasPermission(permission: string): boolean {
    return this.getPermisos().includes(permission);
  }

  hasAnyPermission(perms: string[]): boolean {
    return perms.some(p => this.hasPermission(p));
  }

  hasAllPermissions(perms: string[]): boolean {
    return perms.every(p => this.hasPermission(p));
  }

  // =========================
  // SESSION CONTROL
  // =========================

  logout() {
    localStorage.removeItem('token_ims');
    localStorage.removeItem('rol_ims');
    localStorage.removeItem('nombre_ims');
    localStorage.removeItem('permisos_ims');
    this.router.navigate(['/login']);
  }
}