import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router'; 
import { AuthController } from '../../controllers/auth.controller';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule, RouterModule],
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss']
})
export class LoginPage {
  private auth = inject(AuthController);

  credentials = {
    email: '',
    password: ''
  };

  handleLogin() {
    if (!this.credentials.email || !this.credentials.password) {
      alert("System Alert: Please enter both email and password.");
      return;
    }

    console.log("Transmitting credentials to Lotfi's Gateway...");
    this.auth.login(this.credentials.email, this.credentials.password);
  }
}