export interface Message {
  id: number;
  text: string;
  time: string;
  isMe: boolean; // Determines if bubble is left or right
  senderName?: string;
}