import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';
import { DashboardComponent } from './controllers/dashboard';
import { LoginComponent } from './controllers/login';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
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