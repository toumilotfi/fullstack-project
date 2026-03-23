import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { UserApi } from '../api/user.api'; 
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthController {
  private userApi = inject(UserApi); 
  private router = inject(Router);

  currentUser = signal<User | null>(null);

  login(email: string, secretPassword: string) {
    this.userApi.login(email, secretPassword).subscribe({
      next: (response: string) => {
        if (response.includes("successful")) {
          
          this.userApi.getAllUsers().subscribe({
            next: (users: User[]) => { 
              const myUser = users.find(u => u.email === email);
              if (myUser) {
                if (!myUser.userActive) {
                  alert("ACCOUNT LOCKED: Awaiting Admin Approval.");
                  return;
                }
                this.currentUser.set(myUser);
                this.router.navigate(['/home']);
              }
            }
          });
          
        } else {
          alert("ACCESS DENIED: " + response);
        }
      },
      error: (err: any) => alert("CONNECTION FAILED: Is Lotfi's server running?")
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
    this.userApi.logout().subscribe(); 
    this.router.navigate(['/login']);
  }
}