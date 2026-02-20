export interface AppNotification {
  id: string;
  title: string;
  body: string;
  type: 'task' | 'message' | 'system';
  createdAt: Date;
  isRead: boolean;
}