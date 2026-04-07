import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service';
import { User } from '../models/admin.model';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule],
  templateUrl: '../views/users/users.html',
  styleUrl: '../views/users/users.css',
})
export class UsersComponent implements OnInit {
  public adminService = inject(AdminService);
  
  isModalOpen = false;
  isEditing = false;
  formData: Partial<User> = {};

  ngOnInit() {
     this.adminService.loadUsers();
  }

  openModal(user?: User) {
    if (user) {
      this.isEditing = true;
      this.formData = { ...user };
    } else {
      this.isEditing = false;
      this.formData = { userActive: false, role: 'AGENT' };
    }
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
    this.formData = {};
  }

  save() {
     if (this.isEditing && this.formData.id) {
       this.adminService.http.put(`${environment.apiUrl}/admin/users/${this.formData.id}`, this.formData)
        .subscribe({
          next: (res: any) => { this.adminService.loadUsers(); this.closeModal(); },
          error: (err: any) => console.error(err)
        });
    } else {
       this.adminService.http.post(`${environment.apiUrl}/admin/users/new`, this.formData)
        .subscribe({
          next: (res: any) => { this.adminService.loadUsers(); this.closeModal(); },
          error: (err: any) => console.error(err)
        });
    }
  }

  deleteUser(id: number) {
    if (confirm('TERMINATE AGENT ACCESS PERMANENTLY?')) {
      this.adminService.deleteUser(id).subscribe({
        next: (res: any) => console.log('Operative Purged'),
        error: (err: any) => console.error(err)
      });
    }
  }
}
