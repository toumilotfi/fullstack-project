import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { AuthController } from '../../controllers/auth.controller';
import { NotificationController } from '../../controllers/notification.controller';
import { Router } from '@angular/router';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: './notifications.page.html',
  styleUrls: ['./notifications.page.scss'],
})
export class NotificationsPage implements OnInit {
  public auth = inject(AuthController);
  public notifyCtrl = inject(NotificationController);
  private router = inject(Router);

  ngOnInit() {
    const user = this.auth.currentUser();
    if (user?.id) {
      this.notifyCtrl.loadNotifications(user.id);
    }
  }
onNotificationClick(notification: any) {
  if (!notification.id) return;

  this.notifyCtrl.markRead(notification.id);

  const match = notification.message.match(/Task #(\d+)/);
  if (match) {
    this.router.navigate(['/home']);
  }
}


  /** 
   * Returns the correct icon based on notification type or message content 
   */




  /**
   * Returns a readable title based on type
   */
  getTitle(notification: any): string {
    if (!notification) return 'HQ MESSAGE';

    if (notification.type === 'GLOBAL') return 'GLOBAL BROADCAST';
    if (notification.type === 'DIRECT') return 'DIRECTIVE';
    if (notification.type === 'ALERT') return 'SYSTEM ALERT';
    if (notification.type === 'NEW_USER') return 'NEW AGENT REGISTERED';
    if (notification.type === 'TASK_REPORT') return 'TASK REPORT';

    return 'HQ MESSAGE';
  }
 getIconClass(notification: any): string {
  switch (notification.type) {
    case 'GLOBAL': return 'global-icon';
    case 'DIRECT': return 'direct-icon';
    case 'ALERT': return 'alert-icon';
    case 'NEW_USER': return 'user-icon';
    case 'TASK_REPORT': return 'task-icon';
    default: return '';
  }
}

getIcon(notification: any): string {
  if (!notification) return 'alert-circle-outline';

  if (notification.type) {
    switch (notification.type) {
      case 'GLOBAL': return 'megaphone-outline';
      case 'DIRECT': return 'shield-checkmark-outline';
      case 'ALERT': return 'warning-outline';
      case 'NEW_USER': return 'person-add-outline';
      case 'TASK_REPORT': return 'document-text-outline';
    }
  }

  const msg = notification.message?.toLowerCase() || '';

  if (msg.includes('approved')) return 'checkmark-circle-outline';
  if (msg.includes('declined')) return 'close-circle-outline';
  if (msg.includes('revision')) return 'create-outline';
  if (msg.includes('response')) return 'chatbubble-ellipses-outline';

  return 'notifications-outline'; // fallback
}


}
