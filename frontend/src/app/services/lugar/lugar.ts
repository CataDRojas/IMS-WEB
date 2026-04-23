import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MovimientoLugar {
  movimientoLugarId?: number;
  movimientoLugarDescripcion: string;
  movimientoLugarActivo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class LugarService {
  private apiUrl = 'http://localhost:8080/api/movimiento-lugares'; 

  constructor(private http: HttpClient) { }

  private getHeaders() {
    // Rescatamos el usuario real de la sesión
    const currentUser = localStorage.getItem('nombre_ims'); 
    
    // Si por algún motivo no hay usuario en sesión (onda, se venció o borró el caché), 
    // mandamos un string que el backend reconozca como error, o simplemente lo mandamos nulo 
    // para que el backend con justa razón bloquee la operación.
    return new HttpHeaders().set('X-User', currentUser ? currentUser : 'USUARIO_NO_AUTENTICADO');
  }

  getLugares(): Observable<MovimientoLugar[]> {
    return this.http.get<MovimientoLugar[]>(this.apiUrl);
  }

  getLugarById(id: number): Observable<MovimientoLugar> {
    return this.http.get<MovimientoLugar>(`${this.apiUrl}/${id}`);
  }

  guardarLugar(lugar: MovimientoLugar): Observable<MovimientoLugar> {
    return this.http.post<MovimientoLugar>(this.apiUrl, lugar, { headers: this.getHeaders() });
  }

  desactivarLugar(id: number): Observable<MovimientoLugar> {
    return this.http.patch<MovimientoLugar>(`${this.apiUrl}/${id}/soft-delete`, {}, { headers: this.getHeaders() });
  }
  eliminarLugar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}