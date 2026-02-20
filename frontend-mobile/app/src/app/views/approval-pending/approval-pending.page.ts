import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { AuthController } from '../../controllers/auth.controller';

@Component({
  selector: 'app-approval-pending',
  templateUrl: './approval-pending.page.html',
  styleUrls: ['./approval-pending.page.scss'],
  standalone: true,
  imports: [CommonModule, IonicModule, RouterModule]
})
export class ApprovalPendingPage {
  private authCtrl = inject(AuthController);

  handleLogout() {
    this.authCtrl.logout();
  }
}