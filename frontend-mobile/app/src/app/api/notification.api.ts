import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationApi {
  private http = inject(HttpClient);
  private baseUrl = 'http://192.168.178.44:8080/api/v1';

  getUserNotifications(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/User/not/${userId}`);
  }

  markAsRead(id: number): Observable<string> {
    return this.http.put(`${this.baseUrl}/User/read/${id}`, null, { responseType: 'text' });
  }
}