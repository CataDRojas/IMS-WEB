import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { Home } from './pages/home/home';
import { UsuariosComponent } from './pages/usuarios/usuarios';
import { InventarioHomeComponent } from './pages/inventario/inventario-home/inventario-home';
import { InventarioForm } from './pages/inventario/inventario-form/inventario-form';
import { InventarioHistorial } from './pages/inventario/inventario-historial/inventario-historial';

import { authGuard } from './guards/auth-guard';
import { rolesGuard } from './guards/roles-guard';
import { Ventas } from './pages/ventas/ventas';
import { InicioInventario } from './pages/inicio-inventario/inicio-inventario';
import { BusquedaInventario } from './pages/busqueda-inventario/busqueda-inventario';

export const routes: Routes = [
    { path: '', redirectTo: 'login', pathMatch: 'full' }, 
  
  { path: 'login', component: LoginComponent },


  { path: 'home', component: Home, canActivate: [authGuard]},
  { path: 'usuarios', component: UsuariosComponent, canActivate: [authGuard, rolesGuard], data: { expectedRoles: ['ADMIN'] }},
  { path: 'ventas', component: Ventas, canActivate: [authGuard, rolesGuard], data: { expectedRoles: ['ADMIN', 'VENDEDOR']}},
  //{ path: 'inicio-inventario', component: InicioInventario, canActivate: [authGuard, rolesGuard], data: { expectedRoles: ['ADMIN', 'BODEGUERO']}},
  //{ path: 'busqueda-inventario', component: BusquedaInventario, canActivate: [authGuard, rolesGuard], data: { expectedRoles: ['ADMIN', 'BODEGUERO']}},
  { path: 'inventario', component: InventarioHomeComponent, canActivate: [authGuard, rolesGuard], data: { expectedRoles: ['ADMIN', 'BODEGUERO']}},
  { path: 'inventario/nuevo', component: InventarioForm, canActivate: [authGuard, rolesGuard], data: { expectedRoles: ['ADMIN', 'BODEGUERO']}},
  { path: 'inventario/historial', component: InventarioHistorial, canActivate: [authGuard, rolesGuard], data: { expectedRoles: ['ADMIN', 'BODEGUERO']}},
];
