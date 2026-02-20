import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router'; // Needed for the 'Back to Login' button
import { UiController } from '../../controllers/ui.controller';

@Component({
  selector: 'app-approval-pending',
  templateUrl: './approval-pending.page.html',
  styleUrls: ['./approval-pending.page.scss'],
  standalone: true,
  // We MUST import IonicModule and RouterModule here
  imports: [CommonModule, IonicModule, RouterModule]
})
export class ApprovalPendingPage implements OnInit {
  private ui = inject(UiController);

  ngOnInit() {
    // Hide the bottom navigation bar on this page (User isn't approved yet)
    this.ui.setHasLayout(false);
  }
}