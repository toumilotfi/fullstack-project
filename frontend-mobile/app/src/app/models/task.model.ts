export interface Task {
completed: any;
  id?: number;
  title: string;
  description: string;

  status?: string;

  assignedToUserId?: number; // 🔥 FIX هنا

  userResponse?: string;
  responseAt?: string;

  tempResponse?: string;
}