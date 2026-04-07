import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { InspectorComponent } from './inspector';
import { AdminService } from '../services/admin.service';

describe('InspectorComponent', () => {
  let fixture: ComponentFixture<InspectorComponent>;
  let component: InspectorComponent;
  let adminService: {
    loadTasksCalls: number;
    loadUsersCalls: number;
    loadTasks: () => void;
    loadUsers: () => void;
    tasks: () => unknown[];
    users: () => unknown[];
    http: { put: (...args: unknown[]) => unknown };
  };
  let putResponse$: Subject<unknown> | null;
  let putCalls: unknown[][];
  let tasksResponse: unknown[];

  beforeEach(async () => {
    putResponse$ = null;
    putCalls = [];
    tasksResponse = [];
    adminService = {
      loadTasksCalls: 0,
      loadUsersCalls: 0,
      loadTasks: () => {
        adminService.loadTasksCalls += 1;
      },
      loadUsers: () => {
        adminService.loadUsersCalls += 1;
      },
      tasks: () => tasksResponse,
      users: () => [],
      http: {
        put: (...args: unknown[]) => {
          putCalls.push(args);
          if (!putResponse$) {
            throw new Error('putResponse$ must be configured in the test');
          }
          return putResponse$.asObservable();
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [InspectorComponent],
      providers: [{ provide: AdminService, useValue: adminService }]
    }).compileComponents();

    fixture = TestBed.createComponent(InspectorComponent);
    component = fixture.componentInstance;
  });

  it('returns only submitted tasks from getPendingTasks', () => {
    tasksResponse = [
      { id: 1, status: 'SUBMITTED' },
      { id: 2, status: 'PENDING' },
      { id: 3, status: 'ASSIGNED' }
    ];

    expect(component.getPendingTasks()).toEqual([{ id: 1, status: 'SUBMITTED' }]);
  });

  it('clears processingId after a successful reject', () => {
    putResponse$ = new Subject<unknown>();

    component.rejectTask({ id: 7 } as any);

    expect(component.processingId).toBe(7);

    putResponse$.next({});
    putResponse$.complete();

    expect(adminService.loadTasksCalls).toBe(1);
    expect(component.processingId).toBeNull();
  });

  it('clears processingId and logs when reject fails', () => {
    const error = new Error('backend down');
    const originalConsoleError = console.error;
    const consoleErrors: unknown[][] = [];
    console.error = ((...args: unknown[]) => {
      consoleErrors.push(args);
    }) as typeof console.error;
    putResponse$ = new Subject<unknown>();
    try {
      component.rejectTask({ id: 8 } as any);
      expect(component.processingId).toBe(8);
      putResponse$.error(error);
      expect(consoleErrors).toEqual([['Decline Failed', error]]);
      expect(component.processingId).toBeNull();
    } finally {
      console.error = originalConsoleError;
    }
  });

  it('ignores reject requests for tasks without ids', () => {
    component.rejectTask({} as any);

    expect(putCalls).toEqual([]);
    expect(component.processingId).toBeNull();
  });
});
