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
  
  // ==========================================
  // NUEVAS HERRAMIENTAS DE SESIÓN Y ROLES
  // ==========================================

  // Extraer datos del bolsillo (localStorage)
  getToken(): string | null {
    return localStorage.getItem('token_ims');
  }

  getRol(): string | null {
    return localStorage.getItem('rol_ims');
  }

  getNombre(): string | null {
    return localStorage.getItem('nombre_ims');
  }

  // Verificar si hay alguien logueado
  isLoggedIn(): boolean {
    return this.getToken() !== null; // Devuelve true si hay un token
  }

  // Verificar si el usuario tiene un rol específico
  hasRole(roleEsperado: string): boolean {
    const rolActual = this.getRol();
    // Validamos que exista un rol y que coincida exactamente
    return rolActual === roleEsperado; 
  }

  // Botón de escape: Destruye la sesión y vuelve al login
  logout() {
    localStorage.removeItem('token_ims');
    localStorage.removeItem('rol_ims');
    localStorage.removeItem('nombre_ims');
    this.router.navigate(['/login']);
  }
}
