import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationController {
  unreadCount = signal(3); // Example: 3 unread messages

  clearNotifications() {
    this.unreadCount.set(0);
  }
}