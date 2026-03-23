import { Injectable, inject, signal, effect } from '@angular/core';
import { forkJoin } from 'rxjs';
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
        this.messages.update(prev => [...prev, new ChatMessage(newMsg)]);
      }
    });
  }

  initChat(userId: number) {
    forkJoin([
      this.chatApi.getInbox(userId),
      this.chatApi.getSentMessages(userId)
    ]).subscribe({
      next: ([inbox, sent]) => {
        const all = [...inbox, ...sent];

        this.messages.set(
          all
            .map(m => new ChatMessage(m))
            .sort((a, b) =>
              new Date(a.createdAt || 0).getTime() -
              new Date(b.createdAt || 0).getTime()
            )
        );
      },
      error: (err) => console.error('Failed to load history', err)
    });

    this.wsService.connect(userId);
  }

  sendMessage(userId: number, content: string) {
    this.chatApi.sendMessageToAdmin(userId, content).subscribe({
      next: (sentMsg) => {
        this.messages.update(prev => [...prev, new ChatMessage(sentMsg)]);
      },
      error: (err) => console.error('Transmission failed', err)
    });
  }

  closeChat() {
    this.wsService.disconnect();
  }
}
