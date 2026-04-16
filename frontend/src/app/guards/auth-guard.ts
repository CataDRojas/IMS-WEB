import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  
  const token = localStorage.getItem('token_ims');
  
  console.log('El Guardia está revisando la puerta...');
  console.log('¿Qué encontró en el bolsillo?:', token);

  if (token) {
    console.log('¡El Guardia te deja pasar a', route.url.toString(), '!');
    return true; 
  } else {
    console.log('¡El Guardia te bloqueó el paso!');
    router.navigate(['/login']);
    return false;
  }
};