import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthController } from './auth.controller';
import { UserApi } from '../api/user.api';
import { User } from '../models/user.model';

describe('AuthController', () => {
  let controller: AuthController;
  let userApi: jasmine.SpyObj<UserApi>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    userApi = jasmine.createSpyObj<UserApi>('UserApi', ['login', 'register', 'logout']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthController,
        { provide: UserApi, useValue: userApi },
        { provide: Router, useValue: router }
      ]
    });

    localStorage.clear();
    controller = TestBed.inject(AuthController);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('stores auth token and current user on successful login', () => {
    const user = new User({
      id: 3,
      email: 'user@example.com',
      firstName: 'Lotfi',
      lastName: 'Toumi',
      role: 'USER',
      userActive: true
    });

    userApi.login.and.returnValue(of({ token: 'jwt-token', user }));

    controller.login('user@example.com', 'secret');

    expect(localStorage.getItem('auth_token')).toBe('jwt-token');
    expect(controller.currentUser()).toEqual(user);
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('alerts instead of navigating when the account is inactive', () => {
    const user = new User({
      id: 4,
      email: 'pending@example.com',
      firstName: 'Pending',
      role: 'USER',
      userActive: false
    });
    spyOn(window, 'alert');
    userApi.login.and.returnValue(of({ token: 'jwt-token', user }));

    controller.login('pending@example.com', 'secret');

    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(window.alert).toHaveBeenCalledWith('ACCOUNT LOCKED: Awaiting Admin Approval.');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('alerts when login fails', () => {
    spyOn(window, 'alert');
    userApi.login.and.returnValue(throwError(() => new Error('boom')));

    controller.login('user@example.com', 'secret');

    expect(window.alert).toHaveBeenCalledWith('CONNECTION FAILED: Unable to reach the server. Please try again later.');
  });

  it('clears the stored token on logout', () => {
    localStorage.setItem('auth_token', 'jwt-token');
    controller.currentUser.set(new User({ id: 9, email: 'user@example.com', firstName: 'Lotfi', userActive: true }));
    userApi.logout.and.returnValue(of(void 0));

    controller.logout();

    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(controller.currentUser()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
