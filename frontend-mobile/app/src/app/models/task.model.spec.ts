import { Task } from './task.model';

describe('Task model', () => {
  it('supports createdAt timestamps from the backend', () => {
    const task: Task = {
      title: 'Demo',
      description: 'Check createdAt support',
      createdAt: '2026-04-05T10:00:00Z'
    };

    expect(task.createdAt).toBe('2026-04-05T10:00:00Z');
  });

  it('rejects the legacy completed flag', () => {
    // @ts-expect-error completed is no longer part of the mobile Task model
    const task: Task = { title: 'Demo', description: 'Legacy field', completed: true };

    expect((task as any).completed).toBeTrue();
  });
});
