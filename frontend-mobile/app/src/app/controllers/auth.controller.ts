import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { UserApi, AuthResponse } from '../api/user.api';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthController {
  private userApi = inject(UserApi);
  private router = inject(Router);

  currentUser = signal<User | null>(null);

  constructor() {
    const stored = localStorage.getItem('current_user');
    if (stored) {
      try {
        this.currentUser.set(new User(JSON.parse(stored)));
      } catch {
        localStorage.removeItem('current_user');
      }
    }
  }

  login(email: string, secretPassword: string) {
    this.userApi.login(email, secretPassword).subscribe({
      next: (response: AuthResponse) => {
        if (!response.user.userActive) {
          alert('ACCOUNT LOCKED: Awaiting Admin Approval.');
          return;
        }

        localStorage.setItem('auth_token', response.token);
        localStorage.setItem('current_user', JSON.stringify(response.user));
        this.currentUser.set(new User(response.user));
        this.router.navigate(['/home']);
      },
      error: () => alert("CONNECTION FAILED: Unable to reach the server. Please try again later.")
    });
  }

  register(user: User) {
    this.userApi.register(user).subscribe({
      next: (savedUser: User) => {
        alert("Registration complete! Awaiting Admin approval.");
        this.router.navigate(['/approval-pending'], { queryParams: { email: user.email } });
      },
      error: (err: any) => console.error("Registration Error", err)
    });
  }

  logout() {
    this.currentUser.set(null);
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
    this.userApi.logout().subscribe();
    this.router.navigate(['/login']);
  }
}
