import { TestBed } from '@angular/core/testing';
import { signal, WritableSignal } from '@angular/core';
import { of } from 'rxjs';
import { ToastController } from '@ionic/angular';
import { HomePage } from './home.page';
import { AuthController } from '../../controllers/auth.controller';
import { TaskController } from '../../controllers/task.controller';
import { NotificationController } from '../../controllers/notification.controller';
import { ChatController } from '../../controllers/chat.controller';
import { Task } from '../../models/task.model';

describe('HomePage', () => {
  let page: HomePage;
  let userTasksSignal: WritableSignal<Task[]>;

  beforeEach(() => {
    userTasksSignal = signal<Task[]>([]);

    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthController,
          useValue: {
            currentUser: signal(null)
          }
        },
        {
          provide: TaskController,
          useValue: {
            userTasks: userTasksSignal,
            loadUserTasks: jasmine.createSpy('loadUserTasks'),
            rejectTask: jasmine.createSpy('rejectTask').and.returnValue(of(void 0)),
            submitTaskResponse: jasmine.createSpy('submitTaskResponse').and.returnValue(of(void 0))
          }
        },
        {
          provide: NotificationController,
          useValue: {
            loadNotifications: jasmine.createSpy('loadNotifications')
          }
        },
        {
          provide: ChatController,
          useValue: {}
        },
        {
          provide: ToastController,
          useValue: {
            create: jasmine.createSpy('create').and.resolveTo({
              present: jasmine.createSpy('present').and.resolveTo()
            })
          }
        }
      ]
    });

    page = TestBed.runInInjectionContext(() => new HomePage());
  });

  it('keeps only active tasks in the reactive dashboard list', () => {
    userTasksSignal.set([
      { title: 'Assigned', description: '', status: 'ASSIGNED' },
      { title: 'Submitted', description: '', status: 'SUBMITTED' },
      { title: 'Revision', description: '', status: 'REVISION_REQUESTED' },
      { title: 'Approved', description: '', status: 'APPROVED' },
      { title: 'Declined', description: '', status: 'DECLINED' }
    ]);

    TestBed.flushEffects();

    expect(page.activeTasks.map((task) => task.status)).toEqual([
      'ASSIGNED',
      'SUBMITTED',
      'REVISION_REQUESTED'
    ]);
  });

  it('filters approved and declined tasks in updateActiveTasks', () => {
    userTasksSignal.set([
      { title: 'Assigned', description: '', status: 'ASSIGNED' },
      { title: 'Approved', description: '', status: 'APPROVED' },
      { title: 'Declined', description: '', status: 'DECLINED' }
    ]);

    (page as any).updateActiveTasks();

    expect(page.activeTasks.map((task) => task.status)).toEqual(['ASSIGNED']);
  });

  it('returns distinct colors for each mission status', () => {
    expect(page.getStatusColor('SUBMITTED')).toBe('warning');
    expect(page.getStatusColor('ASSIGNED')).toBe('primary');
    expect(page.getStatusColor('APPROVED')).toBe('success');
    expect(page.getStatusColor('DECLINED')).toBe('danger');
    expect(page.getStatusColor('REVISION_REQUESTED')).toBe('tertiary');
    expect(page.getStatusColor('UNKNOWN')).toBe('medium');
  });
});
