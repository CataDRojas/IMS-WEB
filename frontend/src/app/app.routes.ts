import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { Home } from './pages/home/home';
import { UsuariosComponent } from './pages/usuarios/usuarios';
import { RolesList } from './pages/roles/roles-list/roles-list';
import { RolesForm } from './pages/roles/roles-form/roles-form';
import { InventarioHomeComponent } from './pages/inventario/inventario-home/inventario-home';
import { InventarioForm } from './pages/inventario/inventario-form/inventario-form';
import { InventarioHistorial } from './pages/inventario/inventario-historial/inventario-historial';

import { authGuard } from './guards/auth-guard';
import { permisosGuard } from './guards/permisos-guard';

import { Ventas } from './pages/ventas/ventas';
import { InicioInventario } from './pages/inicio-inventario/inicio-inventario';
import { BusquedaInventario } from './pages/busqueda-inventario/busqueda-inventario';
import { NotFound } from './pages/not-found/not-found';
import { AccessDenied } from './pages/access-denied/access-denied';
import { CategoriasComponent } from './pages/categorias/categorias';
import { ProductosComponent } from './pages/productos/productos';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'home', component: Home, canActivate: [authGuard] },
  
  {
    path: 'usuarios',
    component: UsuariosComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['USUARIOS_MANAGE'] }
  },
  {
    path: 'ventas',
    component: Ventas,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['VENTAS_READ', 'VENTAS_MANAGE'] }
  },
  {
    path: 'inicio-inventario',
    component: InicioInventario,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['INVENTARIO_READ'] }
  },
  {
    path: 'busqueda-inventario',
    component: BusquedaInventario,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ['INVENTARIO_READ'] }
  },
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

  // RUTAS DE INVENTARIO (US 9 y 12) ADAPTADAS AL NUEVO SISTEMA:
  { 
    path: 'inventario', 
    component: InventarioHomeComponent, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['INVENTARIO_READ', 'INVENTARIO_MANAGE'] }
  },
  { 
    path: 'inventario/nuevo', 
    component: InventarioForm, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['INVENTARIO_MANAGE'] }
  },
  { 
    path: 'inventario/historial', 
    component: InventarioHistorial, 
    canActivate: [authGuard, permisosGuard], 
    data: { requiredPermisos: ['INVENTARIO_READ'] }
  },

  { path: 'access-denied', component: AccessDenied },
  { path: '**', component: NotFound }
];