import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { AuthController } from '../../controllers/auth.controller';
import { TaskController } from '../../controllers/task.controller';
import { GlassHeaderComponent } from '../../components/glass-header/glass-header.component';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, IonicModule, GlassHeaderComponent],
  templateUrl: './tasks.page.html',
  styleUrls: ['./tasks.page.scss']
})
export class TasksPage implements OnInit {
  public auth = inject(AuthController);
  public taskCtrl = inject(TaskController);

  ngOnInit() {
    const user = this.auth.currentUser();
    if (user && user.id) {
      this.taskCtrl.loadUserTasks(user.id);
    }
  }
}