import { Injectable, inject, signal } from '@angular/core';
import { NotificationApi } from '../api/notification.api';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationController {
  private notifyApi = inject(NotificationApi);
  
  notifications = signal<Notification[]>([]);
  unreadCount = signal<number>(0);

  loadNotifications(userId: number) {
    this.notifyApi.getUserNotifications(userId).subscribe({
      next: (data: Notification[]) => {
        this.notifications.set(data);
       const unread = data.filter(n => !n.read).length;

        this.unreadCount.set(unread);
      },
      error: (err) => console.error("Notification Radar Offline", err)
    });
  }

  markRead(notificationId: number) {
    this.notifyApi.markAsRead(notificationId).subscribe(() => {
      this.notifications.update(prev => 
        prev.map(n => n.id === notificationId ? { ...n, isRead: true } : n)
      );
      this.unreadCount.update(count => count > 0 ? count - 1 : 0);
    });
  }
}