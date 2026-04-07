import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LoginComponent } from './login';

describe('LoginComponent', () => {
  let router: Router;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    router = TestBed.inject(Router);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('stores admin auth state and navigates to the dashboard on success', () => {
    const navigateCalls: unknown[][] = [];
    const originalNavigate = router.navigate.bind(router);
    router.navigate = ((...args: unknown[]) => {
      navigateCalls.push(args);
      return Promise.resolve(true);
    }) as typeof router.navigate;

    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;

    try {
      component.email = 'admin@example.com';
      component.password = 'secret';
      component.login();

      const req = httpMock.expectOne('/api/v1/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'admin@example.com', password: 'secret' });

      req.flush({
        token: 'jwt-token',
        user: { id: 1, role: 'ADMIN', email: 'admin@example.com' }
      });

      expect(localStorage.getItem('admin_token')).toBe('jwt-token');
      expect(localStorage.getItem('admin_user')).toContain('"role":"ADMIN"');
      expect(navigateCalls).toEqual([[['/admin/dashboard']]]);
    } finally {
      router.navigate = originalNavigate;
    }
  });

  it('blocks non-admin users from entering the dashboard', () => {
    const navigateCalls: unknown[][] = [];
    const originalNavigate = router.navigate.bind(router);
    router.navigate = ((...args: unknown[]) => {
      navigateCalls.push(args);
      return Promise.resolve(true);
    }) as typeof router.navigate;

    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;

    try {
      component.email = 'user@example.com';
      component.password = 'secret';
      component.login();

      const req = httpMock.expectOne('/api/v1/auth/login');
      req.flush({
        token: 'jwt-token',
        user: { id: 2, role: 'USER', email: 'user@example.com' }
      });

      expect(component.errorMsg).toBe('ACCESS DENIED: Administrator credentials required.');
      expect(component.loading).toBe(false);
      expect(localStorage.getItem('admin_token')).toBeNull();
      expect(navigateCalls).toEqual([]);
    } finally {
      router.navigate = originalNavigate;
    }
  });
});
