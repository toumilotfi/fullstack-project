import { Component, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
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
  styleUrl: '../views/dashboard/dashboard.css',
  changeDetection: ChangeDetectionStrategy.OnPush
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
    this.loadAllNotifications();   // <-- Load logs from backend
  }

  loadAllNotifications() {
    const adminId = 1; // Replace with real admin ID if needed

    this.adminService.getUserNotifications(adminId)
      .subscribe({
        next: (notifications) => {
          this.sentAlerts = notifications.map(n => ({
            id: n.id,
            title: n.title,
            message: n.message,
            userId: n.userId,
            isRead: n.isRead,
            createdAt: n.createdAt
          }));
        }
      });
  }

  toggleNotifications() {
    this.showDropdown = !this.showDropdown;
    if (this.showDropdown) this.unreadCount = 0;
  }

  // GLOBAL ALERT
  sendGlobalAlert() {
    if (!this.broadcastMessage.trim()) return;

    this.adminService.sendGlobalNotification(this.broadcastMessage)
      .subscribe({
        next: () => {
          const alert = {
            title: 'SYSTEM ALERT',
            text: this.broadcastMessage,
            type: 'GLOBAL',
            time: new Date(),
            colorClass: 'alert-global'
          };

          this.adminNotifications.unshift(alert);

          this.sentAlerts.unshift({
            title: 'SYSTEM ALERT',
            message: this.broadcastMessage,
            createdAt: new Date().toISOString(),
            userId: 0,
            isRead: false
          });

          this.broadcastMessage = '';
        }
      });
  }

  // DIRECT ALERT
  sendDirectAlert() {
    if (!this.directMessage.trim() || !this.targetUserId) return;

    this.adminService.sendDirectNotification(this.targetUserId, this.directMessage)
      .subscribe({
        next: () => {
          const alert = {
            title: `DIRECTIVE TO #${this.targetUserId}`,
            text: this.directMessage,
            type: 'DIRECT',
            time: new Date(),
            colorClass: 'alert-direct'
          };

          this.adminNotifications.unshift(alert);

          this.sentAlerts.unshift({
            title: 'DIRECTIVE',
            message: this.directMessage,
            createdAt: new Date().toISOString(),
            userId: this.targetUserId!,
            isRead: false
          });

          this.directMessage = '';
        },
        error: err => console.error(err)
      });
  }

  onRadarClick(alert: any) {
    this.showDropdown = false;
  }
}
