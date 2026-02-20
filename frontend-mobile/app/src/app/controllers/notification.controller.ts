import { Injectable, signal, computed } from '@angular/core';
import { AppNotification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationController {
  // Mock data for notifications
  public notifications = signal<AppNotification[]>([
    {
      id: '1',
      title: 'New Task Assigned',
      body: 'You have been assigned to "Design System Update"',
      type: 'task',
      createdAt: new Date(),
      isRead: false
    },
    {
      id: '2',
      title: 'Admin Approved',
      body: 'Your profile update has been approved by the admin.',
      type: 'system',
      createdAt: new Date(Date.now() - 3600000), // 1 hour ago
      isRead: true
    }
  ]);

  // Computed signal for unread count (useful for the navbar badge)
  public unreadCount = computed(() => 
    this.notifications().filter(n => !n.isRead).length
  );

  markAsRead(id: string) {
    this.notifications.update(list => 
      list.map(n => n.id === id ? { ...n, isRead: true } : n)
    );
  }
}