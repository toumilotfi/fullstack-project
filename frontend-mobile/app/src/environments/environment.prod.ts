const runtimeConfig = globalThis.__appConfig ?? {};

export const environment = {
  production: true,
  apiUrl: runtimeConfig.apiUrl ?? 'http://localhost:8080/api/v1',
  wsUrl: runtimeConfig.wsUrl ?? 'http://localhost:8080/chat'
};
