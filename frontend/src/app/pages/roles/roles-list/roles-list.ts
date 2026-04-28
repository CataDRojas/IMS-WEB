import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

//angular materials
import { MatIcon } from '@angular/material/icon';
import { MatAccordion } from '@angular/material/expansion';
import { MatExpansionModule } from '@angular/material/expansion';



import { RolesService, Rol } from '../../../services/roles/roles';
import { PermisosService, Permisos } from '../../../services/permisos/permisos';

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
  INVENTARIO_READ: 'Ver Inventario',
  INVENTARIO_MANAGE: 'Gestionar Inventario',
  VENTA_READ: 'Ver Venta',
  VENTA_MANAGE: 'Gestionar Venta'
};

export const ALL_PERMISSIONS: string[] = Object.keys(PERMISSION_LABELS);

@Component({
  selector: 'app-roles-list',
  standalone: true,
  imports: [CommonModule, MatIcon, MatAccordion, MatExpansionModule, FormsModule],
  templateUrl: './roles-list.html',
  styleUrl: './roles-list.css'
})
// ... imports necesarios ...

export class RolesList implements OnInit {
  roles: Rol[] = [];
  permisosDisponibles: Permisos[] = [];
  
  // Estado del Modal
  mostrarFormulario = false;
  rolActual: Rol = { rolNombre: '', permisos: [] };
  
  permissionLabels = PERMISSION_LABELS;

  constructor(
    private rolesService: RolesService,
    private permisosService: PermisosService
  ) {}

  ngOnInit(): void {
    this.cargarRoles();
    this.cargarPermisos();
  }

  cargarRoles() {
    this.rolesService.getAll().subscribe(data => this.roles = data);
  }

  cargarPermisos() {
    this.permisosService.getAll().subscribe(data => this.permisosDisponibles = data);
  }

  abrirNuevo() {
    this.rolActual = { rolNombre: '', permisos: [] };
    this.mostrarFormulario = true;
  }

  editar(rol: Rol) {
    this.rolActual = JSON.parse(JSON.stringify(rol)); // Clonamos para no editar en vivo la tabla
    this.mostrarFormulario = true;
  }

  cancelar() {
    this.mostrarFormulario = false;
  }

  togglePermiso(permiso: Permisos) {
    const index = this.rolActual.permisos.findIndex(p => p.permisosId === permiso.permisosId);
    if (index > -1) {
      this.rolActual.permisos.splice(index, 1);
    } else {
      this.rolActual.permisos.push(permiso);
    }
  }

  tienePermiso(permiso: Permisos): boolean {
    return this.rolActual.permisos.some(p => p.permisosId === permiso.permisosId);
  }

  guardar() {
    this.rolesService.save(this.rolActual).subscribe(() => {
      this.cargarRoles();
      this.mostrarFormulario = false;
    });
  }

  eliminar(id: number) {
    if (confirm('¿Seguro que deseas eliminar este rol?')) {
      this.rolesService.delete(id).subscribe(() => this.cargarRoles());
    }
  }
}