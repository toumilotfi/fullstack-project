import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service';  
import { AppNotification } from '../models/admin.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule],
  templateUrl: '../views/dashboard/dashboard.html',
  styleUrl: '../views/dashboard/dashboard.css'
})
export class DashboardComponent implements OnInit {
  public adminService = inject(AdminService);
  
  today = new Date();
  showDropdown = false;
  unreadCount = 0;
  
  broadcastMessage = '';
  directMessage = '';
  targetUserId?: number;
  
  adminNotifications: any[] = [];
  sentAlerts: AppNotification[] = [];

  ngOnInit() {
    this.adminService.loadUsers();
    this.adminService.loadTasks();
  }

  toggleNotifications() { this.showDropdown = !this.showDropdown; }

  sendGlobalAlert() {
    if (!this.broadcastMessage.trim()) return;
    this.adminService.sendGlobalNotification('SYSTEM ALERT', this.broadcastMessage).subscribe({
      next: (res: any) => this.broadcastMessage = '', 
    });
  }

  sendDirectAlert() {
    if (!this.directMessage.trim() || !this.targetUserId) return;
    this.adminService.sendDirectNotification(this.targetUserId, 'DIRECTIVE', this.directMessage).subscribe({
      next: (res: any) => this.directMessage = '',
      error: (err: any) => console.error(err)
    });
  }

  onRadarClick(alert: any) { this.showDropdown = false; }
}