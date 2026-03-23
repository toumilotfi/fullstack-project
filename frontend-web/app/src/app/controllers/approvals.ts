import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service'; // ✅ One level up to services

@Component({
  selector: 'app-approvals',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: '../views/approvals/approvals.html', // ✅ Reaching into the subfolder
  styleUrl: '../views/approvals/approvals.css'
})
export class ApprovalsComponent implements OnInit {
  // Inject the service that connects to Lotfi's User/all
  public adminService = inject(AdminService);

  ngOnInit() {
    // Load users from GET /api/v1/admin/users
    this.adminService.loadUsers();
  }

  approve(id: number) {
    // Hits PUT /api/v1/admin/users/approve/{id}
    this.adminService.approveUser(id).subscribe({
      next: (res: any) => console.log('Operative Authorized'), // ✅ Explicit type
      error: (err: any) => console.error('Authorization Failed', err) // ✅ Explicit type
    });
  }

  reject(id: number) {
    // Hits DELETE /api/v1/admin/users/{id}
    if (confirm('TERMINATE APPLICATION AND PURGE RECORD?')) {
      this.adminService.deleteUser(id).subscribe({
        next: (res: any) => console.log('Record Purged'),
        error: (err: any) => console.error(err)
      });
    }
  }
}