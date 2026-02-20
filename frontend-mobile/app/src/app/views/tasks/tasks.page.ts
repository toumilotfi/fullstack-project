import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { UiController } from '../../controllers/ui.controller';
import { TaskController } from '../../controllers/task.controller'; // Import the Controller

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.page.html',
  styleUrls: ['./tasks.page.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule]
})
export class TasksPage implements OnInit {
  // Inject the controllers
  public ui = inject(UiController);
  public taskCtrl = inject(TaskController);

  // Local variable for adding a new task (simple input)
  newTaskTitle = '';

  ngOnInit() {
    this.ui.setHasLayout(true); // Show the bottom bar
  }

  addNewTask() {
    if (!this.newTaskTitle.trim()) return;

    // Call the controller to add the task
    this.taskCtrl.addTask({
      id: Date.now(),
      title: this.newTaskTitle,
      category: 'General',
      priority: 'Medium',
      progress: 0,
      timeLeft: 'Just now'
    });

    this.newTaskTitle = ''; // Clear input
  }
}