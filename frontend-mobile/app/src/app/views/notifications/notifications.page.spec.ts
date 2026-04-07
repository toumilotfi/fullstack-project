import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationsPage } from './notifications.page';
import { AuthController } from '../../controllers/auth.controller';
import { NotificationController } from '../../controllers/notification.controller';

describe('NotificationsPage', () => {
  let page: NotificationsPage;
  let router: jasmine.SpyObj<Router>;
  let notifyCtrl: jasmine.SpyObj<NotificationController>;

  beforeEach(() => {
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    notifyCtrl = jasmine.createSpyObj<NotificationController>('NotificationController', ['loadNotifications', 'markRead'], {
      notifications: signal([]),
      unreadCount: signal(0)
    });

    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthController,
          useValue: {
            currentUser: signal(null)
          }
        },
        { provide: NotificationController, useValue: notifyCtrl },
        { provide: Router, useValue: router }
      ]
    });

    page = TestBed.runInInjectionContext(() => new NotificationsPage());
  });

  it('marks the notification as read and navigates home for task-related titles', () => {
    page.onNotificationClick({ id: 12, title: 'New Task Assigned', message: 'Mission ready.' });

    expect(notifyCtrl.markRead).toHaveBeenCalledWith(12);
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('marks the notification as read without navigating for unrelated titles', () => {
    page.onNotificationClick({ id: 13, title: 'System Update', message: 'No action required.' });

    expect(notifyCtrl.markRead).toHaveBeenCalledWith(13);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
