import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { Home } from './pages/home/home';
import { UsuariosComponent } from './pages/usuarios/usuarios';

import { RolesList } from './pages/roles/roles-list/roles-list';
import { RolesForm } from './pages/roles/roles-form/roles-form';

import { authGuard } from './guards/auth-guard';
import { permisosGuard } from './guards/permisos-guard';

import { Ventas } from './pages/ventas/ventas';
import { NOT_FOUND } from '@angular/core/primitives/di';

import { NotFound } from './pages/not-found/not-found';
import { AccessDenied } from './pages/access-denied/access-denied';

import { CategoriasComponent } from './pages/categorias/categorias';
import { ProductosComponent } from './pages/productos/productos';

import { DescuentosListComponent } from './pages/descuentos/descuentos-list/descuentos-list';
import { DescuentosFormComponent } from './pages/descuentos/descuentos-form/descuentos-form';

// IMPORTS DEL INVENTARIO (TOBAL)
import { InventarioHomeComponent } from './pages/inventario/inventario-home/inventario-home';
import { InventarioForm } from './pages/inventario/inventario-form/inventario-form';
import { InventarioHistorial } from './pages/inventario/inventario-historial/inventario-historial';

// IMPORTS DE LUGARES (TOBAL)
import { LugaresList } from './pages/lugares/lugares-list/lugares-list';
import { LugaresForm } from './pages/lugares/lugares-form/lugares-form';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { 
    path: 'home', 
    component: Home, 
    canActivate: [authGuard] 
  },

  // =========================
  // USERS
  // =========================
  {
    path: 'usuarios',
    component: UsuariosComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['USUARIOS_MANAGE'] }
  },

  // =========================
  // ROLES
  // =========================
  {
    path: 'roles',
    component: RolesList,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['ROLES_MANAGE'] }
  },
  {
    path: 'roles/new',
    component: RolesForm,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['ROLES_MANAGE'] }
  },
  {
    path: 'roles/edit/:id',
    component: RolesForm,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['ROLES_MANAGE'] }
  },

  // =========================
  // VENTAS / MOVIMIENTOS
  // =========================
  {
    path: 'ventas',
    component: Ventas,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['MOVIMIENTO_READ', 'MOVIMIENTO_MANAGE'] }
  },

  // =========================
  // PRODUCTOS
  // =========================
  {
    path: 'productos',
    component: ProductosComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['PRODUCTO_MANAGE', 'PRODUCTO_READ'] }
  },

  // =========================
  // CATEGORIAS
  // =========================
  {
    path: 'categorias',
    component: CategoriasComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['CATEGORIA_MANAGE', 'CATEGORIA_READ'] }
  },

  // =========================
  // DESCUENTOS
  // =========================
  {
    path: 'descuentos',
    component: DescuentosListComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['DESCUENTO_READ'] }
  },
  {
    path: 'descuentos/new',
    component: DescuentosFormComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['DESCUENTO_MANAGE'] }
  },
  {
    path: 'descuentos/edit/:id',
    component: DescuentosFormComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['DESCUENTO_MANAGE'] }
  },

  // =========================
  // INVENTARIO (TOBAL)
  // =========================
  { 
    path: 'inventario', 
    component: InventarioHomeComponent, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['MOVIMIENTO_LUGAR_MANAGE', 'INVENTARIO_READ'] }
  },
  { 
    path: 'inventario/nuevo', 
    component: InventarioForm, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['MOVIMIENTO_LUGAR_MANAGE', 'INVENTARIO_MANAGE'] }
  },
  { 
    path: 'inventario/historial', 
    component: InventarioHistorial, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['MOVIMIENTO_LUGAR_MANAGE', 'INVENTARIO_READ'] }
  },

  // =========================
  // LUGARES (TOBAL)
  // =========================
  {
    path: 'lugares',
    component: LugaresList, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['MOVIMIENTO_LUGAR_MANAGE'] }
  },
  {
    path: 'lugares/nuevo', 
    component: LugaresForm, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['MOVIMIENTO_LUGAR_MANAGE'] }
  },
  {
    path: 'lugares/edit/:id', 
    component: LugaresForm, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['MOVIMIENTO_LUGAR_MANAGE'] }
  },

  // =========================
  // SYSTEM
  // =========================
  { path: 'access-denied', component: AccessDenied },
  { path: '**', component: NotFound }
];