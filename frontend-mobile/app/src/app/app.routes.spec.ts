import { routes } from './app.routes';

describe('app routes', () => {
  it('protects authenticated mobile pages', () => {
    const protectedPaths = ['home', 'profile', 'messaging', 'notifications', 'tasks', 'tasks-card'];

    for (const path of protectedPaths) {
      const route = routes.find(candidate => candidate.path === path);
      expect(route?.canActivate?.length).withContext(path).toBe(1);
    }
  });

  it('loads the task card component from the task-card route', async () => {
    const route = routes.find(candidate => candidate.path === 'tasks-card');
    const component = await route!.loadComponent!();

    expect((component as any).name).toBe('TaskCardComponent');
  });
});
