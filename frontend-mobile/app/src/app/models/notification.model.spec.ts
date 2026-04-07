import { Notification } from './notification.model';

describe('Notification', () => {
  it('uses the read field when present', () => {
    const notification = new Notification({ read: true, userId: 5 });

    expect(notification.read).toBeTrue();
  });

  it('falls back to isRead when read is missing', () => {
    const notification = new Notification({ userId: 5, isRead: true } as any);

    expect(notification.read).toBeTrue();
  });

  it('prefers read over isRead when both are provided', () => {
    const notification = new Notification({ userId: 5, read: false, isRead: true } as any);

    expect(notification.read).toBeFalse();
  });
});
