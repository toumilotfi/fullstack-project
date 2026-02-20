import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { AuthController } from '../../controllers/auth.controller';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.page.html',
  styleUrls: ['./settings.page.scss'],
  standalone: true,
  imports: [CommonModule, IonicModule]
})
export class SettingsPage {
  private authCtrl = inject(AuthController);

  logout() {
    this.authCtrl.logout();
  }
}