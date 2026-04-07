import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { NotificationController } from './notification.controller';
import { NotificationApi } from '../api/notification.api';

describe('NotificationController', () => {
  let controller: NotificationController;
  let notificationApi: jasmine.SpyObj<NotificationApi>;

  beforeEach(() => {
    notificationApi = jasmine.createSpyObj<NotificationApi>('NotificationApi', [
      'getUserNotifications',
      'markAsRead'
    ]);
    notificationApi.markAsRead.and.returnValue(of('ok'));

    TestBed.configureTestingModule({
      providers: [
        NotificationController,
        { provide: NotificationApi, useValue: notificationApi }
      ]
    });

    controller = TestBed.inject(NotificationController);
    controller.notifications.set([{
      id: 5,
      title: 'Alert',
      message: 'Read me',
      userId: 2,
      read: false,
      createdAt: '2026-04-05T10:00:00Z'
    }]);
    controller.unreadCount.set(1);
  });

  it('markRead updates the read field and unread count', () => {
    controller.markRead(5);

    expect(notificationApi.markAsRead).toHaveBeenCalledWith(5);
    expect(controller.notifications()[0].read).toBeTrue();
    expect(controller.unreadCount()).toBe(0);
  });
});
