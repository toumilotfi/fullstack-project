import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { AuthController } from '../../controllers/auth.controller';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  standalone: true,
  imports: [CommonModule, IonicModule]
})
export class ProfilePage {
  // FIXED: This line makes 'auth' available to your HTML button
  public auth = inject(AuthController); 
}