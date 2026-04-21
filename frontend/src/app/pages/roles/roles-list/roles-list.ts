import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { RolesService, Rol } from '../../../services/roles/roles';

export const PERMISSION_LABELS: Record<string, string> = {
  CONFIGURACION_MANAGE: 'Gestionar configuración',
  CATEGORIA_READ: 'Ver categorías',
  CATEGORIA_MANAGE: 'Gestionar categorías',
  DESCUENTO_READ: 'Ver descuentos',
  DESCUENTO_MANAGE: 'Gestionar descuentos',
  ROLES_MANAGE: 'Gestionar roles',
  MOVIMIENTO_LUGAR_MANAGE: 'Gestionar ubicaciones de movimiento',
  USUARIOS_MANAGE: 'Gestionar usuarios',
  PRODUCTO_READ: 'Ver productos',
  PRODUCTO_MANAGE: 'Gestionar productos',
  MOVIMIENTO_READ: 'Ver movimientos',
  MOVIMIENTO_MANAGE: 'Gestionar movimientos'
};

export const ALL_PERMISSIONS: string[] = Object.keys(PERMISSION_LABELS);

@Component({
  selector: 'app-roles-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './roles-list.html',
  styleUrl: './roles-list.css'
})
export class RolesList implements OnInit {

  roles: Rol[] = [];
  expandedRoleId: number | null = null;

  constructor(
    private rolesService: RolesService,
    private router: Router
  ) {}

  permissionLabels = PERMISSION_LABELS;
  allPermissions = ALL_PERMISSIONS;

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

  toggleExpand(id: number): void {
    this.expandedRoleId = this.expandedRoleId === id ? null : id;
  }

  hasPermission(rol: Rol, perm: string): boolean {
    return rol.permisos?.some(p => p.permisosNombre === perm);
  }

  goHome() {
    this.router.navigate(['/home']);
  }
}