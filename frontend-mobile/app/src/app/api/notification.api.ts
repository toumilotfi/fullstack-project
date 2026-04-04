import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../models/notification.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationApi {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

getUserNotifications(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/Not/notifications/status/${userId}`);
  }

  markAsRead(id: number): Observable<string> {
    return this.http.put(
      `${this.baseUrl}/Not/read/${id}`,
      null,
      { responseType: 'text' }
    );
  }

}
