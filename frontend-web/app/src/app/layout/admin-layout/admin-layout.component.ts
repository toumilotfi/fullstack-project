import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, IonicModule, RouterModule, SidebarComponent],
  template: `
    <div class="admin-wrapper">
      <app-sidebar></app-sidebar>
      <main class="content-area">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .admin-wrapper { display: flex; width: 100vw; height: 100vh; overflow: hidden; background: #0f172a; }
    .content-area { flex: 1; overflow-y: auto; padding: 40px; background: radial-gradient(circle at top right, #1e1b4b 0%, #0f172a 60%); }
  `]
})
export class AdminLayoutComponent {}