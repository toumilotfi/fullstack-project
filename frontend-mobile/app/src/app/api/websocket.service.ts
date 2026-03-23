import { Injectable, signal } from '@angular/core';
import { Client, Message, Stomp } from '@stomp/stompjs';
// 1. Change the import to this specific format
import SockJS from 'sockjs-client'; 

// ... inside the connect() method ...
const socketFactory = () => new SockJS(environment.wsUrl);import { environment } from '../../environments/environment';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;
  
  // Use a signal to push incoming messages to the ChatController
  incomingMessage = signal<ChatMessage | null>(null);

  connect(userId: number) {
    // 🚨 The property 'wsUrl' must exist in environment.ts
const socketFactory = () => new SockJS(environment.wsUrl);
    this.stompClient = new Client({
      webSocketFactory: socketFactory,
      debug: (str) => console.log('STOMP Debug:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected to Neural Link:', frame);
      
      // Subscribe to personal message channel
      // Matches Lotfi's destination pattern: /user/{userId}/queue/messages
      this.stompClient?.subscribe(`/user/${userId}/queue/messages`, (message: Message) => {
        if (message.body) {
          const chatMsg: ChatMessage = JSON.parse(message.body);
          this.incomingMessage.set(chatMsg);
        }
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log('Neural Link Disconnected.');
    }
  }
}