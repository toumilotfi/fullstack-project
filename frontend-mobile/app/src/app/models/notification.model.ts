export class Notification {
  id?: number;
  title: string;
  message: string;
  userId: number;
  read: boolean;
  createdAt?: string;

  constructor(data: Partial<Notification> = {}) {
    this.id = data.id;
    this.title = data.title || 'System Alert';
    this.message = data.message || '';
    this.userId = data.userId || 0;
    this.read = data.read ?? false;
    this.createdAt = data.createdAt || new Date().toISOString();
  }
}
