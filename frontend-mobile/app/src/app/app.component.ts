import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { Router, RouterModule } from '@angular/router'; 
import { IonicModule } from '@ionic/angular'; 
import { UiController } from './controllers/ui.controller';
import { NotificationController } from './controllers/notification.controller';
import { addIcons } from 'ionicons';
import { 
  gridOutline, chatbubblesOutline, personOutline, notificationsOutline, 
  rocketOutline, checkmarkDoneOutline, timeOutline, calendarOutline, 
  settingsOutline, logOutOutline, chevronForwardOutline, send, add
} from 'ionicons/icons';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, IonicModule, RouterModule], 
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss']
})
export class AppComponent {
  public uiCtrl = inject(UiController);
  public notifyCtrl = inject(NotificationController);
  private router = inject(Router);
  public currTab = 'home';

  constructor() {
    // Register ALL icons for the app here
    addIcons({ gridOutline, chatbubblesOutline, personOutline, notificationsOutline, rocketOutline, checkmarkDoneOutline, timeOutline, calendarOutline, settingsOutline, logOutOutline, chevronForwardOutline, send, add });
  }

  nav(path: string) {
    if(path.includes('home')) this.currTab = 'home';
    if(path.includes('messaging')) this.currTab = 'chat';
    if(path.includes('profile')) this.currTab = 'profile';
    this.router.navigate([path]);
  }
}