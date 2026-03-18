import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { AuthController } from '../../controllers/auth.controller';
import { ChatController } from '../../controllers/chat.controller';

@Component({
  selector: 'app-messaging',
  standalone: true,
  imports: [CommonModule, IonicModule, FormsModule],
  templateUrl: './messaging.page.html',
  styleUrls: ['./messaging.page.scss'],
})
export class MessagingPage implements OnInit, OnDestroy {
  public auth = inject(AuthController);
  public chatCtrl = inject(ChatController);

  newMessage: string = '';

  ngOnInit() {
    const user = this.auth.currentUser();
    if (user && user.id) {
      console.log("Opening secure channel for User:", user.id);
      this.chatCtrl.initChat(user.id); 
    } else {
      console.warn("No active user session found.");
    }
  }

  ngOnDestroy() {
    this.chatCtrl.closeChat(); 
  }

  sendMessage() {
    const user = this.auth.currentUser();
    
    if (!this.newMessage.trim() || !user || !user.id) return;

    this.chatCtrl.sendMessage(user.id, this.newMessage);
    this.newMessage = ''; 
  }
}