import { Task } from './admin.model';

describe('Task model', () => {
  it('defaults the status to ASSIGNED', () => {
    const task = new Task();

    expect(task.status).toBe('ASSIGNED');
  });
});
