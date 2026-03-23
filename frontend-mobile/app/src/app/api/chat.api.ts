import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatApi {
  private http = inject(HttpClient);
  private baseUrl = 'http://172.20.10.2:8080/api/v1/User';

  getInbox(userId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.baseUrl}/messages/inbox/${userId}`);
  }

  sendMessageToAdmin(userId: number, content: string): Observable<ChatMessage> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('adminId', 1)
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
