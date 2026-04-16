import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  
  credenciales = {
    email: '',
    password: ''
  };

  constructor(
    private router: Router,
    private auth: Auth) {}

  iniciarSesion() {
    console.log('Intentando iniciar sesión con:', this.credenciales);

    this.auth.iniciarSesion(this.credenciales).subscribe({
      next: (respuesta: any) => {
        console.log('¡Éxito! El backend respondió esto:', respuesta);
        
        if (respuesta.token) {
          // 1. SE GUARDA EL TOKEN EN EL BOLSILLITO DEL NAVEGADOR
          localStorage.setItem('token_ims', respuesta.token);
          localStorage.setItem('rol_ims', respuesta.rol);
          localStorage.setItem('nombre_ims', respuesta.nombre);
          
          // 2. Salta al Dashboard
          this.router.navigate(['/home']); 
        }
      },
      error: (error) => {
        console.error('¡Ups! Conexión rechazada. Detalles del error:', error);
        alert('Credenciales incorrectas o error de conexión.');
      }
    });
  }

}
