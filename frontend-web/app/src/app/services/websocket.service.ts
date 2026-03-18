import { Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ChatMessage } from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminWebSocketService {
  private stompClient: Client;
  public incomingMessage = signal<ChatMessage | null>(null);

  constructor() {
    this.stompClient = new Client({
       webSocketFactory: () => new SockJS('http://172.21.0.1:8080/chat'),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('Connected to Lotfi\'s RabbitMQ Broker');
         this.stompClient.subscribe('/topic/admin', (msg) => {
          this.incomingMessage.set(JSON.parse(msg.body));
        });
      }
    });
  }

  connect() { this.stompClient.activate(); }
  disconnect() { this.stompClient.deactivate(); }
}