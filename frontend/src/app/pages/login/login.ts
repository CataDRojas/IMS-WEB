import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common'; // 👈 REQUIRED


//Angular material
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Auth } from '../../services/auth';
import { ApiError } from '../../core/errors/api-error';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {

  credenciales = {
    email: '',
    password: ''
  };

  errorMessage: string | null = null;

  constructor(
    private router: Router,
    private auth: Auth
  ) {}

  iniciarSesion() {

    this.errorMessage = null;

    this.auth.iniciarSesion(this.credenciales).subscribe({

      next: (respuesta: any) => {

        if (respuesta.token) {

          localStorage.setItem('token_ims', respuesta.token);
          localStorage.setItem('nombre_ims', respuesta.nombre);

          localStorage.setItem('rol_ims', respuesta.rol);
          localStorage.setItem('rol_id_ims', String(respuesta.rolId));

          localStorage.setItem(
            'permisos_ims',
            JSON.stringify(respuesta.permisos ?? [])
          );

          this.router.navigate(['/home']);
        }
      },

      error: (err: ApiError) => {

        console.error('Login error:', err);
        if (err.status === 401) {
          this.errorMessage = 'Credenciales incorrectas';
          return;
        }

        if (err.status === 403) {
          this.errorMessage = 'Acceso denegado';
          return;
        }

        if (err.message) {
          this.errorMessage = err.message;
          return;
        }

        this.errorMessage = 'Error de conexión o servidor';
      }
    });
  }
}