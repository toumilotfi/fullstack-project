import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TaskApi {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/Task`;

  getAllTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.baseUrl}/tasks`);
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.baseUrl}/tasks/${id}`);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tasks/${id}`);
  }
approveTask(id: number) {
  return this.http.put(`${this.baseUrl}/tasks/approve/${id}`, {});
}

respondToTask(id: number, responseText: string) {
  return this.http.put(
    `${this.baseUrl}/tasks/${id}/respond`,
    { response: responseText } // ✅ clean
  );
}
declineTask(id: number) {
  return this.http.put(`${this.baseUrl}/tasks/${id}/decline`, {});
}
}
