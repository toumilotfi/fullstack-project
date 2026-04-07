const runtimeConfig = globalThis.__appConfig ?? {};

export const environment = {
  production: true,
  apiUrl: runtimeConfig.apiUrl ?? '/api/v1',
  wsUrl: runtimeConfig.wsUrl ?? '/chat'
};
