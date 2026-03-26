import { Component, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service'; 
import { Task, User } from '../models/admin.model';  
@Component({
  selector: 'app-inspector',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: '../views/inspector/inspector.html',
  styleUrl: '../views/inspector/inspector.css',
  changeDetection: ChangeDetectionStrategy.OnPush
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
    t.status === 'SUBMITTED' || t.status === 'PENDING'
  );
}

acceptTask(task: Task) {
  if (!task.id) return;
  this.processingId = task.id;

  this.adminService.http.put(
    `http://localhost:8080/api/v1/Task/tasks/approve/${task.id}`,
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

  this.adminService.http.put(
    `http://localhost:8080/api/v1/Task/tasks/${task.id}/decline`,
    {}
  ).subscribe({
    next: () => this.adminService.loadTasks(),
    error: (err) => console.error(err)
  });
}

}