import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service'; 
import { Task, User } from '../models/admin.model';
import { environment } from '../../environments/environment';
@Component({
  selector: 'app-inspector',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: '../views/inspector/inspector.html',
  styleUrl: '../views/inspector/inspector.css',
})
export class InspectorComponent implements OnInit {
  public adminService = inject(AdminService);
  processingId: number | null = null;

  ngOnInit() {
     this.adminService.loadTasks();
    this.adminService.loadUsers();
  }

 

  getAgentName(userId: number): string {
    const user = this.adminService.users().find((u: User) => u.id === userId); // ✅ Added typing
    return user ? `${user.firstName} ${user.lastName}` : 'UNKNOWN AGENT';
  }
getPendingTasks(): Task[] {
  return this.adminService.tasks().filter((t: Task) =>
    t.status === 'SUBMITTED'
  );
}

acceptTask(task: Task) {
  if (!task.id) return;
  this.processingId = task.id;

  this.adminService.http.put(
    `${environment.apiUrl}/Task/tasks/approve/${task.id}`,
    {}
  ).subscribe({
    next: () => {
      this.adminService.loadTasks();
      this.processingId = null;
    },
    error: (err) => {
      console.error('Authorization Failed', err);
      this.processingId = null;
    }
  });
}

rejectTask(task: Task) {
  if (!task.id) return;
  this.processingId = task.id;

  this.adminService.http.put(
    `${environment.apiUrl}/Task/tasks/${task.id}/decline`,
    {}
  ).subscribe({
    next: () => {
      this.adminService.loadTasks();
      this.processingId = null;
    },
    error: (err) => {
      console.error('Decline Failed', err);
      this.processingId = null;
    }
  });
}

}
