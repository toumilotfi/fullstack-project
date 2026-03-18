import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatApi {
  private http = inject(HttpClient);
  private baseUrl = 'http://192.168.178.44:8080/api/v1/User';

  getInbox(userId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.baseUrl}/messages/inbox/${userId}`);
  }

  sendMessageToAdmin(userId: number, content: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.baseUrl}/message/admin?userId=${userId}`, content);
  }
}