export interface Task {
  id?: number;
  title: string;
  description: string;

  status?: string;

  assignedToUserId?: number; // 🔥 FIX هنا

  userResponse?: string;
  responseAt?: string;

  createdAt?: string;
  tempResponse?: string;
}
