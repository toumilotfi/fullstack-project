import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TaskApi } from './task.api';
import { environment } from '../../environments/environment';

describe('TaskApi', () => {
  let api: TaskApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), TaskApi]
    });

    api = TestBed.inject(TaskApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('sends task responses as a JSON string body', () => {
    api.respondToTask(11, 'Mission update').subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/Task/tasks/11/respond`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toBe('"Mission update"');
    expect(request.request.headers.get('Content-Type')).toBe('application/json');

    request.flush({});
  });

  it('declines tasks through the decline endpoint', () => {
    api.declineTask(12).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/Task/tasks/12/decline`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({});

    request.flush({});
  });
});
