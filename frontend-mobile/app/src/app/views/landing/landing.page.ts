import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { addIcons } from 'ionicons';
import { shieldCheckmarkOutline, arrowForward } from 'ionicons/icons';

addIcons({
  'shield-checkmark-outline': shieldCheckmarkOutline,
  'arrow-forward': arrowForward
});
export class YourPage {}
@Component({
  selector: 'app-landing',
  templateUrl: './landing.page.html',
  styleUrls: ['./landing.page.scss'],
  standalone: true,
  imports: [CommonModule, IonicModule, RouterModule]
})
export class LandingPage {}