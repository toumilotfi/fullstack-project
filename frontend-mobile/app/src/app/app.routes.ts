import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'landing', pathMatch: 'full' },
  { path: 'landing', loadComponent: () => import('./views/landing/landing.page').then(m => m.LandingPage) },
  { path: 'login', loadComponent: () => import('./views/login/login.page').then(m => m.LoginPage) },
  { path: 'register', loadComponent: () => import('./views/register/register.page').then(m => m.RegisterPage) },
  { path: 'approval-pending', loadComponent: () => import('./views/approval-pending/approval-pending.page').then(m => m.ApprovalPendingPage) },
  // Dashboard Pages
  { path: 'home', loadComponent: () => import('./views/home/home.page').then(m => m.HomePage) },
  { path: 'tasks', loadComponent: () => import('./views/tasks/tasks.page').then(m => m.TasksPage) },
];