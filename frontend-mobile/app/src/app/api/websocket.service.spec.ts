import { TestBed } from '@angular/core/testing';
import { Client } from '@stomp/stompjs';
import { WebSocketService } from './websocket.service';

describe('WebSocketService', () => {
  let service: WebSocketService;
  let subscriptions: Record<string, (message: { body?: string }) => void>;

  beforeEach(() => {
    subscriptions = {};

    spyOn(Client.prototype, 'subscribe').and.callFake((destination: string, callback: any) => {
      subscriptions[destination] = callback;
      return { id: destination, unsubscribe: () => {} } as any;
    });

    spyOn(Client.prototype, 'activate').and.callFake(function (this: Client) {
      this.onConnect?.({} as any);
    });

    spyOn(Client.prototype, 'deactivate').and.returnValue(Promise.resolve());

    TestBed.configureTestingModule({
      providers: [WebSocketService]
    });

    service = TestBed.inject(WebSocketService);
  });

  it('subscribes to the shared user topic on connect', () => {
    service.connect(7);

    expect(subscriptions['/topic/user']).toEqual(jasmine.any(Function));
    expect(subscriptions['/topic/admin']).toBeUndefined();
    expect(subscriptions['/topic/user/7']).toBeUndefined();
  });

  it('delivers messages addressed to the connected user', () => {
    service.connect(7);

    subscriptions['/topic/user']({
      body: JSON.stringify({
        senderId: 1,
        receiverId: 7,
        senderRole: 'ADMIN',
        content: 'Private',
        read: false
      })
    });

    expect(service.incomingMessage()?.receiverId).toBe(7);
    expect(service.incomingMessage()?.content).toBe('Private');
  });

  it('ignores messages for other users or broadcast payloads', () => {
    service.connect(7);

    subscriptions['/topic/user']({
      body: JSON.stringify({
        senderId: 1,
        receiverId: 99,
        senderRole: 'ADMIN',
        content: 'Other User',
        read: false
      })
    });

    expect(service.incomingMessage()).toBeNull();

    subscriptions['/topic/user']({
      body: JSON.stringify({
        senderId: 1,
        receiverId: 0,
        senderRole: 'ADMIN',
        content: 'Broadcast',
        read: false
      })
    });

    expect(service.incomingMessage()).toBeNull();
  });
});
