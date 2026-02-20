import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full',
  },
  {
    path: 'landing',
    loadComponent: () => import('./views/landing/landing.page').then(m => m.LandingPage),
  },
  {
    path: 'login',
    loadComponent: () => import('./views/login/login.page').then(m => m.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () => import('./views/register/register.page').then(m => m.RegisterPage),
  },
  {
    // THIS IS THE CRITICAL LINE FOR APPROVAL PAGE
    path: 'approval-pending',
    loadComponent: () => import('./views/approval-pending/approval-pending.page').then(m => m.ApprovalPendingPage),
  },
  {
    path: 'home',
    loadComponent: () => import('./views/home/home.page').then(m => m.HomePage),
  },
  {
    path: 'messaging',
    loadComponent: () => import('./views/messaging/messaging.page').then(m => m.MessagingPage),
  },
  {
    path: 'notifications',
    loadComponent: () => import('./views/notifications/notifications.page').then(m => m.NotificationsPage),
  },
  {
    path: 'profile',
    loadComponent: () => import('./views/profile/profile.page').then(m => m.ProfilePage),
  },
];