import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { environment } from '../../environments/environment';
import { Task } from '../models/admin.model';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AdminService]
    });

    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('counts approved tasks as completed missions', () => {
    service.tasks.set([
      new Task({ id: 1, status: 'APPROVED' }),
      new Task({ id: 2, status: 'SUBMITTED' }),
      new Task({ id: 3, status: 'APPROVED' })
    ]);

    expect(service.completedTasks()).toBe(2);
  });

  it('logs an error when loading users fails', () => {
    const originalConsoleError = console.error;
    const consoleErrors: unknown[][] = [];
    console.error = ((...args: unknown[]) => {
      consoleErrors.push(args);
    }) as typeof console.error;
    try {
      service.loadUsers();
      const request = httpMock.expectOne(`${environment.apiUrl}/admin/users`);
      request.flush('boom', { status: 500, statusText: 'Server Error' });
      expect(consoleErrors.length).toBe(1);
      const [message, error] = consoleErrors[0] as [string, HttpErrorResponse];
      expect(message).toBe('Failed to load users');
      expect(error.status).toBe(500);
    } finally {
      console.error = originalConsoleError;
    }
  });

  it('logs an error when loading tasks fails', () => {
    const originalConsoleError = console.error;
    const consoleErrors: unknown[][] = [];
    console.error = ((...args: unknown[]) => {
      consoleErrors.push(args);
    }) as typeof console.error;
    try {
      service.loadTasks();
      const request = httpMock.expectOne(`${environment.apiUrl}/Task/tasks`);
      request.flush('boom', { status: 500, statusText: 'Server Error' });
      expect(consoleErrors.length).toBe(1);
      const [message, error] = consoleErrors[0] as [string, HttpErrorResponse];
      expect(message).toBe('Failed to load tasks');
      expect(error.status).toBe(500);
    } finally {
      console.error = originalConsoleError;
    }
  });

  it('sends global notifications with a message query param', () => {
    service.sendGlobalNotification('Broadcast').subscribe();

    const request = httpMock.expectOne((req) =>
      req.method === 'POST' && req.url === `${environment.apiUrl}/Not/notify/all`
    );

    expect(request.request.body).toEqual({});
    expect(request.request.params.get('message')).toBe('Broadcast');

    request.flush({});
  });

  it('sends direct notifications with a message query param', () => {
    service.sendDirectNotification(5, 'Directive').subscribe();

    const request = httpMock.expectOne((req) =>
      req.method === 'POST' && req.url === `${environment.apiUrl}/Not/notify/5`
    );

    expect(request.request.body).toEqual({});
    expect(request.request.params.get('message')).toBe('Directive');

    request.flush({});
  });
});
