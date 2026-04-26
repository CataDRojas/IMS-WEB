import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { Auth } from '../services/auth';
import { ApiError } from '../core/errors/api-error';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(Auth);
  const token = localStorage.getItem('token_ims');

  const authReq = token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : req;

  return next(authReq).pipe(

    catchError((error: ApiError) => {

      // 🔐 UNAUTHORIZED → logout
      if (error.status === 401) {
        auth.logout();
      }

      // 🔄 FORBIDDEN → try refresh permisos
      if (error.status === 403) {

        const refresh$ = auth.refreshMe?.();

        if (refresh$) {
          refresh$.subscribe({
            next: (user: any) => {
              auth.setPermisos(user.permisos);
            },
            error: () => {
              auth.logout();
            }
          });
        }
      }

      return throwError(() => error);
    })

  );
};