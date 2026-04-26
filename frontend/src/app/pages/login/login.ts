import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

//Angular material
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule, 
            MatCardModule, 
            MatFormFieldModule, 
            MatInputModule, 
            MatButtonModule,
            MatIconModule],
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
    private auth: Auth
  ) {}

  iniciarSesion() {
    console.log('Intentando iniciar sesión con:', this.credenciales);

    this.auth.iniciarSesion(this.credenciales).subscribe({
      next: (respuesta: any) => {
        console.log('¡Éxito! El backend respondió esto:', respuesta);

        if (respuesta.token) {

          // AUTH
          localStorage.setItem('token_ims', respuesta.token);
          localStorage.setItem('nombre_ims', respuesta.nombre);

          // ROL
          localStorage.setItem('rol_ims', respuesta.rol);
          localStorage.setItem('rol_id_ims', String(respuesta.rolId));

          // PERMISOS
          localStorage.setItem(
            'permisos_ims',
            JSON.stringify(respuesta.permisos ?? [])
          );

          this.router.navigate(['/home']);
        }
      },
      error: (error) => {
        console.error('Login error:', error);
        alert('Credenciales incorrectas o error de conexión.');
      }
    });
  }
}