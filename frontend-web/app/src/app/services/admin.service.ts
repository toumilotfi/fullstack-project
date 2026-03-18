import { Injectable, inject, signal, computed } from '@angular/core'; // ✅ Corrected from @angular/common/http
import { HttpClient } from '@angular/common/http';
import { User, Task, AppNotification } from '../models/admin.model';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  public http = inject(HttpClient);  
  private baseUrl = 'http://localhost:8080/api/v1';

   users = signal<User[]>([]);
  tasks = signal<Task[]>([]);

   totalUsers = computed(() => this.users().length);
  pendingApprovals = computed(() => this.users().filter((u: User) => !u.userActive).length);
  pendingUsers = computed(() => this.users().filter((u: User) => !u.userActive));
  completedTasks = computed(() => this.tasks().filter((t: Task) => t.completed).length);
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

   sendGlobalNotification(title: string, message: string) {
    return this.http.post(`${this.baseUrl}/Not/notify/all`, { title, message });
  }

  sendDirectNotification(userId: number, title: string, message: string) {
    return this.http.post(`${this.baseUrl}/Not/notify/${userId}`, { title, message });
  }
}