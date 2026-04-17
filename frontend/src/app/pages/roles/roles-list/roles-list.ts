import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { RolesService, Rol } from '../../../services/roles/roles';

@Component({
  selector: 'app-roles-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './roles-list.html',
  styleUrl: './roles-list.css'
})
export class RolesList implements OnInit {

  roles: Rol[] = [];

  constructor(
    private rolesService: RolesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarRoles();
  }

  cargarRoles(): void {
    this.rolesService.getAll().subscribe({
      next: (data: Rol[]) => {
        this.roles = data;
      },
      error: (err: any) => {
        console.error('Error cargando roles:', err);
      }
    });
  }

  crear(): void {
    this.router.navigate(['/roles/new']);
  }

  editar(id: number): void {
    this.router.navigate(['/roles/edit', id]);
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este rol?')) return;

    this.rolesService.delete(id).subscribe({
      next: () => this.cargarRoles(),
      error: (err: any) => {
        console.error('Error eliminando rol:', err);
      }
    });
  }
}