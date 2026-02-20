import { Component, OnInit, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { UiController } from '../../controllers/ui.controller';

@Component({
  selector: 'app-messaging',
  templateUrl: './messaging.page.html',
  styleUrls: ['./messaging.page.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule]
})
export class MessagingPage implements OnInit {
  ui = inject(UiController);
  newMsg = '';
  
  // Dummy data for the "Pro" look
  messages = [
    { id: 1, text: 'Hey Ahmed! Did you see the new glassmorphism UI?', time: '10:00 AM', isMe: false },
    { id: 2, text: 'Yeah, I just finished the dashboard. It looks insane! 🚀', time: '10:02 AM', isMe: true },
    { id: 3, text: 'Awesome. Send me the screenshots when you can.', time: '10:05 AM', isMe: false },
    { id: 4, text: 'Sure thing, uploading them now...', time: '10:06 AM', isMe: true }
  ];

  ngOnInit() {
    this.ui.setHasLayout(true); // Ensure the bottom nav shows up
  }

  sendMessage() {
    if (!this.newMsg.trim()) return;
    
    // Add the message with a smooth animation feel
    this.messages.push({
      id: Date.now(),
      text: this.newMsg,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isMe: true
    });
    this.newMsg = '';
  }
}