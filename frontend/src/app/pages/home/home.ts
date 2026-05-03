import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';


//Angular material
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatDivider } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';


@Component({
  selector: 'app-home',
  imports: [NgIf, RouterLink, CommonModule,CommonModule, 
    RouterModule, 
    MatIconModule, 
    MatMenuModule, 
    MatButtonModule,
    MatDivider],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  nombreUsuario = localStorage.getItem('nombre_ims') ?? '';
  rolUsuario = localStorage.getItem('rol_ims') ?? 'Invitado';
  permisosUsuario: string[] = [];

  constructor(private auth: Auth, private dialog: MatDialog) {
    const stored = localStorage.getItem('permisos_ims');

    try {
      this.permisosUsuario = stored ? JSON.parse(stored) : [];
    } catch {
      this.permisosUsuario = [];
    }
  }

  tienePermiso(permiso: string): boolean {
    return this.permisosUsuario.includes(permiso);
  }

  logout() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Cerrar Sesión',
        mensaje: '¿Estás seguro que deseas cerrar sesión y salir del sistema?',
        textoConfirmar: 'Sí, cerrar sesión',
        colorBoton: '#ef4444', // Rojo 
        icono: 'logout',
        colorIcono: '#ef4444'
      }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.auth.logout();
      }
    });
  }
}