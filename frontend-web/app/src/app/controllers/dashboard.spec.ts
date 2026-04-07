import { ComponentFixture, TestBed } from '@angular/core/testing';
import { computed, signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard';
import { AdminService } from '../services/admin.service';
import { Task, User } from '../models/admin.model';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let component: DashboardComponent;
  let notificationsResponse = of<any[]>([]);
  let globalNotificationResponse = of({});
  let tasksSignal = signal<Task[]>([]);
  let usersSignal = signal<User[]>([]);
  let adminService: {
      loadUsersCalls: number;
      loadTasksCalls: number;
      sendDirectNotificationCalls: unknown[][];
      loadUsers: () => void;
      loadTasks: () => void;
      getUserNotifications: () => unknown;
      sendGlobalNotification: () => unknown;
      sendDirectNotification: (...args: unknown[]) => unknown;
      tasks: typeof tasksSignal;
      users: typeof usersSignal;
      totalUsers: ReturnType<typeof computed<number>>;
      pendingApprovals: ReturnType<typeof computed<number>>;
      completedTasks: ReturnType<typeof computed<number>>;
      completionRate: ReturnType<typeof computed<number>>;
  };

  beforeEach(async () => {
    notificationsResponse = of([]);
    globalNotificationResponse = of({});
    tasksSignal = signal<Task[]>([]);
    usersSignal = signal<User[]>([]);
    adminService = {
      loadUsersCalls: 0,
      loadTasksCalls: 0,
      sendDirectNotificationCalls: [],
      loadUsers: () => {
        adminService.loadUsersCalls += 1;
      },
      loadTasks: () => {
        adminService.loadTasksCalls += 1;
      },
      getUserNotifications: () => notificationsResponse,
      sendGlobalNotification: () => globalNotificationResponse,
      sendDirectNotification: (...args: unknown[]) => {
        adminService.sendDirectNotificationCalls.push(args);
        return of({});
      },
      tasks: tasksSignal,
      users: usersSignal,
      totalUsers: computed(() => usersSignal().length),
      pendingApprovals: computed(() => usersSignal().filter((user) => !user.userActive).length),
      completedTasks: computed(() => tasksSignal().filter((task) => task.status === 'APPROVED').length),
      completionRate: computed(() => {
        if (tasksSignal().length === 0) {
          return 0;
        }
        return Math.round((tasksSignal().filter((task) => task.status === 'APPROVED').length / tasksSignal().length) * 100);
      })
    };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [{ provide: AdminService, useValue: adminService }]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  beforeEach(() => {
    localStorage.setItem('admin_user', JSON.stringify({ id: 1 }));
  });

  afterEach(() => {
    localStorage.removeItem('admin_user');
    localStorage.removeItem('admin_token');
  });

  it('counts assigned and submitted tasks as pending missions in the template', () => {
    tasksSignal.set([
      new Task({ id: 1, status: 'ASSIGNED' }),
      new Task({ id: 2, status: 'SUBMITTED' }),
      new Task({ id: 3, status: 'APPROVED' })
    ]);

    fixture.detectChanges();

    const cards = Array.from<Element>(fixture.nativeElement.querySelectorAll('.stat-card'))
      .map((card) => card.textContent?.replace(/\s+/g, ' ').trim());

    expect(cards).toContain('Pending Missions 2');
  });

  it('maps notification read state from read when isRead is missing', () => {
    notificationsResponse = of([{
      id: 9,
      title: 'Alert',
      message: 'Watch this',
      userId: 1,
      read: true,
      createdAt: '2026-04-05T10:00:00Z'
    }]);

    component.loadAllNotifications();

    expect(component.sentAlerts.length).toBe(1);
    expect(component.sentAlerts[0].isRead).toBe(true);
  });

  it('logs an error when loading notifications fails', () => {
    const error = new Error('notifications offline');
    const originalConsoleError = console.error;
    const consoleErrors: unknown[][] = [];
    console.error = ((...args: unknown[]) => {
      consoleErrors.push(args);
    }) as typeof console.error;
    notificationsResponse = throwError(() => error);
    try {
      component.loadAllNotifications();
      expect(consoleErrors).toEqual([['Failed to load notifications', error]]);
    } finally {
      console.error = originalConsoleError;
    }
  });

  it('logs an error when sending a global alert fails', () => {
    const error = new Error('send failed');
    const originalConsoleError = console.error;
    const consoleErrors: unknown[][] = [];
    console.error = ((...args: unknown[]) => {
      consoleErrors.push(args);
    }) as typeof console.error;
    globalNotificationResponse = throwError(() => error);
    component.broadcastMessage = 'Broadcast';
    try {
      component.sendGlobalAlert();
      expect(consoleErrors).toEqual([['Failed to send global alert', error]]);
    } finally {
      console.error = originalConsoleError;
    }
  });
});
