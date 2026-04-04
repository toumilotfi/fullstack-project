import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';

export interface AuthResponse {
  token: string;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class UserApi {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, { email, password });
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
