import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatMessage } from '../models/chat-message.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatApi {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/User`;

  getInbox(userId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.baseUrl}/messages/inbox/${userId}`);
  }

  sendMessageToAdmin(userId: number, content: string): Observable<ChatMessage> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('message', content);

    return this.http.post<ChatMessage>(
      `${this.baseUrl}/message/admin`,
      {},
      { params }
    );
  }
  getSentMessages(userId: number): Observable<ChatMessage[]> {
  return this.http.get<ChatMessage[]>(`${this.baseUrl}/messages/sent/${userId}`);
}

}
