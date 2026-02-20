import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule, RouterModule],
  templateUrl: './register.page.html',
  styleUrls: ['./register.page.scss']
})
export class RegisterPage {
  private router = inject(Router);
  user = { name: '', email: '', password: '' };

handleRegister() {
  console.log("Navigating to Approval...");
  // This must match the path in app.routes.ts exactly!
  this.router.navigate(['/approval-pending']); 
}
}