import { Injectable, signal } from '@angular/core';
import { Message } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatController {
  public messages = signal<Message[]>([
    { id: '1', senderId: 'admin', text: 'Hello Ahmed! How is the project going?', createdAt: new Date(Date.now() - 100000), isMe: false },
    { id: '2', senderId: 'me', text: 'Hi! It is going great, just finished the notifications.', createdAt: new Date(Date.now() - 50000), isMe: true },
  ]);

  sendMessage(text: string) {
    if (!text.trim()) return;
    
    const newMessage: Message = {
      id: Date.now().toString(),
      senderId: 'me',
      text: text,
      createdAt: new Date(),
      isMe: true
    };

    this.messages.update(msgs => [...msgs, newMessage]);
  }
}