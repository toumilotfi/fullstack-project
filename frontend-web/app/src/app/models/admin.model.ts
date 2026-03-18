export class User {
  id?: number;
  email: string;
  firstName: string;
  lastName: string;
  secretPassword?: string;
  userActive: boolean;
  role?: string;
  createdAt?: string;

  constructor(data: Partial<User> = {}) {
    this.id = data.id;
    this.email = data.email || '';
    this.firstName = data.firstName || '';
    this.lastName = data.lastName || '';
    this.secretPassword = data.secretPassword;
    this.userActive = data.userActive ?? false;
    this.role = data.role || 'USER';
    this.createdAt = data.createdAt;
  }
}

 export class Task {
  id?: number;
  title: string;
  description: string;
  assignedToUserId: number;
  completed: boolean;
   response?: string; 
  createdAt?: string;

  constructor(data: Partial<Task> = {}) {
    this.id = data.id;
    this.title = data.title || '';
    this.description = data.description || '';
    this.assignedToUserId = data.assignedToUserId || 0;
    this.completed = data.completed ?? false;
    this.response = data.response || '';  
    this.createdAt = data.createdAt;
  }
}

export class ChatMessage {
  id?: number;
  senderId: number;
  receiverId: number;
  senderRole: string;
  content: string;
  createdAt: string;
  read: boolean;

  constructor(data: Partial<ChatMessage> = {}) {
    this.id = data.id;
    this.senderId = data.senderId || 0;
    this.receiverId = data.receiverId || 0;
    this.senderRole = data.senderRole || 'USER';
    this.content = data.content || '';
    this.createdAt = data.createdAt || new Date().toISOString();
    this.read = data.read ?? false;
  }
}

export class AppNotification {
  id?: number;
  title: string;
  message: string;
  userId: number;
  isRead: boolean;
  createdAt?: string;

  constructor(data: Partial<AppNotification> = {}) {
    this.id = data.id;
    this.title = data.title || 'SYSTEM ALERT';
    this.message = data.message || '';
    this.userId = data.userId || 0;
    this.isRead = data.isRead ?? false;
    this.createdAt = data.createdAt;
  }
}