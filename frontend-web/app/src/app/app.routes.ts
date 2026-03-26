import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';
import { DashboardComponent } from './controllers/dashboard';

export const routes: Routes = [
  { path: '', redirectTo: 'admin', pathMatch: 'full' },
  {
    path: 'admin',
    component: AdminLayoutComponent,
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