import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { User, Task, AppNotification } from '../models/admin.model';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  public http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

   users = signal<User[]>([]);
  tasks = signal<Task[]>([]);

   totalUsers = computed(() => this.users().length);
  pendingApprovals = computed(() => this.users().filter((u: User) => !u.userActive).length);
  pendingUsers = computed(() => this.users().filter((u: User) => !u.userActive));
  completedTasks = computed(() => this.tasks().filter((t: Task) => t.status === 'APPROVED').length);
  completionRate = computed(() => {
    if (this.tasks().length === 0) return 0;
    return Math.round((this.completedTasks() / this.tasks().length) * 100);
  });

   loadUsers() {
    return this.http.get<User[]>(`${this.baseUrl}/admin/users`).subscribe((data: User[]) => {
      this.users.set(data);
    });
  }

  approveUser(id: number) {
    return this.http.put(`${this.baseUrl}/admin/users/approve/${id}`, {}).pipe(
      tap(() => this.loadUsers())
    );
  }

  deleteUser(id: number) {
    return this.http.delete(`${this.baseUrl}/admin/users/${id}`).pipe(
      tap(() => this.loadUsers())
    );
  }
deleteTask(id: number) {
  return this.http.delete(`${this.baseUrl}/Task/tasks/${id}`).pipe(
    tap(() => this.loadTasks())
  );
}

   loadTasks() {
    return this.http.get<Task[]>(`${this.baseUrl}/Task/tasks`).subscribe((data: Task[]) => {
      this.tasks.set(data);
    });
  }

  deployTask(task: Partial<Task>) {
    return this.http.post(`${this.baseUrl}/Task/tasks`, task).pipe(
      tap(() => this.loadTasks())
    );
  }

sendGlobalNotification(message: string) {
  const params = new HttpParams().set('message', message);
  return this.http.post(`${this.baseUrl}/Not/notify/all`, {}, { params });
}

sendDirectNotification(userId: number, message: string) {
  const params = new HttpParams().set('message', message);
  return this.http.post(`${this.baseUrl}/Not/notify/${userId}`, {}, { params });
}
getUserNotifications(userId: number) {
  return this.http.get<any[]>(`${this.baseUrl}/Not/notifications/status/${userId}`);
}


  declineTask(id: number) {
  return this.http.put(`${this.baseUrl}/Task/tasks/${id}/decline`, {});
}

}