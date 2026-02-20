export interface Message {
  id: string;
  senderId: string;
  text: string;
  createdAt: Date;
  isMe: boolean; // Helper for UI alignment
}