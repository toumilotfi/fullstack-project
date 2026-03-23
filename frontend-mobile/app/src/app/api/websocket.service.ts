import { Injectable, signal } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;

  // Signal used by ChatController
  incomingMessage = signal<ChatMessage | null>(null);

  connect(userId: number) {
    const socketFactory = () => new SockJS(environment.wsUrl);

    this.stompClient = new Client({
      webSocketFactory: socketFactory,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: () => {} // silence logs
    });

    this.stompClient.onConnect = () => {
      console.log('Connected to Secure WebSocket');

      // 🔥 1. Listen to ALL admin messages
      this.stompClient?.subscribe('/topic/admin', (message: Message) => {
        if (!message.body) return;

        const chatMsg: ChatMessage = JSON.parse(message.body);

        // Only messages intended for this user
        if (chatMsg.receiverId === userId) {
          this.incomingMessage.set(chatMsg);
        }
      });

      // 🔥 2. Optional: personal channel if backend supports it
      this.stompClient?.subscribe(`/topic/user/${userId}`, (message: Message) => {
        if (!message.body) return;

        const chatMsg: ChatMessage = JSON.parse(message.body);
        this.incomingMessage.set(chatMsg);
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker error:', frame.headers['message']);
      console.error('Details:', frame.body);
    };

    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log('WebSocket disconnected.');
    }
  }
}
