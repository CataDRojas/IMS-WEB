import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const permisosGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  const permisosRaw = localStorage.getItem('permisos_ims');
  const userPermisos: string[] = permisosRaw ? JSON.parse(permisosRaw) : [];

  const requiredPermisos = (route.data?.['requiredPermisos'] as string[] | undefined) ?? [];

  console.log('Permisos guard check');
  console.log('User:', userPermisos);
  console.log('Required:', requiredPermisos);

  // no restriction → allow access
  if (requiredPermisos.length === 0) return true;

  // OR-based access (more realistic for ERP systems)
  const hasAccess = requiredPermisos.some(p => userPermisos.includes(p));

  if (hasAccess) {
    return true;
  }

  console.log('Access denied');
  router.navigate(['/home']);
  return false;
};