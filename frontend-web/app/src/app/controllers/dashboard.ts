import { Component, inject, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
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
  private cdr = inject(ChangeDetectorRef);

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
    this.loadAllNotifications();
  }

  private getAdminIdFromToken(): number {
    try {
      const stored = localStorage.getItem('admin_user');
      if (stored) {
        const user = JSON.parse(stored);
        if (user?.id) return user.id;
      }
      const token = localStorage.getItem('admin_token');
      if (!token) return 0;
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId ?? payload.id ?? 0;
    } catch {
      return 0;
    }
  }

  loadAllNotifications() {
    const adminId = this.getAdminIdFromToken();
    if (!adminId) return;

    this.adminService.getUserNotifications(adminId)
      .subscribe({
        next: (notifications) => {
          this.sentAlerts = notifications.map(n => ({
            id: n.id,
            title: n.title,
            message: n.message,
            userId: n.userId,
            isRead: n.read ?? n.isRead,
            createdAt: n.createdAt
          }));
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Failed to load notifications', err);
          this.cdr.markForCheck();
        }
      });
  }

  toggleNotifications() {
    this.showDropdown = !this.showDropdown;
    if (this.showDropdown) this.unreadCount = 0;
  }

  sendGlobalAlert() {
    if (!this.broadcastMessage.trim()) return;

    this.adminService.sendGlobalNotification(this.broadcastMessage)
      .subscribe({
        error: (err) => console.error('Failed to send global alert', err),
        next: () => {
          const alert = {
            title: 'SYSTEM ALERT',
            text: this.broadcastMessage,
            type: 'GLOBAL',
            time: new Date(),
            colorClass: 'alert-global'
          };

          this.adminNotifications.unshift(alert);

          this.sentAlerts = [{
            title: 'SYSTEM ALERT',
            message: this.broadcastMessage,
            createdAt: new Date().toISOString(),
            userId: 0,
            isRead: false
          }, ...this.sentAlerts];

          this.broadcastMessage = '';
          this.cdr.markForCheck();
        }
      });
  }

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

          this.sentAlerts = [{
            title: 'DIRECTIVE',
            message: this.directMessage,
            createdAt: new Date().toISOString(),
            userId: this.targetUserId!,
            isRead: false
          }, ...this.sentAlerts];

          this.directMessage = '';
          this.cdr.markForCheck();
        },
        error: err => console.error(err)
      });
  }

  onRadarClick(alert: any) {
    this.showDropdown = false;
  }
}
