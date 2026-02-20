import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';

// 1. Controllers (Logic)
import { UiController } from '../../controllers/ui.controller';
import { TaskController } from '../../controllers/task.controller';

// 2. Components (The UI tools we just made)
import { TaskCardComponent } from '../../components/task-card/task-card.component';
import { GlassHeaderComponent } from '../../components/glass-header/glass-header.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule, 
    IonicModule, 
    TaskCardComponent,    // <--- Registered
    GlassHeaderComponent  // <--- Registered
  ],
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.scss']
})
export class HomePage implements OnInit {
  public ui = inject(UiController);
  public taskCtrl = inject(TaskController);

  ngOnInit() {
    this.ui.setHasLayout(true); // Shows the navigation bar
  }
}