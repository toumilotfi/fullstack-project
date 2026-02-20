import { Injectable, signal } from '@angular/core';

export interface Message {
  id: number;
  text: string;
  time: string;
  isMe: boolean;
}

@Injectable({ providedIn: 'root' })
export class MessagingController {
  // Signal for messages
  public messages = signal<Message[]>([
    { id: 1, text: 'Hey Ahmed! Did you see the new UI?', time: '10:00 AM', isMe: false },
    { id: 2, text: 'Yeah, the Neon Glass look is fire! 🔥', time: '10:02 AM', isMe: true }
  ]);

  // Logic to send a message
  sendMessage(text: string) {
    const newMsg: Message = {
      id: Date.now(),
      text: text,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isMe: true
    };
    // Update the signal
    this.messages.update(msgs => [...msgs, newMsg]);
  }
}