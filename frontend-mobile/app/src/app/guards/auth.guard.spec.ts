import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    router = jasmine.createSpyObj<Router>('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: router }
      ]
    });

    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('allows navigation when an auth token is present in localStorage', () => {
    localStorage.setItem('auth_token', 'jwt-token');

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(result).toBeTrue();
  });

  it('redirects guests to login', () => {
    const loginTree = {} as UrlTree;
    router.createUrlTree.and.returnValue(loginTree);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
    expect(result).toBe(loginTree);
  });
});
