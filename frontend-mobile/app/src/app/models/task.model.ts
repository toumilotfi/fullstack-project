export interface Task {
  id: number;
  title: string;
  category: string;     // e.g., 'Development' or 'Design'
  priority: 'High' | 'Medium' | 'Low';
  progress: number;     // 0 to 100
  timeLeft: string;
}