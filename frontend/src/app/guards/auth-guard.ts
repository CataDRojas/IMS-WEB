import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  const token = localStorage.getItem('token_ims');
  const permisos = localStorage.getItem('permisos_ims');

  console.log('El Guardia está revisando la puerta...');
  console.log('Token encontrado:', token);
  console.log('Permisos encontrados:', permisos);

  if (token && permisos) {
    console.log('Acceso permitido a:', route.url.toString());
    return true;
  } else {
    console.log('Acceso denegado');
    router.navigate(['/login']);
    return false;
  }
};