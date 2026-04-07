import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
export const routes: Routes = [

  { path: '', redirectTo: 'landing', pathMatch: 'full' },
  { path: 'landing', loadComponent: () => import('./views/landing/landing.page').then(m => m.LandingPage) },
  { path: 'login', loadComponent: () => import('./views/login/login.page').then(m => m.LoginPage) },
  { path: 'register', loadComponent: () => import('./views/register/register.page').then(m => m.RegisterPage) },
  { path: 'approval-pending', loadComponent: () => import('./views/approval-pending/approval-pending.page').then(m => m.ApprovalPendingPage) },
  { path: 'forgot-password', loadComponent: () => import('./views/forgot-password/forgot-password.page').then(m => m.ForgotPasswordPage) },
  { path: 'home', canActivate: [authGuard], loadComponent: () => import('./views/home/home.page').then(m => m.HomePage) },
  { path: 'profile', canActivate: [authGuard], loadComponent: () => import('./views/profile/profile.page').then(m => m.ProfilePage) },
  { path: 'messaging', canActivate: [authGuard], loadComponent: () => import('./views/messaging/messaging.page').then(m => m.MessagingPage) },
  
  { path: 'notifications', canActivate: [authGuard], loadComponent: () => import('./views/notifications/notifications.page').then(m => m.NotificationsPage) },
  { path: 'tasks', canActivate: [authGuard], loadComponent: () => import('./views/tasks/tasks.page').then(m => m.TasksPage) },
     { path: 'tasks-card', canActivate: [authGuard], loadComponent: () => import('./components/task-card/task-card.component').then(m => m.TaskCardComponent) }
];
