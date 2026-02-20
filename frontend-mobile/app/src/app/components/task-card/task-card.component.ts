import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { Task } from '../../models/task.model'; // Import your interface

@Component({
  selector: 'app-task-card',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: './task-card.component.html',
  styleUrls: ['./task-card.component.scss']
})
export class TaskCardComponent {
  @Input() task!: Task; // Input data from the parent page
}