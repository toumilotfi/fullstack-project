import { Injectable, inject, signal } from '@angular/core';
import { TaskApi } from '../api/task.api';
import { Task } from '../models/task.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TaskController {
  private taskApi = inject(TaskApi);
  
  userTasks = signal<Task[]>([]);

  loadUserTasks(userId: number) {
    this.taskApi.getAllTasks().subscribe({
      next: (allTasks: Task[]) => {
        const myTasks = allTasks.filter(t => t.assignedToUserId === userId);
        this.userTasks.set(myTasks);
      },
      error: (err: any) => {
        console.error("Mission API Offline", err);
        this.userTasks.set([]); 
      }
    });
  }

  submitTaskResponse(taskId: number, reportText: string): Observable<any> {
    return this.taskApi.respondToTask(taskId, reportText);
  }

  rejectTask(taskId: number): Observable<void> {
    return this.taskApi.deleteTask(taskId);
  }
}