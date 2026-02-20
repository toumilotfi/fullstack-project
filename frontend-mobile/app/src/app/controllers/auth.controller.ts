import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UiController } from './ui.controller';

@Injectable({ providedIn: 'root' })
export class AuthController {
  private router = inject(Router);
  private ui = inject(UiController);

  login(email: string, pass: string) {
    if (email && pass) {
      // Simulate a login delay for realism
      console.log('Logging in...');
      this.ui.setHasLayout(true); // Show the bottom bar
      this.router.navigate(['/home']);
    } else {
      alert('Please enter your email and password.');
    }
  }

  register(name: string, email: string, pass: string) {
    if (name && email && pass) {
      console.log('Registering...');
      // Send user to "Approval Pending" instead of Home
      this.ui.setHasLayout(false); // Hide the bottom bar
      this.router.navigate(['/approval-pending']);
    } else {
      alert('Please fill in all fields.');
    }
  }
  
  logout() {
    this.ui.setHasLayout(false);
    this.router.navigate(['/landing']);
  }
}