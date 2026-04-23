import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { RolesService, Rol } from '../../../services/roles/roles';
import { PermisosService, Permisos } from '../../../services/permisos/permisos';

import {
  PERMISSION_LABELS,
  ALL_PERMISSIONS
} from '../roles-list/roles-list';

@Component({
  selector: 'app-roles-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles-form.html',
  styleUrl: './roles-form.css'
})
export class RolesForm implements OnInit {

  rol: Rol = {
    rolNombre: '',
    permisos: []
  };

  permisosDisponibles: Permisos[] = [];

  rolId?: number;

  permissionLabels = PERMISSION_LABELS;
  allPermissions = ALL_PERMISSIONS;

  constructor(
    private rolesService: RolesService,
    private permisosService: PermisosService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarPermisos();

    const paramId = this.route.snapshot.paramMap.get('id');
    this.rolId = paramId ? Number(paramId) : undefined;

    if (this.rolId) {
      this.cargarRol(this.rolId);
    }
  }

  cargarPermisos(): void {
    this.permisosService.getAll().subscribe({
      next: (data: Permisos[]) => {
        this.permisosDisponibles = data;
      },
      error: (err: any) => {
        console.error('Error cargando permisos:', err);
      }
    });
  }

  cargarRol(id: number): void {
    this.rolesService.getById(id).subscribe({
      next: (data: Rol) => {
        this.rol = data;
      },
      error: (err: any) => {
        console.error('Error cargando rol:', err);
      }
    });
  }

  togglePermiso(permiso: Permisos): void {
    const exists = this.rol.permisos.some(
      (p: Permisos) => p.permisosId === permiso.permisosId
    );

    if (exists) {
      this.rol.permisos = this.rol.permisos.filter(
        (p: Permisos) => p.permisosId !== permiso.permisosId
      );
    } else {
      this.rol.permisos = [...this.rol.permisos, permiso];
    }
  }

  tienePermiso(permiso: Permisos): boolean {
    return this.rol.permisos.some(
      (p: Permisos) => p.permisosId === permiso.permisosId
    );
  }

  guardar(): void {
    this.rolesService.save(this.rol).subscribe({
      next: () => {

        const storedRolIdRaw = localStorage.getItem('rol_id_ims');
        const storedRolId = storedRolIdRaw !== null ? Number(storedRolIdRaw) : null;

        const currentRolId = this.rolId !== undefined ? Number(this.rolId) : null;

        if (storedRolId !== null && currentRolId !== null && currentRolId === storedRolId) {

          const permisosList: string[] = this.rol.permisos
            .map((p: Permisos) => p.permisosNombre)
            .filter((name): name is string => !!name);

          localStorage.setItem(
            'permisos_ims',
            JSON.stringify(permisosList)
          );
        }

        this.router.navigate(['/roles']);
      },
      error: (err: any) => {
        console.error('Error guardando rol:', err);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/roles']);
  }

  getPermisoLabel(nombre?: string): string {
    if (!nombre) return '';
    return nombre
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, l => l.toUpperCase());
  }
}