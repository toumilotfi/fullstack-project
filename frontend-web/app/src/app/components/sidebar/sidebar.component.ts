import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { AdminWebSocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, IonicModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class SidebarComponent {
  router = inject(Router);
  webSocket = inject(AdminWebSocketService);

  signOut() {
    if (typeof localStorage !== 'undefined') localStorage.removeItem('admin_token');
    this.webSocket.disconnect();
    this.router.navigate(['/login']);
  }
}