import { Component } from '@angular/core';
import { IonicModule } from '@ionic/angular'; // <--- Import Ionic
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  // We MUST add IonicModule here so <ion-app> works
  imports: [IonicModule, CommonModule], 
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}