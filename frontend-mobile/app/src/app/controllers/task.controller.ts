import { Injectable, signal } from '@angular/core';
import { Task } from '../models/task.model';

@Injectable({
  providedIn: 'root'
})
export class TaskController {
  // Signal to hold our tasks (Mock Database)
  public tasks = signal<Task[]>([
    {
      id: '1',
      title: 'Design System Update',
      description: 'Update the mobile UI kit to version 2.0',
      priority: 'High',
      status: 'In Progress',
      dueDate: '2026-02-25',
      progress: 65
    },
    {
      id: '2',
      title: 'Fix Login Bug',
      description: 'Users report issues with password reset links',
      priority: 'Medium',
      status: 'To Do',
      dueDate: '2026-02-22',
      progress: 0
    }
  ]);

  getTasks() {
    return this.tasks();
  }

  getCompletionStats() {
    const total = this.tasks().length;
    const completed = this.tasks().filter(t => t.status === 'Completed').length;
    return (completed / total) || 0;
  }
}