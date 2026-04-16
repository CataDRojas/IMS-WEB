import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

// roles.guard.ts
export const rolesGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const userRol = localStorage.getItem('rol_ims');

  const expectedRoles = route.data['expectedRoles'] as Array<string>;

  if (userRol && expectedRoles.includes(userRol)) {
    return true;
  }

  alert('No tienes permisos para esta sección');
  router.navigate(['/home']);
  return false;
};