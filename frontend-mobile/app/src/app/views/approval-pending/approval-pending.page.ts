import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { UserApi } from '../../api/user.api'; 
import { User } from '../../models/user.model';

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

    // Hits GET /api/v1/admin/users to find the current user's state
    this.userApi.getAllUsers().subscribe({
      next: (users: User[]) => {
        const myUser = users.find(u => u.email === this.registeredEmail);
        
        if (myUser && myUser.userActive) {
          alert("ACCESS GRANTED: Lotfi has approved your account. Proceed to login.");
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