import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class Auth {

  private apiUrl = 'http://localhost:8080/api/auth/login';
  private meUrl = 'http://localhost:8080/api/auth/me';

  private permisosTsKey = 'permisos_ts';
  private ttlMs = 1 * 30 * 1000; // 2 min freshness window (adjust later if needed)

  constructor(private http: HttpClient, private router: Router) {}

  // =========================
  // AUTH
  // =========================

  iniciarSesion(credenciales: any) {
    return this.http.post(this.apiUrl, credenciales);
  }

  refreshMe() {
    return this.http.get<any>(this.meUrl);
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

  setPermisos(permisos: string[]) {
    localStorage.setItem('permisos_ims', JSON.stringify(permisos));
    localStorage.setItem(this.permisosTsKey, Date.now().toString());
  }

  getPermisosTimestamp(): number {
    return Number(localStorage.getItem(this.permisosTsKey) || 0);
  }

  // =========================
  // AUTH STATE
  // =========================

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // =========================
  // PERMISSION LOGIC
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
  // LAZY REFRESH (KEY PIECE)
  // =========================

  refreshPermisosIfNeeded() {
    const last = this.getPermisosTimestamp();

    const isStale = Date.now() - last > this.ttlMs;

    if (!isStale) {
      return null;
    }

    return this.refreshMe();
  }

  // =========================
  // SESSION CONTROL
  // =========================

  logout() {
    localStorage.removeItem('token_ims');
    localStorage.removeItem('rol_ims');
    localStorage.removeItem('nombre_ims');
    localStorage.removeItem('permisos_ims');
    localStorage.removeItem(this.permisosTsKey);
    this.router.navigate(['/login']);
  }
}