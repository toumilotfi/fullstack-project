import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { TaskController } from '../../controllers/task.controller';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.page.html',
  styleUrls: ['./tasks.page.scss'],
  standalone: true,
  imports: [CommonModule, IonicModule]
})
export class TasksPage {
  public taskCtrl = inject(TaskController);
  
  // State for filtering
  public filter = signal<'All' | 'To Do' | 'In Progress' | 'Completed'>('All');

  // Computed signal to filter tasks automatically when the filter changes
  public filteredTasks = computed(() => {
    const allTasks = this.taskCtrl.tasks();
    if (this.filter() === 'All') return allTasks;
    return allTasks.filter(t => t.status === this.filter());
  });

  segmentChanged(event: any) {
    this.filter.set(event.detail.value);
  }
}