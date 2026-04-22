import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';
import { map } from 'rxjs';

export const permisosGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const auth = inject(Auth);

  const requiredPermisos =
    (route.data?.['requiredPermisos'] as string[] | undefined) ?? [];

  if (requiredPermisos.length === 0) return true;

  const refresh$ = auth.refreshPermisosIfNeeded();

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

return refresh$.pipe(
  map((user: any) => {

    auth.setPermisos(user.permisos);

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