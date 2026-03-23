import { Injectable, inject, signal, effect } from '@angular/core';
import { ChatApi } from '../api/chat.api';
import { WebSocketService } from '../api/websocket.service';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatController {
  private chatApi = inject(ChatApi);
  private wsService = inject(WebSocketService);

  messages = signal<ChatMessage[]>([]);

  constructor() {
    effect(() => {
      const newMsg = this.wsService.incomingMessage();
      if (newMsg) {
        this.messages.update(prev => [...prev, newMsg]);
      }
    });
  }
initChat(userId: number) {
  this.chatApi.getInbox(userId).subscribe({
    next: (history: ChatMessage[]) => this.messages.set(history),
    error: (err: any) => console.error('Failed to load history', err)
  });

  this.wsService.connect(userId);
}

  sendMessage(userId: number, content: string) {
    this.chatApi.sendMessageToAdmin(userId, content).subscribe({
      next: (sentMsg) => {
        this.messages.update(prev => [...prev, sentMsg]);
      },
      error: (err) => console.error('Transmission failed', err)
    });
  }

  closeChat() {
    this.wsService.disconnect();
  }
}