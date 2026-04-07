import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UserApi } from './user.api';
import { environment } from '../../environments/environment';

describe('UserApi', () => {
  let api: UserApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), UserApi]
    });

    api = TestBed.inject(UserApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('posts login credentials as JSON to the gateway auth endpoint', () => {
    const responseBody = {
      token: 'jwt-token',
      user: {
        id: 7,
        email: 'user@example.com',
        firstName: 'Lotfi',
        lastName: 'Toumi',
        role: 'USER',
        userActive: true
      }
    };

    api.login('user@example.com', 'secret').subscribe(response => {
      expect(response.token).toBe('jwt-token');
      expect(response.user.email).toBe('user@example.com');
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'user@example.com', password: 'secret' });
    request.flush(responseBody);
  });
});
