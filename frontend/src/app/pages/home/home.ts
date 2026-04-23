import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [NgIf, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  nombreUsuario = localStorage.getItem('nombre_ims') ?? '';

  permisosUsuario: string[] = [];

  constructor() {
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
}