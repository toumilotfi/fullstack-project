import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service';  

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule],
  templateUrl: '../views/login/login.html',
  styleUrl: '../views/login/login.css'
})
export class LoginComponent {
   private adminService = inject(AdminService);
  private router = inject(Router);

  credentials = { email: '', password: '' };
  errorMessage = '';

  onLogin() {
     this.adminService.http.post<any>('http://localhost:8080/api/v1/auth/login', this.credentials).subscribe({
      next: (res: any) => { 
        if (res.token) {
           localStorage.setItem('admin_token', res.token);
          this.router.navigate(['/admin/dashboard']);
        }
      },
      error: (err: any) => {  
        this.errorMessage = 'Authorization Denied: Invalid Access Key';
      }
    });
  }
}