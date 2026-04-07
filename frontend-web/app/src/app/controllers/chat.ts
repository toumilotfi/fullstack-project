import { Component, inject, OnInit, effect, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { HttpParams } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { AdminService } from '../services/admin.service';
import { AdminWebSocketService } from '../services/websocket.service';
import { User, ChatMessage } from '../models/admin.model';
import { environment } from '../../environments/environment';

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

  @ViewChild('scrollArea') scrollArea!: ElementRef;

  constructor() {
    effect(() => {
      const msg = this.webSocket.incomingMessage();

      if (
        msg &&
        this.selectedUser &&
        (msg.senderId === this.selectedUser.id ||
         msg.receiverId === this.selectedUser.id)
      ) {
        this.chatHistory.push(new ChatMessage(msg));
        this.scrollToBottom();
      }
    });
  }

  ngOnInit() {
    this.adminService.loadUsers();
    this.webSocket.connect();
  }

  private scrollToBottom() {
    setTimeout(() => {
      if (this.scrollArea) {
        this.scrollArea.nativeElement.scrollTop =
          this.scrollArea.nativeElement.scrollHeight;
      }
    });
  }

  selectUser(user: User) {
    this.selectedUser = user;

    const inbox$ = this.adminService.http.get<ChatMessage[]>(
      `${environment.apiUrl}/admin/messages/inbox`
    );

    const sent$ = this.adminService.http.get<ChatMessage[]>(
      `${environment.apiUrl}/admin/messages`
    );

    forkJoin([inbox$, sent$]).subscribe({
      next: ([inbox, sent]) => {
        const all = [...inbox, ...sent];

        this.chatHistory = all
          .filter(m =>
            m.senderId === user.id || m.receiverId === user.id
          )
          .map(m => new ChatMessage(m))
          .sort(
            (a, b) =>
              new Date(a.createdAt).getTime() -
              new Date(b.createdAt).getTime()
          );

        this.scrollToBottom();
      },
      error: err => console.error('Comms Sync Failure:', err)
    });
  }

  private getAdminId(): number {
    try {
      const stored = localStorage.getItem('admin_user');
      if (stored) {
        const user = JSON.parse(stored);
        if (user?.id) return user.id;
      }
      const token = localStorage.getItem('admin_token');
      if (!token) return 0;
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId ?? payload.id ?? 0;
    } catch {
      return 0;
    }
  }

  sendMessage() {
    if (!this.messageText.trim() || !this.selectedUser) return;

    const adminId = this.getAdminId();
    if (!adminId) return;
    const userId = this.selectedUser.id!;

    const params = new HttpParams()
      .set('adminId', adminId)
      .set('userId', userId)
      .set('message', this.messageText);

    this.adminService.http
      .post<string>(
        `${environment.apiUrl}/admin/message/user`,
        {},
        { params }
      )
      .subscribe({
        next: () => {
          const newMsg = new ChatMessage({
            senderId: adminId,
            receiverId: userId,
            senderRole: 'ADMIN',
            content: this.messageText,
            read: false
          });

          this.chatHistory.push(newMsg);
          this.messageText = '';
          this.scrollToBottom();
        },
        error: err => console.error('Transmission Failure:', err)
      });
  }
}
