import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TaskController } from './task.controller';
import { TaskApi } from '../api/task.api';

describe('TaskController', () => {
  let controller: TaskController;
  let taskApi: jasmine.SpyObj<TaskApi>;

  beforeEach(() => {
    taskApi = jasmine.createSpyObj<TaskApi>('TaskApi', [
      'declineTask',
      'deleteTask',
      'respondToTask',
      'getAllTasks'
    ]);

    TestBed.configureTestingModule({
      providers: [
        TaskController,
        { provide: TaskApi, useValue: taskApi }
      ]
    });

    controller = TestBed.inject(TaskController);
  });

  it('rejectTask calls the decline endpoint instead of deleting the task', () => {
    taskApi.declineTask.and.returnValue(of({ ok: true }));

    let result: any;
    controller.rejectTask(12).subscribe(value => result = value);

    expect(taskApi.declineTask).toHaveBeenCalledWith(12);
    expect(taskApi.deleteTask).not.toHaveBeenCalled();
    expect(result).toEqual({ ok: true });
  });
});
