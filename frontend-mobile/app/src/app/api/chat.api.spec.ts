import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ChatApi } from './chat.api';
import { environment } from '../../environments/environment';

describe('ChatApi', () => {
  let api: ChatApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ChatApi]
    });

    api = TestBed.inject(ChatApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('sends userId and message without an adminId parameter', () => {
    api.sendMessageToAdmin(7, 'Hello admin').subscribe();

    const request = httpMock.expectOne((req) =>
      req.method === 'POST' && req.url === `${environment.apiUrl}/User/message/admin`
    );

    expect(request.request.params.get('userId')).toBe('7');
    expect(request.request.params.get('message')).toBe('Hello admin');
    expect(request.request.params.has('adminId')).toBeFalse();

    request.flush({
      senderId: 7,
      receiverId: 1,
      senderRole: 'USER',
      content: 'Hello admin',
      read: false
    });
  });
});
