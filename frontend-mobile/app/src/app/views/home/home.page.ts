import { Component, inject, OnInit, OnDestroy, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule, ToastController } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AuthController } from '../../controllers/auth.controller';
import { TaskController } from '../../controllers/task.controller';
import { NotificationController } from '../../controllers/notification.controller';
import { ChatController } from '../../controllers/chat.controller';
import { Task } from '../../models/task.model';

import { addIcons } from 'ionicons';
import {
  personOutline,
  chatbubblesOutline,
  syncOutline,
  albumsOutline,
  notificationsOutline
} from 'ionicons/icons';

addIcons({
  personOutline,
  chatbubblesOutline,
  syncOutline,
  albumsOutline,
  notificationsOutline
});

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, IonicModule, RouterModule, FormsModule],
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.scss']
})
export class HomePage implements OnInit, OnDestroy {

  // 🔌 Inject
  public auth = inject(AuthController);
  public taskCtrl = inject(TaskController);
  public notifyCtrl = inject(NotificationController);
  public chatCtrl = inject(ChatController);
  private toastController = inject(ToastController);

  private poller!: ReturnType<typeof setInterval>;

  // 🧠 STATE
  activeView: 'DASHBOARD' | 'EXECUTION' = 'DASHBOARD';
  activeTask: Task | null = null;
  activeTasks: Task[] = [];

  taskReport = '';
  isSending = false;
  today: Date = new Date();

  constructor() {
    // Reactively update activeTasks whenever the signal changes (after API response arrives)
    effect(() => {
      this.activeTasks = this.taskCtrl.userTasks()
        .filter((t: Task) => t.status !== 'APPROVED' && t.status !== 'DECLINED');
    });
  }

  // 🚀 INIT
  ngOnInit() {
    this.refreshDashboard();
    this.startPolling();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  // 🔁 POLLING
  private startPolling() {
    this.poller = setInterval(() => {
      this.refreshDashboard();
    }, 5000);
  }

  private stopPolling() {
    if (this.poller) clearInterval(this.poller);
  }

  // 📊 DATA
  private updateActiveTasks() {
    this.activeTasks = [...this.taskCtrl.userTasks()]
      .filter(t => t.status !== 'APPROVED' && t.status !== 'DECLINED');
  }

  refreshDashboard() {
    const user = this.auth.currentUser();

    if (!user?.id) return;

    this.taskCtrl.loadUserTasks(user.id);
    this.notifyCtrl.loadNotifications(user.id);
    // activeTasks is updated reactively via effect() when userTasks signal changes
  }

  // 🎯 ACTIONS
  acceptTask(task: Task) {
    this.activeTask = task;
    this.activeView = 'EXECUTION';
    this.taskReport = '';
  }

  goHome() {
    this.activeView = 'DASHBOARD';
    this.activeTask = null;
    this.taskReport = '';
  }

  rejectTask(task: Task) {
    if (!task.id) return;

    this.taskCtrl.rejectTask(task.id).subscribe({
      next: () => {
        this.showToast('PROTOCOL ABORTED', 'medium');
        this.refreshDashboard();
      },
      error: () => {
        this.showToast('TERMINATION ERROR', 'danger');
      }
    });
  }

  submitToInspector() {
    if (!this.activeTask?.id || !this.taskReport.trim()) {
      this.showToast('DATA REQUIRED', 'warning');
      return;
    }

    this.isSending = true;

    this.taskCtrl.submitTaskResponse(this.activeTask.id, this.taskReport)
      .subscribe({
        next: () => {
          this.showToast('INTEL TRANSMITTED', 'success');
          this.isSending = false;
          this.goHome();
          this.refreshDashboard();
        },
        error: () => {
          this.isSending = false;
          this.showToast('UPLINK FAILED', 'danger');
        }
      });
  }

  // ⚡ PERFORMANCE
  trackByTaskId(index: number, task: Task) {
    return task.id ?? index;
  }

  // 🔔 TOAST
  async showToast(msg: string, color: string) {
    const toast = await this.toastController.create({
      message: msg,
      duration: 2000,
      color,
      position: 'top',
      cssClass: 'cyber-toast'
    });

    await toast.present();
  }
  getStatusColor(status?: string) {
    switch (status) {
      case 'SUBMITTED':
        return 'warning';

      case 'ASSIGNED':
        return 'primary';

      case 'APPROVED':
        return 'success';

      case 'DECLINED':
        return 'danger';

      case 'REVISION_REQUESTED':
        return 'tertiary';

      default:
        return 'medium';
    }
  }
}