import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { Auth } from '../services/auth';

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

    catchError((error: HttpErrorResponse) => {

      // 🔴 session expired / invalid token
      if (error.status === 401) {
        auth.logout();
      }

      // 🟡 permissions outdated
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