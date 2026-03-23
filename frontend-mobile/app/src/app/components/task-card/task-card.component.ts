import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule, ToastController } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { AuthController } from '../../controllers/auth.controller';
import { TaskController } from '../../controllers/task.controller';
import { GlassHeaderComponent } from '../../components/glass-header/glass-header.component';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-task-card',
  standalone: true,
    imports: [CommonModule, IonicModule, FormsModule, ],
  templateUrl: './task-card.component.html',
  styleUrls: ['./task-card.component.scss']
})

export class TasksPage implements OnInit {

  public auth = inject(AuthController);
  public taskCtrl = inject(TaskController);
  private toastCtrl = inject(ToastController);

  private userId!: number;

  // 🚀 INIT
  ngOnInit() {
    const user = this.auth.currentUser();

    if (user?.id) {
      this.userId = user.id;
      this.taskCtrl.loadUserTasks(this.userId);
    }
  }

  // ================= RESPOND =================
  respond(task: Task & { tempResponse?: string }) {

    // 🔥 FIX: ensure id is number
    if (!task.id || typeof task.id !== 'number') return;

    if (!task.tempResponse?.trim()) {
      this.showToast('Write a response first ⚠️', 'warning');
      return;
    }

    this.taskCtrl.respondToTask(task.id, task.tempResponse).subscribe({
      next: () => {
        this.showToast('Response sent ✅', 'success');

        this.taskCtrl.loadUserTasks(this.userId);

        task.tempResponse = '';
      },
      error: () => {
        this.showToast('Error sending response ❌', 'danger');
      }
    });
  }

  // 🎨 STATUS
  getStatusColor(status?: string) {
    switch (status) {
      case 'APPROVED': return 'success';
      case 'DECLINED': return 'danger';
      case 'SUBMITTED': return 'primary';
      case 'REVISION_REQUESTED': return 'warning';
      case 'ASSIGNED': return 'medium';
      default: return 'medium';
    }
  }

  // 📊 PROGRESS
  getProgress(status?: string) {
    switch (status) {
      case 'ASSIGNED': return 0.2;
      case 'SUBMITTED': return 0.5;
      case 'REVISION_REQUESTED': return 0.7;
      case 'APPROVED': return 1;
      case 'DECLINED': return 1;
      default: return 0;
    }
  }

  // ⚡ PERFORMANCE
  trackByTaskId(index: number, task: Task) {
    return task.id ?? index;
  }

  // 🔔 TOAST
  async showToast(message: string, color: string) {
    const toast = await this.toastCtrl.create({
      message,
      duration: 2000,
      position: 'top',
      color
    });
    await toast.present();
  }
}