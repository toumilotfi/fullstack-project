import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service';  
import { Task } from '../models/admin.model';  

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule],
  templateUrl: '../views/tasks/tasks.html',
  styleUrl: '../views/tasks/tasks.css'
})
export class TasksComponent implements OnInit {
  public adminService = inject(AdminService);
  
   newTask: Partial<Task> = {
    title: '',
    description: '',
    assignedToUserId: 0,
    completed: false
  };

  isDeploying = false;

  ngOnInit() {
     this.adminService.loadUsers();
    this.adminService.loadTasks();
  }

  deployMission() {
    if (!this.newTask.title || !this.newTask.assignedToUserId) return;
    
    this.isDeploying = true;
     this.adminService.deployTask(this.newTask).subscribe({
      next: (res: any) => {  
        this.isDeploying = false;
        this.resetForm();
        console.log('Mission Transmitted');
      },
      error: (err: any) => {  
        this.isDeploying = false;
        console.error('Transmission Failure', err);
      }
    });
  }

  resetForm() {
    this.newTask = { title: '', description: '', assignedToUserId: 0, completed: false };
  }


  deleteTask(id: number) {
  this.adminService.deleteTask(id).subscribe({
    next: () => this.adminService.loadTasks(),
    error: (err) => console.error(err)
  });
}


}