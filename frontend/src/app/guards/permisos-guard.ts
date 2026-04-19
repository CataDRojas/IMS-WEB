import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';
import { map } from 'rxjs';

export const permisosGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const auth = inject(Auth);

  const requiredPermisos =
    (route.data?.['requiredPermisos'] as string[] | undefined) ?? [];

  // 🧠 if no restrictions → allow immediately
  if (requiredPermisos.length === 0) return true;

  // 🧠 try lazy refresh (ONLY if stale)
  const refresh$ = auth.refreshPermisosIfNeeded();

  // =========================
  // CASE 1: no refresh needed
  // =========================
  if (!refresh$) {
    const userPermisos = auth.getPermisos();

    const allowed = requiredPermisos.some(p =>
      userPermisos.includes(p)
    );

    if (!allowed) {
      router.navigate(['/access-denied']);
    }

    return allowed;
  }

  // =========================
  // CASE 2: refresh needed
  // =========================
return refresh$.pipe(
  map((user: any) => {

    // 🧠 update cache first (ONLY perms, as your guard expects)
    auth.setPermisos(user.permisos);

    // 🧠 ALSO sync role data (without introducing new patterns)
    localStorage.setItem('rol_ims', user.rol);
    localStorage.setItem('rol_id_ims', String(user.rolId));
    localStorage.setItem('nombre_ims', user.nombre);

    const allowed = requiredPermisos.some(p =>
      user.permisos.includes(p)
    );

    if (!allowed) {
      router.navigate(['/access-denied']);
    }

    return allowed;
  })
);
};