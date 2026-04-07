import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { TasksComponent } from './tasks';
import { AdminService } from '../services/admin.service';
import { Task } from '../models/admin.model';

describe('TasksComponent', () => {
  let fixture: ComponentFixture<TasksComponent>;
  let component: TasksComponent;
  let tasksSignal = signal<Task[]>([]);
  let usersSignal = signal([]);
  let adminService: {
    users: typeof usersSignal;
    tasks: typeof tasksSignal;
    loadUsersCalls: number;
    loadTasksCalls: number;
    loadUsers: () => void;
    loadTasks: () => void;
    deployTask: () => unknown;
    deleteTask: () => unknown;
  };

  beforeEach(async () => {
    tasksSignal = signal<Task[]>([]);
    usersSignal = signal([]);
    adminService = {
      users: usersSignal,
      tasks: tasksSignal,
      loadUsersCalls: 0,
      loadTasksCalls: 0,
      loadUsers: () => {
        adminService.loadUsersCalls += 1;
      },
      loadTasks: () => {
        adminService.loadTasksCalls += 1;
      },
      deployTask: () => of({}),
      deleteTask: () => of({})
    };

    await TestBed.configureTestingModule({
      imports: [TasksComponent],
      providers: [{ provide: AdminService, useValue: adminService }]
    }).compileComponents();

    fixture = TestBed.createComponent(TasksComponent);
    component = fixture.componentInstance;
  });

  it('marks approved tasks as done in the task list', () => {
    tasksSignal.set([
      new Task({ id: 1, title: 'Approved', assignedToUserId: 7, status: 'APPROVED' }),
      new Task({ id: 2, title: 'Assigned', assignedToUserId: 8, status: 'ASSIGNED' })
    ]);

    fixture.detectChanges();

    const doneIndicators = fixture.nativeElement.querySelectorAll('.status-indicator.done');

    expect(doneIndicators.length).toBe(1);
  });
});
