import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule, ToastController } from '@ionic/angular'; 
import { RouterModule, Router } from '@angular/router';
import { UserApi } from '../../api/user.api';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule, RouterModule],
  templateUrl: './forgot-password.page.html',
  styleUrls: ['./forgot-password.page.scss']
})
export class ForgotPasswordPage {
  email = '';
  private userApi = inject(UserApi);
  private toastCtrl = inject(ToastController);
  private router = inject(Router);

  async sendResetLink() {
    if (!this.email) {
      this.showToast('Please enter your registered email.', 'warning');
      return;
    }

    this.userApi.forgotPassword(this.email).subscribe({
      next: async (response: string) => {
        await this.showToast('Reset sequence initiated. Check your inbox.', 'success');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: async (err: any) => {
        console.error("Auth Radar Error:", err);
        await this.showToast('Transmission failed. User not found or server offline.', 'danger');
      }
    });
  }

  async showToast(msg: string, color: string) {
    const toast = await this.toastCtrl.create({
      message: msg,
      duration: 3000,
      position: 'top',
      color: color,
      cssClass: 'cyber-toast'
    });
    await toast.present();
  }
}