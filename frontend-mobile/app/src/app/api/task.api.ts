import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskApi {
  private http = inject(HttpClient);
  private baseUrl = 'http://172.20.10.2:8080/api/v1/Task';

  getAllTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.baseUrl}/tasks`);
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.baseUrl}/tasks/${id}`);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tasks/${id}`);
  }

  respondToTask(id: number, responseText: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/tasks/${id}/respond`, { response: responseText });
  }
}