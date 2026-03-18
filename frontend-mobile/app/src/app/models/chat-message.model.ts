export class ChatMessage {
  id?: number;
  senderId: number;
  receiverId: number;
  senderRole: string; 
  content: string;
  createdAt?: string;
  read: boolean;

  constructor(data: Partial<ChatMessage> = {}) {
    this.id = data.id;
    this.senderId = data.senderId || 0;
    this.receiverId = data.receiverId || 0;
    this.senderRole = data.senderRole || 'USER';
    this.content = data.content || '';
    this.createdAt = data.createdAt || new Date().toISOString();
    this.read = data.read || false;
  }
}