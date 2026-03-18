import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule, ToastController } from '@ionic/angular'; 
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms'; 

import { AuthController } from '../../controllers/auth.controller';
import { TaskController } from '../../controllers/task.controller';
import { NotificationController } from '../../controllers/notification.controller';
import { ChatController } from '../../controllers/chat.controller';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, IonicModule, RouterModule, FormsModule],
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.scss']
})
export class HomePage implements OnInit, OnDestroy {
  public auth = inject(AuthController);
  public taskCtrl = inject(TaskController);
  public notifyCtrl = inject(NotificationController);
  public chatCtrl = inject(ChatController);
  private toastController = inject(ToastController); 

  private poller: any;
  
  activeView: 'DASHBOARD' | 'EXECUTION' = 'DASHBOARD';
  activeTask: Task | null = null;
  taskReport: string = '';
  isSending: boolean = false;

  ngOnInit() {
    this.refreshDashboard();
    this.poller = setInterval(() => this.refreshDashboard(), 5000);
  }

  ngOnDestroy() { 
    if (this.poller) clearInterval(this.poller); 
  }

  getActiveTasks() {
    return this.taskCtrl.userTasks().filter(t => !t.completed);
  }

  refreshDashboard() {
    const user = this.auth.currentUser();
    if (user?.id) {
      this.taskCtrl.loadUserTasks(user.id);
      this.notifyCtrl.loadNotifications(user.id);
    }
  }

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
      error: () => this.showToast('TERMINATION ERROR', 'danger')
    });
  }

  async submitToInspector() {
    if (!this.activeTask?.id || !this.taskReport.trim()) {
      this.showToast('DATA REQUIRED', 'warning');
      return;
    }

    this.isSending = true;
    this.taskCtrl.submitTaskResponse(this.activeTask.id, this.taskReport).subscribe({
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

  async showToast(msg: string, color: string) {
    const toast = await this.toastController.create({ 
      message: msg, 
      duration: 2000, 
      color: color, 
      position: 'top',
      cssClass: 'cyber-toast' 
    });
    await toast.present();
  }
}