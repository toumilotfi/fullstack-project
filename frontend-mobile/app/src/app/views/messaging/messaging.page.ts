import { Component, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule, IonContent } from '@ionic/angular';
import { ChatController } from '../../controllers/chat.controller';

@Component({
  selector: 'app-messaging',
  templateUrl: './messaging.page.html',
  styleUrls: ['./messaging.page.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule]
})
export class MessagingPage {
  @ViewChild(IonContent) content!: IonContent;
  public chatCtrl = inject(ChatController);
  public newMessage = '';

  send() {
    if(this.newMessage.trim()){
      this.chatCtrl.sendMessage(this.newMessage);
      this.newMessage = '';
      setTimeout(() => this.content.scrollToBottom(300), 100);
    }
  }
}