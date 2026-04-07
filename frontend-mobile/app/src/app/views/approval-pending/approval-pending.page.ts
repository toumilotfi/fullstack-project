import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { UserApi } from '../../api/user.api';

@Component({
  selector: 'app-approval-pending',
  standalone: true,
  imports: [CommonModule, IonicModule, RouterModule],
  templateUrl: './approval-pending.page.html',
  styleUrls: ['./approval-pending.page.scss'],
})
export class ApprovalPendingPage implements OnInit {
  private userApi = inject(UserApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  
  registeredEmail: string = '';

  ngOnInit() {
    // Capture the email passed from the Register page
    this.route.queryParams.subscribe(params => {
      if (params['email']) {
        this.registeredEmail = params['email'];
      }
    });
  }

  // STRICT API MATCH: Checks if the Admin has flipped the userActive bit
  checkStatus() {
    if (!this.registeredEmail) {
      this.router.navigate(['/login']);
      return;
    }

    this.userApi.checkApprovalStatus(this.registeredEmail).subscribe({
      next: (result) => {
        if (result.active) {
          alert("ACCESS GRANTED: Your account has been approved. Proceed to login.");
          this.router.navigate(['/login']);
        } else {
          alert("STATUS: LOCKED. HQ is still reviewing your credentials.");
        }
      },
      error: (err: any) => {
        console.error("Network Error", err);
        alert("Cannot reach HQ Radar. Check server connection.");
      }
    });
  }
}