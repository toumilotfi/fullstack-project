import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';
import { DashboardComponent } from './controllers/dashboard';
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (typeof localStorage !== 'undefined' && localStorage.getItem('admin_token')) {
    return true;
  }
  return router.createUrlTree(['/login']);
};

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./controllers/login').then(m => m.LoginComponent)
  },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      {
        path: 'approvals',
        loadComponent: () => import('./controllers/approvals').then(m => m.ApprovalsComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./controllers/users').then(m => m.UsersComponent)
      },
      {
        path: 'tasks',
        loadComponent: () => import('./controllers/tasks').then(m => m.TasksComponent)
      },
      {
        path: 'inspector',
        loadComponent: () => import('./controllers/inspector').then(m => m.InspectorComponent)
      },
      {
        path: 'chat',
        loadComponent: () => import('./controllers/chat').then(m => m.ChatComponent)
      }
    ]
  }
];