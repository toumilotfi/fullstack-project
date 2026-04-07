import { environment } from './environment';

describe('environment', () => {
  it('uses the api proxy path in development', () => {
    expect(environment.apiUrl).toBe('/api/v1');
  });

  it('uses the chat websocket path in development', () => {
    expect(environment.wsUrl).toBe('/chat');
  });
});
