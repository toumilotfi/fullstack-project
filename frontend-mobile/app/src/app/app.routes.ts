import { Routes } from '@angular/router';
export const routes: Routes = [

  { path: '', redirectTo: 'landing', pathMatch: 'full' },
  { path: 'landing', loadComponent: () => import('./views/landing/landing.page').then(m => m.LandingPage) },
  { path: 'login', loadComponent: () => import('./views/login/login.page').then(m => m.LoginPage) },
  { path: 'register', loadComponent: () => import('./views/register/register.page').then(m => m.RegisterPage) },
  { path: 'approval-pending', loadComponent: () => import('./views/approval-pending/approval-pending.page').then(m => m.ApprovalPendingPage) },
  { path: 'forgot-password', loadComponent: () => import('./views/forgot-password/forgot-password.page').then(m => m.ForgotPasswordPage) },
  { path: 'home', loadComponent: () => import('./views/home/home.page').then(m => m.HomePage) },
  { path: 'profile', loadComponent: () => import('./views/profile/profile.page').then(m => m.ProfilePage) },
  { path: 'messaging', loadComponent: () => import('./views/messaging/messaging.page').then(m => m.MessagingPage) },
  
  { path: 'notifications', loadComponent: () => import('./views/notifications/notifications.page').then(m => m.NotificationsPage) },
  { path: 'tasks', loadComponent: () => import('./views/tasks/tasks.page').then(m => m.TasksPage) },
     { path: 'tasks-card', loadComponent: () => import('./components/task-card/task-card.component').then(m => m.TasksPage) }
];