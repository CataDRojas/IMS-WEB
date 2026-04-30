import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfiguracionService {

  private apiUrl = 'http://localhost:8080/api/configuracion';

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const currentUser = localStorage.getItem('nombre_ims');

    return new HttpHeaders().set(
      'X-User',
      currentUser ? currentUser : 'USUARIO_NO_AUTENTICADO'
    );
  }

  // GET CONFIG
  getConfiguracion(): Observable<any> {
    return this.http.get<any>(this.apiUrl, {
      headers: this.getHeaders()
    });
  }

  // SAVE CONFIG
  saveConfiguracion(config: any): Observable<any> {
    console.log('🌐 HTTP CALL OUTGOING', config);

    return this.http.post<any>(this.apiUrl, config, {
      headers: this.getHeaders()
    });
  }
}