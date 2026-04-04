import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { User } from '../models/admin.model';

interface AuthResponse {
  token: string;
  user: User;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: '../views/login/login.html',
  styleUrl: '../views/login/login.css'
})
export class LoginComponent {
  private http = inject(HttpClient);
  private router = inject(Router);

  email = '';
  password = '';
  loading = false;
  errorMsg = '';

  login() {
    if (!this.email.trim() || !this.password.trim()) return;

    this.loading = true;
    this.errorMsg = '';

    this.http.post<AuthResponse>(
      `${environment.apiUrl}/auth/login`,
      { email: this.email, password: this.password }
    ).subscribe({
      next: (response) => {
        if (response.user.role !== 'ADMIN') {
          this.errorMsg = 'ACCESS DENIED: Administrator credentials required.';
          this.loading = false;
          return;
        }
        localStorage.setItem('admin_token', response.token);
        this.router.navigate(['/admin/dashboard']);
      },
      error: () => {
        this.errorMsg = 'Authentication failed. Check credentials and try again.';
        this.loading = false;
      }
    });
  }
}
