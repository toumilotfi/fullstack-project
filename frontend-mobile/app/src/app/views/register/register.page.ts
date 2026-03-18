import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { AuthController } from '../../controllers/auth.controller';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule, RouterModule],
  templateUrl: './register.page.html',
  styleUrls: ['./register.page.scss']
})
export class RegisterPage {
  private auth = inject(AuthController);

  user: User = new User({
    firstName: '',
    lastName: '',
    email: '',
    secretPassword: '',
    userActive: false 
  });

  handleRegister() {
    if(!this.user.firstName || !this.user.email || !this.user.secretPassword) {
      alert("Please fill in all required fields.");
      return;
    }

    console.log("Transmitting new agent profile to Gateway...");
    this.auth.register(this.user);
  }
}