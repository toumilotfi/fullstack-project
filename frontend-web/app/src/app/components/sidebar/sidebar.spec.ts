import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { SidebarComponent } from './sidebar.component';
import { AdminWebSocketService } from '../../services/websocket.service';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let router: Router;
  let disconnectCount: number;
  let webSocket: Pick<AdminWebSocketService, 'disconnect'>;
  let navigateCalls: unknown[][];

  beforeEach(async () => {
    disconnectCount = 0;
    navigateCalls = [];
    webSocket = {
      disconnect: () => {
        disconnectCount += 1;
      }
    };

    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [
        provideRouter([]),
        { provide: AdminWebSocketService, useValue: webSocket }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    router.navigate = ((...args: unknown[]) => {
      navigateCalls.push(args);
      return Promise.resolve(true);
    }) as Router['navigate'];
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_user');
  });

  it('renders a single functional sign out button', () => {
    const buttons = fixture.nativeElement.querySelectorAll('.logout-btn');

    expect(buttons.length).toBe(1);

    buttons[0].click();

    expect(disconnectCount).toBe(1);
    expect(navigateCalls).toEqual([[['/login']]]);
  });

  it('signOut clears the stored admin session', () => {
    localStorage.setItem('admin_token', 'token');
    localStorage.setItem('admin_user', JSON.stringify({ id: 9 }));

    component.signOut();

    expect(localStorage.getItem('admin_token')).toBeNull();
    expect(localStorage.getItem('admin_user')).toBeNull();
  });
});
