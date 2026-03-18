import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms'; 
import { AuthController } from '../../controllers/auth.controller';
import { UserApi } from '../../api/user.api'; 

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, IonicModule, FormsModule], 
  templateUrl: './profile.page.html', 
  styleUrls: ['./profile.page.scss']
})
export class ProfilePage {
  public auth = inject(AuthController);
  private userApi = inject(UserApi);

  isEditOpen = false;

  editData = {
    firstName: '',
    lastName: '',
    secretPassword: ''
  };

  setOpen(isOpen: boolean) {
    this.isEditOpen = isOpen;
    const user = this.auth.currentUser();
    
    if (isOpen && user) {
      this.editData.firstName = user.firstName;
      this.editData.lastName = user.lastName || '';
      this.editData.secretPassword = ''; 
    }
  }

  saveChanges() {
    const user = this.auth.currentUser();

    if (!user || !user.id) {
      alert("Error: User session invalid.");
      return;
    }

    const updatedUser = { ...user, ...this.editData };

    this.userApi.updateProfile(user.id, updatedUser as any).subscribe({
      next: (savedUser: any) => {
        this.auth.currentUser.set(savedUser); 
        this.setOpen(false); 
        alert("Profile Updated Successfully!");
      },
      error: (err: any) => {
        console.error("Update failed:", err);
        alert("Failed to update profile. Check server connection.");
      }
    });
  }
}