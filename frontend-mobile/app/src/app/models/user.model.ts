export class User {
  id?: number;
  email: string;
  firstName: string;
  lastName?: string;
  secretPassword?: string;
  userActive: boolean;
  createdAt?: string;

  constructor(data: Partial<User> = {}) {
    this.id = data.id;
    this.email = data.email || '';
    this.firstName = data.firstName || '';
    this.lastName = data.lastName || '';
    this.secretPassword = data.secretPassword || '';
    this.userActive = data.userActive ?? false;
    this.createdAt = data.createdAt || new Date().toISOString();
  }
}