import { Component, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { AdminService } from '../services/admin.service';
import { User } from '../models/admin.model';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, IonicModule],
  templateUrl: '../views/users/users.html',
  styleUrl: '../views/users/users.css',
  changeDetection: ChangeDetectionStrategy.OnPush
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
       this.adminService.http.put(`http://172.21.0.1:8080/api/v1/admin/users/${this.formData.id}`, this.formData)
        .subscribe({
          next: (res: any) => { this.adminService.loadUsers(); this.closeModal(); },
          error: (err: any) => console.error(err)
        });
    } else {
       this.adminService.http.post(`http://172.21.0.1:8080/api/v1/admin/users/new`, this.formData)
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