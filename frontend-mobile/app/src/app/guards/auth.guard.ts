import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);

  if (typeof localStorage !== 'undefined' && localStorage.getItem('auth_token')) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
