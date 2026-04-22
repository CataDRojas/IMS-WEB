import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const auth = inject(Auth);

  const token = auth.getToken();
  const permisos = auth.getPermisos();

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  if (!permisos || permisos.length === 0) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};