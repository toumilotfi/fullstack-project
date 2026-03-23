import { Component, inject, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service';
import { AdminWebSocketService } from '../services/websocket.service';
import { User, ChatMessage } from '../models/admin.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule],
  templateUrl: '../views/chat/chat.html',
  styleUrl: '../views/chat/chat.css'
})
export class ChatComponent implements OnInit {
  public adminService = inject(AdminService);
  public webSocket = inject(AdminWebSocketService);

  selectedUser: User | null = null;
  messageText: string = '';
  chatHistory: ChatMessage[] = [];

  constructor() {
    effect(() => {
      const msg = this.webSocket.incomingMessage();
      if (msg && this.selectedUser && (msg.senderId === this.selectedUser.id)) {
        this.chatHistory.push(msg);
      }
    });
  }

  ngOnInit() {
     this.adminService.loadUsers();
     this.webSocket.connect();
  }

   selectUser(user: User) {
    this.selectedUser = user;
     this.adminService.http.get<ChatMessage[]>(`http://localhost:8080/api/v1/admin/messages/inbox`).subscribe({
      next: (msgs: ChatMessage[]) => {
         this.chatHistory = msgs.filter((m: ChatMessage) => 
          m.senderId === user.id || m.receiverId === user.id
        );
      },
      error: (err: any) => console.error('Comms Sync Failure:', err)
    });
  }

   sendMessage() {
    if (!this.messageText.trim() || !this.selectedUser) return;

    const msg: Partial<ChatMessage> = {
      receiverId: this.selectedUser.id,
      content: this.messageText,
      senderRole: 'ADMIN'  
    };

     this.adminService.http.post<ChatMessage>(`http://localhost:8080/api/v1/admin/message/user`, msg).subscribe({
      next: (res: ChatMessage) => {
        this.chatHistory.push(res);
        this.messageText = ''; // Clear input field in template
      },
      error: (err: any) => console.error('Transmission Failure:', err)
    });
  }
}