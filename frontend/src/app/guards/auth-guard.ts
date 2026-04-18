import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const auth = inject(Auth);

  const token = auth.getToken();
  const permisos = auth.getPermisos();

  // 🧠 basic session validation
  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  // 🧠 if no permissions exist, treat as invalid session
  if (!permisos || permisos.length === 0) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};