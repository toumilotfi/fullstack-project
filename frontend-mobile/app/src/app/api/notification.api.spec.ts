import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { NotificationApi } from './notification.api';
import { environment } from '../../environments/environment';

describe('NotificationApi', () => {
  let api: NotificationApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), NotificationApi]
    });

    api = TestBed.inject(NotificationApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads user notifications from the gateway path', () => {
    api.getUserNotifications(7).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/Not/notifications/status/7`);
    expect(request.request.method).toBe('GET');

    request.flush([]);
  });

  it('marks notifications as read with a text response', () => {
    api.markAsRead(5).subscribe(response => {
      expect(response).toBe('ok');
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/Not/read/5`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.responseType).toBe('text');

    request.flush('ok');
  });
});
