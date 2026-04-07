import { Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ChatMessage } from '../models/admin.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminWebSocketService {
  private stompClient: Client;
  public incomingMessage = signal<ChatMessage | null>(null);

  constructor() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(`${environment.wsUrl}`),
      reconnectDelay: 5000,
      debug: () => {},

      onConnect: () => {
        console.log('Connected to WebSocket Broker');

        this.stompClient.subscribe('/topic/admin', (msg) => {
          this.incomingMessage.set(JSON.parse(msg.body));
        });
      }
    });
  }

  connect() {
    if (!this.stompClient.active) {
      this.stompClient.activate();
    }
  }

  disconnect() {
    if (this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }
}
