import { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiError } from '../errors/api-error';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {

  return next(req).pipe(

    catchError((error: HttpErrorResponse) => {

      const normalized: ApiError = normalizeError(error);

      return throwError(() => normalized);
    })

  );
};

// =========================
// NORMALIZATION CORE
// =========================
function normalizeError(error: HttpErrorResponse): ApiError {

  // Case: backend structured error (your GlobalExceptionHandler)
  const backend = error.error;

  if (backend && typeof backend === 'object' && backend.errorCode) {
    return {
      status: error.status,
      errorCode: backend.errorCode,
      message: backend.message,
      path: backend.path,
      raw: backend
    };
  }

  // Case: fallback (network / unexpected / HTML error / etc.)
  return {
    status: error.status,
    errorCode: 'ERR_UNKNOWN',
    message: error.message || 'Unexpected error',
    raw: error.error
  };
}