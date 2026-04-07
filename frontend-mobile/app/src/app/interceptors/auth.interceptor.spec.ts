import { HttpHandlerFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  afterEach(() => {
    localStorage.clear();
  });

  it('adds the bearer token from localStorage when present', () => {
    localStorage.setItem('auth_token', 'jwt-token');
    const request = new HttpRequest('GET', '/api/test');
    let forwardedRequest: HttpRequest<unknown> | undefined;

    const next: HttpHandlerFn = req => {
      forwardedRequest = req;
      return of(new HttpResponse({ status: 200 }));
    };

    authInterceptor(request, next).subscribe();

    expect(forwardedRequest?.headers.get('Authorization')).toBe('Bearer jwt-token');
  });

  it('forwards the original request when no token is present', () => {
    const request = new HttpRequest('GET', '/api/test');
    let forwardedRequest: HttpRequest<unknown> | undefined;

    const next: HttpHandlerFn = req => {
      forwardedRequest = req;
      return of(new HttpResponse({ status: 200 }));
    };

    authInterceptor(request, next).subscribe();

    expect(forwardedRequest?.headers.has('Authorization')).toBeFalse();
  });
});
