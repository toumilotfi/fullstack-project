export class Task {
  id?: number;
  title: string;
  description: string;
  assignedToUserId: number;
  createdAt?: string;
  
  // STRICT API MATCH: Using booleans, not a status string
  completed: boolean; 
  approved: boolean;  
  
  userResponse?: string; 

  constructor(data: Partial<Task> = {}) {
    this.id = data.id;
    this.title = data.title || '';
    this.description = data.description || '';
    this.assignedToUserId = data.assignedToUserId || 0;
    this.createdAt = data.createdAt || new Date().toISOString();
    
    this.completed = data.completed || false;
    this.approved = data.approved || false;
    
    this.userResponse = data.userResponse || '';
  }
}