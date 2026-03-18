import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserApi {
  private http = inject(HttpClient);
  private baseUrl = 'http://192.168.178.44:8080/api/v1';

  login(email: string, secretPassword: string): Observable<string> {
    const params = new HttpParams().set('email', email).set('password', secretPassword);
    return this.http.post(`${this.baseUrl}/auth/login`, null, { params, responseType: 'text' });
  }

  register(user: User): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/auth/register`, user);
  }

  updateProfile(id: number, user: User): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/auth/update/${id}`, user);
  }

  forgotPassword(email: string): Observable<string> {
    const params = new HttpParams().set('email', email);
    return this.http.post(`${this.baseUrl}/auth/forgot-password`, null, { params, responseType: 'text' });
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/User/logout`, {});
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/admin/users`);
  }
}