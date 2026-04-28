import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { Home } from './pages/home/home';
import { UsuariosComponent } from './pages/usuarios/usuarios';

import { RolesList } from './pages/roles/roles-list/roles-list';

import { authGuard } from './guards/auth-guard';
import { permisosGuard } from './guards/permisos-guard';

import { VentasHistorial } from './pages/ventas/ventas-historial/ventas-historial';
import { VentasForm } from './pages/ventas/ventas-form/ventas-form';
import { NOT_FOUND } from '@angular/core/primitives/di';

import { NotFound } from './pages/not-found/not-found';
import { AccessDenied } from './pages/access-denied/access-denied';

import { CategoriasComponent } from './pages/categorias/categorias';
import { ProductosComponent } from './pages/productos/productos';

import { DescuentosListComponent } from './pages/descuentos/descuentos-list/descuentos-list';

// IMPORTS DEL INVENTARIO
import { InventarioForm } from './pages/inventario/inventario-form/inventario-form';
import { InventarioHistorial } from './pages/inventario/inventario-historial/inventario-historial';

// IMPORTS DE LUGARES 
import { LugaresComponent } from './pages/lugares/lugares-list/lugares-list';

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

// =========================
// VENTAS / MOVIMIENTOS
// =========================
{
  path: 'ventas',
  canActivate: [authGuard, permisosGuard],
  data: { requiredPermisos: ['VENTA_READ', 'VENTA_MANAGE'] },
  children: [
    {
      path: '',
      component: VentasHistorial // 🔥 hub is now default
    },
    {
      path: 'form',
      component: VentasForm
    },
    {
      path: 'historial',
      redirectTo: '',
      pathMatch: 'full'
    }
  ]
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

  // =========================
  // INVENTARIO
  // =========================
  { 
    path: 'inventario', 
    component: InventarioForm, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['INVENTARIO_MANAGE', 'INVENTARIO_READ'] }
  },
  { 
    path: 'inventario/nuevo', 
    component: InventarioForm, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['INVENTARIO_MANAGE', 'INVENTARIO_READ'] }
  },
  { 
    path: 'inventario/historial', 
    component: InventarioHistorial, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['INVENTARIO_MANAGE', 'INVENTARIO_READ'] }
  },

  // =========================
  // LUGARES
  // =========================
  {
    path: 'lugares',
    component: LugaresComponent, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['MOVIMIENTO_LUGAR_MANAGE'] }
  },

  // =========================
  // SYSTEM
  // =========================
  { path: 'access-denied', component: AccessDenied },
  { path: '**', component: NotFound }
];