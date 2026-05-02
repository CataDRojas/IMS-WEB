import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login";
import { Home } from "./pages/home/home";
import { UsuariosComponent } from "./pages/usuarios/usuarios";
import { RolesList } from "./pages/roles/roles-list/roles-list";
import { authGuard } from "./guards/auth-guard";
import { permisosGuard } from "./guards/permisos-guard";

import { VentasForm } from "./pages/ventas/ventas";
import { NOT_FOUND } from "@angular/core/primitives/di";

import { NotFound } from "./pages/not-found/not-found";
import { AccessDenied } from "./pages/access-denied/access-denied";
import { CategoriasComponent } from "./pages/categorias/categorias";
import { ProductosComponent } from "./pages/productos/productos";
import { DescuentosListComponent } from "./pages/descuentos/descuentos-list/descuentos-list";
import { InventarioForm } from "./pages/inventario/inventario-form/inventario-form";
import { RecepcionForm } from "./pages/recepcion/recepcion-form/recepcion-form";
import { LugaresComponent } from "./pages/lugares/lugares-list/lugares-list";
import { ConfiguracionSistema } from "./pages/configuracion-sistema/configuracion-sistema";
import { VentasHistorial } from "./pages/ventas/ventas-historial/ventas-historial";

export const routes: Routes = [
  { path: "", redirectTo: "login", pathMatch: "full" },
  { path: "login", component: LoginComponent },
  { path: "home", component: Home, canActivate: [authGuard] },

  // =========================
  // USERS
  // =========================
  {
    path: "usuarios",
    component: UsuariosComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["USUARIOS_MANAGE"] },
  },

  // =========================
  // ROLES
  // =========================
  {
    path: "roles",
    component: RolesList,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["ROLES_MANAGE"] },
  },

  // =========================
  // VENTAS
  // =========================
  {
    path: "ventas",
    component: VentasForm,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["VENTA_READ", "VENTA_MANAGE"] },
  },

  // =========================
  // HISTORIAL UNIFICADO
  // =========================
  {
    path: "historial",
    component: VentasHistorial,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["VENTA_READ", "INVENTARIO_READ"] },
  },

  // =========================
  // PRODUCTOS
  // =========================
  {
    path: "productos",
    component: ProductosComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["PRODUCTO_MANAGE", "PRODUCTO_READ"] },
  },

  // =========================
  // CATEGORIAS
  // =========================
  {
    path: "categorias",
    component: CategoriasComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["CATEGORIA_MANAGE", "CATEGORIA_READ"] },
  },

  // =========================
  // DESCUENTOS
  // =========================
  {
    path: "descuentos",
    component: DescuentosListComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["DESCUENTO_READ"] },
  },

  // =========================
  // INVENTARIO
  // =========================
  {
    path: "inventario",
    component: InventarioForm,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["INVENTARIO_MANAGE", "INVENTARIO_READ"] },
  },

  // =========================
  // RECEPCION
  // =========================
  {
    path: "recepcion",
    component: RecepcionForm,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["INVENTARIO_MANAGE", "INVENTARIO_READ"] },
  },

  // =========================
  // LUGARES
  // =========================
  {
    path: "lugares",
    component: LugaresComponent,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["MOVIMIENTO_LUGAR_MANAGE"] },
  },

  // =========================
  // CONFIGURACION
  // =========================
  {
    path: "configuracion",
    component: ConfiguracionSistema,
    canActivate: [authGuard, permisosGuard],
    data: { requiredPermisos: ["CONFIGURACION_MANAGE"] },
  },

  // =========================
  // SYSTEM
  // =========================
  { path: "access-denied", component: AccessDenied },
  { path: "**", component: NotFound },
];
