import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';

@Component({
  selector: 'app-glass-header',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: './glass-header.component.html',
  styleUrls: ['./glass-header.component.scss']
})
export class GlassHeaderComponent {
  @Input() title: string = 'TaskFlow';
  @Input() subtitle?: string; // Optional
}