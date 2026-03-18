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
  getIcon(name: string): string {
  if (!name) return 'notifications-outline';

  if (name.toLowerCase().includes('approved')) return 'checkmark-circle';
  if (name.toLowerCase().includes('declined')) return 'close-circle';
  if (name.toLowerCase().includes('revision')) return 'create-outline';
  if (name.toLowerCase().includes('response')) return 'chatbubble-ellipses';
  
  return 'radio-outline'; // default
}
  
}