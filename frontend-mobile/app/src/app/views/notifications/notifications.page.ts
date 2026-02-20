import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { NotificationController } from '../../controllers/notification.controller';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.page.html',
  styleUrls: ['./notifications.page.scss'],
  standalone: true,
  imports: [CommonModule, IonicModule]
})
export class NotificationsPage {
  public notifyCtrl = inject(NotificationController);

  markRead(id: string) {
    this.notifyCtrl.markAsRead(id);
  }
}