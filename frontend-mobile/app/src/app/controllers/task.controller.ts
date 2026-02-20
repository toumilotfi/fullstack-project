import { Injectable, signal } from '@angular/core';

// 1. Define the Interface directly here or import it
export interface Task {
  id: number;
  title: string;
  category: string;
  priority: 'High' | 'Medium' | 'Low';
  progress: number;
  timeLeft: string;
}

@Injectable({ providedIn: 'root' })
export class TaskController {
  // 2. Use a Signal for Reactive Data (The 2026 Standard)
  public tasks = signal<Task[]>([
    {
      id: 1,
      title: 'Fix Approval Logic',
      category: 'Development',
      priority: 'High',
      progress: 75,
      timeLeft: '2h left'
    },
    {
      id: 2,
      title: 'Design Profile UI',
      category: 'Design',
      priority: 'Medium',
      progress: 30,
      timeLeft: '4h left'
    },
    {
      id: 3,
      title: 'Client Meeting',
      category: 'Marketing',
      priority: 'Low',
      progress: 100,
      timeLeft: 'Done'
    }
  ]);

  // Method to add a new task (Optional but good for MVC)
  addTask(t: Task) {
    this.tasks.update(values => [...values, t]);
  }
}